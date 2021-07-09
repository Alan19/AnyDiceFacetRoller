package commander;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandContext {
    private final DiscordApi api;
    private final Message message;
    private final User user;
    private final List<SlashCommandInteractionOption> options;
    private final MessageAuthor author;
    private final TextChannel channel;
    private final Server server;

    public CommandContext(DiscordApi api, Message message) {
        this.api = api;
        this.message = message;
        this.user = message.getUserAuthor().orElse(null);
        this.author = message.getAuthor();
        this.channel = message.getChannel();
        this.server = message.getServer().orElse(null);
        this.options = new ArrayList<>();
    }

    public CommandContext(DiscordApi api, Message message, User user, TextChannel channel, Server server, List<SlashCommandInteractionOption> options) {
        this.api = api;
        this.message = message;
        this.user = user;
        this.options = options;
        this.author = null;
        this.channel = channel;
        this.server = server;
    }

    public List<SlashCommandInteractionOption> getOptions() {
        return options;
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
