package listeners;

import logic.PlotPointEnhancementHelper;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import sheets.PPManager;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Listener that listens for a user using plot point enhancement
 */
public class PlotPointEnhancementListener implements EventListener {

    public PlotPointEnhancementListener(DiscordApi api) {
        startListening(api);
    }

    @Override
    public void startListening(DiscordApi api) {
        api.addReactionAddListener(event -> {
            //Do nothing if the bot is the one who reacts
            if (event.getUser().isYourself()) {
                return;
            }
            //Check if bot has made the react on the post already
            event.getReaction().ifPresent(reaction -> {
                if (reaction.containsYou()) {
                    event.requestMessage().thenAcceptAsync(message -> {
                        //Get user roll value and add to that based on reaction. Then deduct plot points.
                        int rollVal = Integer.parseInt(message.getEmbeds().get(0).getFields().get(4).getValue());
                        Emoji emoji = reaction.getEmoji();
                        int toAdd = getAddAmount(emoji);
                        PPManager manager = new PPManager();
                        User user = event.getUser();
                        int oldPP = manager.getPlotPoints(user.getIdAsString());
                        int newPP = manager.setPlotPoints(user.getIdAsString(), oldPP - toAdd);
                        EmbedBuilder embedBuilder = message.getEmbeds()
                                .get(0)
                                .toBuilder()
                                .addInlineField("Enhancing roll...", rollVal + " → " + (rollVal + toAdd))
                                .addInlineField("Plot points", oldPP + " → " + newPP);
                        message.edit(embedBuilder);
                        try {
                            message.removeAllReactions().get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

        });
    }

    private int getAddAmount(Emoji emoji) {
        int toAdd = 0;
        PlotPointEnhancementHelper helper = new PlotPointEnhancementHelper();
        if (emoji.isCustomEmoji()) {
            String tag = emoji.asKnownCustomEmoji().get().getMentionTag();
            String trimmedEmoji = tag.substring(2, tag.length() - 1);
            for (Map.Entry<Integer, String> emojiEntry : helper.getOneToTwelveEmojiMap().entrySet()) {
                if (emojiEntry.getValue().equals(trimmedEmoji)) {
                    toAdd = emojiEntry.getKey();
                }
            }

        } else {
            String unicodeEmoji = emoji.asUnicodeEmoji().get();
            for (Map.Entry<Integer, String> emojiEntry :
                    helper.getOneToTwelveEmojiMap().entrySet()) {
                if (emojiEntry.getValue().equals(unicodeEmoji)) {
                    toAdd = emojiEntry.getKey();
                }
            }
        }
        return toAdd;
    }
}
