package commander;

import org.javacord.api.interaction.ApplicationCommandOptionType;

import java.util.Objects;

public class CommandParameterBuilder {
    private String name;
    private String description;
    private ApplicationCommandOptionType type;
    private String matchingRegex;
    private boolean required;
    private String[] choices;

    public CommandParameterBuilder(String name) {
        this.name = name;
    }

    public CommandParameterBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandParameterBuilder withType(ApplicationCommandOptionType type) {
        this.type = type;
        return this;
    }

    public CommandParameterBuilder withChoices(String[] choices) {
        this.choices = choices;
        return this;
    }

    public CommandParameterBuilder withRegex(String regex) {
        this.matchingRegex = regex;
        return this;
    }

    public CommandParameterBuilder withRequired(boolean required) {
        this.required = required;
        return this;
    }

    public CommandParameter build() {
        return new CommandParameter(Objects.requireNonNull(name),
                Objects.requireNonNull(description),
                Objects.requireNonNull(type),
                matchingRegex == null ? "" : matchingRegex,
                required,
                choices == null ? new String[0] : choices);
    }

    public static CommandParameterBuilder of(String name) {
        return new CommandParameterBuilder(Objects.requireNonNull(name));
    }
}
