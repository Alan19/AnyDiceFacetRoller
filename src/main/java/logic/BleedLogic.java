package logic;

import commander.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.ApplicationCommandOptionType;
import util.OptionalHelper;
import util.RandomColor;

import java.util.Optional;

public class BleedLogic {
    public static CommandSpec getSpec() {
        return CommandSpecBuilder.of("bleed")
                .withDescription("Applies plot point bleed!")
                .withUsage("~bleed <targets> [modifer]")
                .withParameters(
                        CommandParameterBuilder.of("targets").withDescription("the party or player to bleed").withType(ApplicationCommandOptionType.MENTIONABLE).withRequired(true),
                        CommandParameterBuilder.of("modifier").withDescription("The bonus or penalty on the bleed").withType(ApplicationCommandOptionType.INTEGER).withRequired(false)
                )
                .withHandler(BleedLogic::handle)
                .build();
    }

    public static Optional<CommandResponse> handle(CommandContext context) {
        return OptionalHelper.tupled(context.getUser(), context.getDisplayName())
                .map(tuple -> new EmbedBuilder()
                        .setColor(RandomColor.getRandomColor())
                        .setAuthor(tuple.getLeft())
                        .setTitle("A snack for " + tuple.getRight())
                        .setDescription("Here is a cookie!")
                        .setImage("https://upload.wikimedia.org/wikipedia/commons/5/5c/Choc-Chip-Cookie.png")
                )
                .map(CommandResponse::of);
    }

}
