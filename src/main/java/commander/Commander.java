package commander;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.interaction.InteractionCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.ServerJoinListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores all of the command specs and the handlers for handling slash and text commands. Registers the slash commands.
 */
public class Commander implements MessageCreateListener, InteractionCreateListener, ServerJoinListener {
    private final Pattern commandPattern;
    private final DiscordApi api;
    private final Map<String, CommandSpec> commandSpecs;

    public Commander(DiscordApi api, String prefix, CommandSpec... commandSpecs) {
        this.api = api;
        this.commandPattern = Pattern.compile(prefix + "(?<command>\\w+)");
        this.commandSpecs = new LinkedHashMap<>();
        for (CommandSpec commandSpec : commandSpecs) {
            this.commandSpecs.put(commandSpec.getName(), commandSpec);
            for (String alias : commandSpec.getAlias()) {
                this.commandSpecs.put(alias, commandSpec);
            }
        }
    }

    public void register() {
        this.api.addMessageCreateListener(this);
        this.api.addInteractionCreateListener(this);
        this.api.addServerJoinListener(this);
        this.api.getServers()
                .forEach(server -> CompletableFuture.allOf(this.commandSpecs.values()
                        .parallelStream()
                        .map(value -> new SlashCommandBuilder()
                                .setName(value.getName())
                                .setDescription(value.getDescription())
                                .setOptions(value.getOptions())
                                .createForServer(server)
                        )
                        .toArray(CompletableFuture[]::new)
                ));

    }

    /**
     * Handles a text command by matching the command and then matching the parameters. Then creates a context with the
     * message and the options and handles it.
     *
     * @param event The MessageCreateEvent
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        // TODO Properly compile parameters
        Matcher matcher = commandPattern.matcher(event.getMessageContent());
        if (matcher.matches()) {
            String command = matcher.group("command");
            CommandSpec commandSpec = commandSpecs.get(command);
            if (commandSpec != null) {
                Map<String, String> options = new HashMap<>();
                for (CommandParameter commandParameter : commandSpec.getParameters()) {
                    Pattern optionPattern = Pattern.compile(commandParameter.getMatchingRegex());
                    options.put(commandParameter.getDescription(), optionPattern.matcher(event.getMessageContent()).group());
                }
                commandSpec.getHandler()
                        .apply(new CommandContext(api, event.getMessage(), options))
                        .ifPresent(commandResponse -> {
                            MessageBuilder messageBuilder = new MessageBuilder();
                            commandResponse.handle(messageBuilder::setContent, messageBuilder::setEmbed);
                            messageBuilder.send(event.getChannel());
                        });
            }
        }
    }

    @Override
    public void onInteractionCreate(InteractionCreateEvent event) {
        event.getSlashCommandInteraction()
                .ifPresent(interaction -> {
                    CommandSpec commandSpec = commandSpecs.get(interaction.getCommandName());
                    if (commandSpec != null) {
                        CompletableFuture<InteractionOriginalResponseUpdater> responseBuilder = interaction.respondLater();
                        commandSpec.getHandler()
                                .apply(new CommandContext(
                                        api,
                                        null,
                                        event.getInteraction().getUser(),
                                        interaction.getChannel().orElse(null),
                                        interaction.getServer().orElse(null),
                                        interaction.getOptions()
                                ))
                                .orElseGet(() -> CommandResponse.of("Acknowledged"))
                                .handle(responseBuilder);
                    }
                });
    }

    @Override
    public void onServerJoin(ServerJoinEvent event) {
        CompletableFuture.allOf(this.commandSpecs.values()
                .parallelStream()
                .map(value -> new SlashCommandBuilder()
                        .setName(value.getName())
                        .setDescription(value.getDescription())
                        .createForServer(event.getServer())
                )
                .toArray(CompletableFuture[]::new)
        );
    }
}
