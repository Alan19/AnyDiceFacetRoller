package commander;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandSpecBuilder {
    private final String name;
    private String[] alias;
    private String description;
    private String usage;
    private CommandSpec[] children;
    private CommandParameterBuilder[] parameters;
    private Function<CommandContext, Optional<CommandResponse>> handler;

    public CommandSpecBuilder(String name) {
        this.name = name;
    }

    public CommandSpecBuilder withAlias(String... alias) {
        this.alias = alias;
        return this;
    }

    public CommandSpecBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandSpecBuilder withUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandSpecBuilder withChildren(CommandSpec... children) {
        if (parameters.length == 0) {
            for (CommandSpec child : children) {
                if (!child.getChildren().isEmpty()) {
                    throw new IllegalArgumentException("Cannot add Children that have Children");
                }
            }
            this.children = children;
        }
        else {
            throw new UnsupportedOperationException("Commands with parameters cannot have children");
        }
        return this;
    }

    public CommandSpecBuilder withParameters(CommandParameterBuilder... parameters) {
        if (children.length == 0) {
            this.parameters = parameters;
        }
        else {
            throw new UnsupportedOperationException("Commands with children cannot have parameters");
        }
        return this;
    }

    public CommandSpecBuilder withHandler(Consumer<CommandContext> handler) {
        this.handler = context -> {
            handler.accept(context);
            return Optional.empty();
        };
        return this;
    }

    public CommandSpecBuilder withHandler(Function<CommandContext, Optional<CommandResponse>> handler) {
        this.handler = handler;
        return this;
    }

    public CommandSpec build() {
        return new CommandSpec(
                Objects.requireNonNull(name),
                alias == null ? new String[0] : alias,
                Objects.requireNonNull(description),
                Objects.requireNonNull(usage),
                children == null ? new CommandSpec[0] : children,
                parameters == null ? new CommandParameter[0] : Arrays.stream(parameters).map(CommandParameterBuilder::build).collect(Collectors.toList()).toArray(new CommandParameter[]{}),
                children == null ? Objects.requireNonNull(handler, "Must have Children or Handler") : handler
        );
    }

    public static CommandSpecBuilder of(String name) {
        return new CommandSpecBuilder(Objects.requireNonNull(name));
    }
}
