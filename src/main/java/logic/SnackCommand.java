package logic;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class SnackCommand {

    private MessageAuthor author;

    public SnackCommand(MessageAuthor author) {
        this.author = author;
    }

    public EmbedBuilder dispenseSnack(){
        return new EmbedBuilder()
                .setColor(RandomColor.getRandomColor())
                .setAuthor(author)
                .setTitle("A snack for " + author.getDiscriminatedName())
                .setDescription("Here is a cookie")
                .setImage("");
    }
}