package commands;

import com.vdurmont.emoji.EmojiParser;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import dicerolling.DicePool;
import dicerolling.DiceRoller;
import dicerolling.PoolProcessor;
import doom.DoomHandler;
import util.PlotPointEnhancementHelper;
import org.apache.commons.lang3.tuple.Triple;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import roles.Storytellers;
import sheets.PlotPointHandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RollCommand implements CommandExecutor {
    /**
     * Rolls a dice pool and sends the output to the channel the message was sent in
     * <p>
     * Performs side effects based on the dice pool, sender, and if the roll is a reroll
     *
     * @param channel  The channel the command was sent from
     * @param message  The message for the command
     * @param dicePool The dice pool to be rolled
     * @param isReroll Whether the roll is a reroll
     */
    private static void rollPoolAndSend(TextChannel channel, Message message, DicePool dicePool, boolean isReroll) {
        DiceRoller diceRoller = new DiceRoller(dicePool);
        final CompletableFuture<Message> sentMessageFuture = channel.sendMessage(diceRoller.generateResults(message.getAuthor())).exceptionally(ExceptionLogger.get());
        sentMessageFuture.thenAcceptAsync(sentMessage -> {
            if (!isReroll) {
                handlePlotPointChanges(message, dicePool.getPlotPointsSpent(), dicePool.getPlotPointDiscount());
            }
            if (!Storytellers.isMessageAuthorStoryteller(message.getAuthor()) && dicePool.areOpportunitiesEnabled()) {
                handleOpportunities(message.getAuthor(), sentMessage, channel, dicePool.areOpportunitiesEnabled(), diceRoller.getDoom());
            }
            if (dicePool.isEnhancementEnabled()) {
                PlotPointEnhancementHelper.addPlotPointEnhancementEmojis(sentMessage);
                queueReactionRemoval(sentMessage);
            }
            if (!isReroll) {
                attachRerollReaction(message, dicePool, diceRoller, sentMessage, channel);
            }
        });
    }

    /**
     * Re-rolls the previous roll. Plot points spent by the player are not spent again, plot and doom points from opportunites are reverted before the re-roll.
     *
     * @param channel  The channel the reroll is in
     * @param message  The message that started the reroll
     * @param dicePool The dice pool to be re-rolled
     * @param doom     The number of doom points generated by the previous roll
     */
    private static void reroll(TextChannel channel, Message message, DicePool dicePool, int doom) {
        // Revert opportunities and plot point spending
        if (dicePool.areOpportunitiesEnabled()) {
            final EmbedBuilder doomEmbed = DoomHandler.addDoom(doom * -1);
            final boolean isThereAnOpportunity = doom > 0;
            final int changeInPlotPoints = (isThereAnOpportunity ? -1 : 0) + dicePool.getPlotPointsSpent() - dicePool.getPlotPointDiscount();
            if (changeInPlotPoints != 0) {
                final ArrayList<Triple<User, Integer, Integer>> plotPointChanges = new ArrayList<>();
                PlotPointHandler.addPlotPointsToUser(message.getUserAuthor().get(), changeInPlotPoints, plotPointChanges, new ArrayList<>()).thenAccept(integer -> channel.sendMessage(PlotPointHandler.generateEmbed(plotPointChanges, channel, message.getAuthor()))).join();
            }
            if (doom * -1 != 0) {
                channel.sendMessage(doomEmbed).join();
            }
        }
        rollPoolAndSend(channel, message, dicePool, true);
    }

    /**
     * Attach the reroll reaction and attach a listener that lasts for 60 seconds (same as the other reacts)
     *
     * @param userMessage The message containing the roll command
     * @param dicePool    The dice pool for rolling
     * @param diceRoller  The dice roller object containing the result of the roll
     * @param sentMessage The message that contains the embed with the roll result
     * @param channel     The channel the message was sent from
     */
    public static void attachRerollReaction(Message userMessage, DicePool dicePool, DiceRoller diceRoller, Message sentMessage, TextChannel channel) {
        sentMessage.addReaction(EmojiParser.parseToUnicode(":repeat:")).thenAccept(unused -> sentMessage.addReactionAddListener(event -> onRerollReact(userMessage, dicePool, diceRoller, sentMessage, channel, event)).removeAfter(60, TimeUnit.SECONDS));
    }

    public static void rollPoolAndSend(TextChannel channel, Message message, DicePool dicePool) {
        rollPoolAndSend(channel, message, dicePool, false);
    }

    /**
     * Deletes the reactions from a message after 60 seconds
     *
     * @param sentMessage The message containing the embed for the roll
     */
    private static void queueReactionRemoval(Message sentMessage) {
        sentMessage.getApi().getThreadPool().getScheduler().schedule(() -> PlotPointEnhancementHelper.removeEnhancementEmojis(sentMessage), 60, TimeUnit.SECONDS);
    }

    /**
     * Adds plot points and doom points when rolling a 1
     *
     * @param author                  The author that made the roll
     * @param rollEmbedMessage        The message with the embed for the roll
     * @param channel                 The channel the roll was made in
     * @param areOpportunitiesEnabled Whether opportunities are enabled
     * @param doomGenerated           The amount of doom generated
     */
    private static void handleOpportunities(MessageAuthor author, Message rollEmbedMessage, TextChannel channel, boolean areOpportunitiesEnabled, int doomGenerated) {
        // Send embed for plot points and doom if there's an opportunity
        if (areOpportunitiesEnabled && doomGenerated >= 1) {
            rollEmbedMessage.addReaction(EmojiParser.parseToUnicode(":eight_pointed_black_star:"));
            EmbedBuilder doomEmbed = DoomHandler.addDoom(doomGenerated);
            ArrayList<Triple<User, Integer, Integer>> plotPointChanges = new ArrayList<>();
            if (author.asUser().isPresent()) {
                PlotPointHandler.addPlotPointsToUser(author.asUser().get(), 1, plotPointChanges, new ArrayList<>())
                        .thenAccept(integer -> channel.sendMessage(PlotPointHandler.generateEmbed(plotPointChanges, channel, author).setTitle("An opportunity!")))
                        .join();
                doomEmbed.setFooter(MessageFormat.format("Generated by {0}!", PlotPointHandler.getUsernameInChannel(author.asUser().get(), channel)));
            }
            channel.sendMessage(doomEmbed);
        }
    }

    /**
     * Sends an embed for spending plot points on a roll
     *
     * @param message     The message of the command
     * @param pointsSpent The number of plot points spent
     * @param discount    The discount on the roll
     */
    private static void handlePlotPointChanges(Message message, int pointsSpent, int discount) {
        final int plotPointsSpent = pointsSpent - discount;
        MessageAuthor author = message.getAuthor();
        TextChannel channel = message.getChannel();
        if (plotPointsSpent != 0) {
            if (Storytellers.isMessageAuthorStoryteller(author)) {
                //DMs use doom points as plot points and 1s do not increase the doom pool
                EmbedBuilder doomEmbed = DoomHandler.addDoom(plotPointsSpent * -1);
                channel.sendMessage(doomEmbed.setTitle(MessageFormat.format("Using {0} doom points!", plotPointsSpent)));
            }
            else if (author.asUser().isPresent()) {
                //Players have to spend plot points and gain doom points on opportunities
                ArrayList<Triple<User, Integer, Integer>> plotPointChanges = new ArrayList<>();
                PlotPointHandler.addPlotPointsToUser(author.asUser().get(), plotPointsSpent * -1, plotPointChanges, new ArrayList<>())
                        .thenAccept(integer -> channel.sendMessage(PlotPointHandler.generateEmbed(plotPointChanges, channel, author)
                                .setTitle(MessageFormat.format("Using {0} plot points!", plotPointsSpent)))).join();
            }
        }
    }

    /**
     * When a user reacts to the repeat reaction,
     *
     * @param userMessage The message the user sent to roll dice
     * @param dicePool    The dice pool for the roll
     * @param diceRoller  The dice roller object containing the results of the roll
     * @param sentMessage The message with an embed containing result of the roll
     * @param channel     The channel the message was sent in
     * @param event       The reaction event
     */
    private static void onRerollReact(Message userMessage, DicePool dicePool, DiceRoller diceRoller, Message sentMessage, TextChannel channel, ReactionAddEvent event) {
        if (event.getUser().map(user -> !user.isYourself()).orElse(false) && event.getReaction().map(reaction -> reaction.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":repeat:"))).orElse(false)) {
            event.addReactionsToMessage(EmojiParser.parseToUnicode(":bulb:"));
            PlotPointEnhancementHelper.removeEnhancementEmojis(sentMessage).thenAccept(reactionsRemoved -> reroll(channel, userMessage, dicePool, diceRoller.getDoom()));
        }
    }

    /**
     * Rolls a pool of dice based on the input. After rolling, adds doom points to doom pool and makes appropriate changes the player's plot point count based on input options. If the DM is rolling, plot points they spend come from the doom point pool.
     *
     * @param author  The author of the message
     * @param message The message containing the command
     * @param channel The channel the message was sent from
     */
    @Command(aliases = {"~r", "~roll"}, description = "A command that allows you to roll dice\n\tdie: A string representing a die. Some die examples are d4, pd12, 3d12, kd12, +3.\n\tskill: The value of a cell from a character's spreadsheet with no spaces and all lowercase.", privateMessages = false, usage = "~r [-fsu=x|-fsd=x|-maxf=x|-diff=|-k=x|-pdisc=x|-enh=true/false|-opp=true/false|-nd=pd/d/kd|-minf=x] die|skill [die|skill ...]")
    public void onRollCommand(MessageAuthor author, Message message, TextChannel channel) {
        String messageContent = message.getContent();

        //Variables containing roll information
        final PoolProcessor poolProcessor = new PoolProcessor(author, messageContent);
        if (poolProcessor.getErrorEmbed() != null) {
            new MessageBuilder().setEmbed(poolProcessor.getErrorEmbed()).send(channel);
        }
        else {
            final DicePool dicePool = poolProcessor.getDicePool();
            rollPoolAndSend(channel, message, dicePool);
        }
    }

}
