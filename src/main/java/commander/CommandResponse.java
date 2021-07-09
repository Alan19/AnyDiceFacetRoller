package commander;

import org.apache.commons.lang3.tuple.Pair;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CommandResponse {
    private final CompletableFuture<String> content;
    private final CompletableFuture<EmbedBuilder> embed;

    public CommandResponse(CompletableFuture<String> content, CompletableFuture<EmbedBuilder> embedBuilderCompletableFuture) {
        this.content = content;
        this.embed = embedBuilderCompletableFuture;
    }

    public static CommandResponse of(String content) {
        return new CommandResponse(CompletableFuture.completedFuture(content), CompletableFuture.completedFuture(null));
    }

    public static CommandResponse of(EmbedBuilder embed) {
        return new CommandResponse(CompletableFuture.completedFuture(null), CompletableFuture.completedFuture(embed));
    }

    public static CommandResponse of(CompletableFuture<EmbedBuilder> embedBuilderCompletableFuture) {
        return new CommandResponse(CompletableFuture.completedFuture(null), embedBuilderCompletableFuture);
    }

    private static void updateResponder(Pair<String, EmbedBuilder> interactionResponse, InteractionOriginalResponseUpdater updater) {
        if (interactionResponse.getLeft() != null) {
            updater.setContent(interactionResponse.getLeft());
        }
        if (interactionResponse.getRight() != null) {
            updater.addEmbed(interactionResponse.getRight());
        }
        updater.update();
    }

    public void handle(Consumer<String> contentConsumer, Consumer<EmbedBuilder> embedConsumer) {
        if (content != null) {
            content.thenAccept(contentConsumer);
        }
        if (embed != null) {
            embed.thenAccept(embedConsumer);
        }
    }

    public void handle(CompletableFuture<InteractionOriginalResponseUpdater> responseBuilder) {
        final CompletableFuture<Pair<String, EmbedBuilder>> interactionResponse = content.thenCombine(embed, Pair::of);
        interactionResponse.thenAcceptBoth(responseBuilder, CommandResponse::updateResponder);
    }
}
