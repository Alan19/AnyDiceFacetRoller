package slashcommands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SlashCommands {
    public static CompletableFuture<SlashCommand> registerBleedCommand(DiscordApi api) {
        final Server server = getServer(api);
        final List<SlashCommandOption> SlashCommandOptions = new ArrayList<>();
        SlashCommandOptions.add(SlashCommandOption.create(SlashCommandOptionType.MENTIONABLE, "targets", "The party or player to bleed", true));
        SlashCommandOptions.add(SlashCommandOption.create(SlashCommandOptionType.INTEGER, "modifier", "The bonus or penalty on the bleed", false));
        SlashCommandOptions.add(SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "bleed-all", "If all plot points should be lost instead of only half of above the cap", false));
        final SlashCommandBuilder builder = new SlashCommandBuilder().setName("bleed").setDescription("Applies plot point bleed!").setOptions(SlashCommandOptions);
        return builder.createForServer(server);
    }

    public static CompletableFuture<SlashCommand> registerDoomCommand(DiscordApi api) {
        final List<SlashCommandOption> SlashCommandOptions = new ArrayList<>();
        final SlashCommandOptionBuilder[] SlashCommandOptionBuilders = {getNameOption(), getCountOption()};
        SlashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Adds to the doom pool", SlashCommandOptionBuilders));
        SlashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "sub", "Subtracts from the doom pool", SlashCommandOptionBuilders));
        SlashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "set", "Sets from the doom pool to the specified amount", getCountOption().setRequired(true), getNameOption()));
        SlashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "select", "Chooses the specified doom pool as the active doom pool", getNameOption().setRequired(true)));
        SlashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "query", "Queries the value of all doom pools", getNameOption().setDescription("the name of the doom pool to query")));
        SlashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "delete", "Deletes the doom pool from the doom pool tracker", getNameOption().setDescription("the name of the doom pool to delete")));
        final SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName("doom")
                .setDescription("Accesses the doom pool")
                .addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, "mode", "how to modify the doom pool", SlashCommandOptions));
        return builder.createForServer(getServer(api));
    }

    public static CompletableFuture<SlashCommand> registerPlotPointCommand(DiscordApi api) {
        final List<SlashCommandOption> slashCommandOptions = new ArrayList<>();
        final SlashCommandOptionBuilder[] slashCommandOptionBuilders = {getMentionableOption(), getPlotPointCountOption()};
        slashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Adds to the specified plot point pool(s)", slashCommandOptionBuilders));
        slashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "sub", "Subtracts from the specified plot point pool(s)", slashCommandOptionBuilders));
        slashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "set", "Sets the specified plot point pools to the specified amount", getCountOption().setRequired(true), getNameOption()));
        slashCommandOptions.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "query", "Queries the value of all plot point pools", getNameOption().setDescription("the name of the plot point pool to query")));
        final SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName("plotpoints")
                .setDescription("Accesses the plot point pools")
                .addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, "mode", "how to modify the plot point pools", slashCommandOptions));
        return builder.createForServer(getServer(api));
    }

    private static SlashCommandOptionBuilder getNameOption() {
        return new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.STRING)
                .setName("name")
                .setDescription("the name of the doom pool to modify")
                .setRequired(false);
    }

    private static SlashCommandOptionBuilder getMentionableOption() {
        return new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.MENTIONABLE)
                .setName("name")
                .setDescription("the user(s) to target with the command")
                .setRequired(false);
    }

    private static SlashCommandOptionBuilder getCountOption() {
        return new SlashCommandOptionBuilder()
                .setName("count")
                .setDescription("the amount to modify the doom pool by")
                .setType(SlashCommandOptionType.INTEGER)
                .setRequired(false);
    }

    private static SlashCommandOptionBuilder getPlotPointCountOption() {
        return new SlashCommandOptionBuilder()
                .setName("count")
                .setDescription("the amount to modify the plot point pool by")
                .setType(SlashCommandOptionType.INTEGER)
                .setRequired(false);
    }

    private static Server getServer(DiscordApi api) {
        return api.getServerById(468046159781429250L).get();
    }

}
