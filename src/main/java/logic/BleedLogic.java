package logic;

import commander.*;
import org.apache.commons.lang3.tuple.Triple;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import sheets.PlotPointHandler;
import sheets.SheetsHandler;
import util.OptionalHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BleedLogic {
    public static CommandSpec getSpec() {
        return CommandSpecBuilder.of("bleed")
                .withDescription("Applies plot point bleed!")
                .withUsage("~bleed <targets> [modifer]")
                .withParameters(
                        CommandParameterBuilder.of("targets").withDescription("The party or player to bleed").withType(SlashCommandOptionType.MENTIONABLE).withRequired(true),
                        CommandParameterBuilder.of("modifier").withDescription("The bonus or penalty on the bleed").withType(SlashCommandOptionType.INTEGER).withRequired(false)
                )
                .withHandler(BleedLogic::handle)
                .build();
    }

    /**
     * Sends an embed that contains the changes in plot points after bleed, and the total session bleed value
     * <p>
     * Players whose plot points cannot be modified will be listed in the embed
     *
     * @param channel          The channel to send the embed to
     * @param user             The user that sent the command
     * @param plotPointChanges A list of triples containing changes in plot points
     * @return An embed that contains the change in plot points the the total bleed amount
     */
    private static EmbedBuilder generateBleedEmbed(TextChannel channel, User user, List<Triple<User, Integer, Integer>> plotPointChanges, int modifier) {
        int totalBleed = plotPointChanges.stream().mapToInt(plotPointChange -> plotPointChange.getMiddle() - plotPointChange.getRight()).sum();
        return PlotPointHandler.generateEmbed(plotPointChanges, channel, user).setTitle("Post-session Bleed!").setDescription(MessageFormat.format("What can you do with **{0}** plot point(s)?", totalBleed));
    }


    /**
     * Applies end of session bleed to the party and record the changes in plot points
     *
     * @param plotPointChanges  A list recording the changes in plot points for this session
     * @param uneditablePlayers A list of players whose plot points were not edited
     * @param user              The user that will lose plot points
     * @return A CompletableFuture representing the completion of the bleed operation on a user with exceptions handled
     */
    private static CompletableFuture<Optional<Integer>> applyBleed(List<Triple<User, Integer, Integer>> plotPointChanges, List<User> uneditablePlayers, User user) {
        final Optional<Integer> oldPlotPointCount = SheetsHandler.getPlotPoints(user);
        final Optional<Integer> playerBleed = SheetsHandler.getPlayerBleed(user);
        if (oldPlotPointCount.isPresent() && playerBleed.isPresent()) {
            final int newPlotPointCount = oldPlotPointCount.get() - playerBleed.get();
            return PlotPointHandler.setPlotPointsAndLog(plotPointChanges, uneditablePlayers, user, oldPlotPointCount.get(), newPlotPointCount);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }


    public static Optional<CommandResponse> handle(CommandContext context) {
        Optional<Mentionable> target = context.getOptions().stream()
                .filter(applicationCommandInteractionOption -> applicationCommandInteractionOption.getName().equals("targets"))
                .findFirst()
                .flatMap(SlashCommandInteractionOption::getMentionableValue);
        Optional<Integer> modifier = context.getOptions().stream()
                .filter(applicationCommandInteractionOption -> applicationCommandInteractionOption.getName().equals("modifier"))
                .findFirst()
                .flatMap(SlashCommandInteractionOption::getIntValue);

        final Mentionable mentionable = target.get();
        List<User> mentionedUsers = new ArrayList<>();
        if (mentionable instanceof User) {
            mentionedUsers.add((User) mentionable);
        }
        else if (mentionable instanceof Role) {
            mentionedUsers.addAll(((Role) mentionable).getUsers());
        }
        List<Triple<User, Integer, Integer>> plotPointChanges = new ArrayList<>();
        List<User> uneditablePlayers = new ArrayList<>();
        final List<CompletableFuture<Optional<Integer>>> bleedFutureList = mentionedUsers.stream()
                .filter(user -> SheetsHandler.getPlayerBleed(user).map(integer -> integer > 0).orElse(false))
                .map(user -> applyBleed(plotPointChanges, uneditablePlayers, user))
                .collect(Collectors.toList());
        return OptionalHelper.tupled(context.getUser(), context.getDisplayName()).map(tuple -> CompletableFuture.allOf(bleedFutureList.toArray(new CompletableFuture[]{})).thenApply(unused -> generateBleedEmbed(context.getChannel().get(), context.getUser().get(), plotPointChanges, modifier.orElse(0)))).map(CommandResponse::of);
    }

}
