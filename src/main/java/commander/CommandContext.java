package commander;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores information that would allow a command class to handle the incoming command
 * <p>
 * Includes information about the slash command options, text command options, and various information about the message
 */
public class CommandContext {
    private final DiscordApi api;
    private final Message message;
    private final User user;
    private final List<SlashCommandInteractionOption> slashCommandOptions;
    private final Map<String, String> textOptions;
    private final MessageAuthor author;
    private final TextChannel channel;
    private final Server server;

    public CommandContext(DiscordApi api, Message message, Map<String, String> options) {
        this.api = api;
        this.message = message;
        this.user = message.getUserAuthor().orElse(null);
        this.author = message.getAuthor();
        this.channel = message.getChannel();
        this.server = message.getServer().orElse(null);
        this.slashCommandOptions = null;
        this.textOptions = options;
    }

    public CommandContext(DiscordApi api, Message message, User user, TextChannel channel, Server server, List<SlashCommandInteractionOption> slashCommandOptions) {
        this.api = api;
        this.message = message;
        this.user = user;
        this.slashCommandOptions = slashCommandOptions;
        this.author = null;
        this.channel = channel;
        this.server = server;
        this.textOptions = null;
    }

    public List<SlashCommandInteractionOption> getSlashCommandOptions() {
        return slashCommandOptions;
    }

    public Optional<Message> getMessage() {
        return Optional.ofNullable(this.message);
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(this.user);
    }

    public Optional<TextChannel> getChannel() {
        return Optional.ofNullable(this.channel);
    }

    public Optional<MessageAuthor> getAuthor() {
        return Optional.ofNullable(this.author);
    }

    public Optional<Server> getServer() {
        return Optional.ofNullable(this.server);
    }

    public DiscordApi getApi() {
        return api;
    }

    public Optional<Map<String, String>> getTextOptions() {
        return Optional.ofNullable(textOptions);
    }

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(this.getAuthor()
                .map(MessageAuthor::getDisplayName)
                .orElseGet(() -> {
                    if (server != null && user != null) {
                        return server.getDisplayName(user);
                    }
                    else {
                        return null;
                    }
                })
        );
    }

}
