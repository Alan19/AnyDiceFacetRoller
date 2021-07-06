package commander;

import org.javacord.api.interaction.ApplicationCommandOptionType;

public class CommandParameterBuilder {
    private String name;
    private String description;
    private ApplicationCommandOptionType type;
    private String matchingRegex;
    private boolean required;
    private String[] choices;

    public CommandParameterBuilder(String name, String description, ApplicationCommandOptionType type, String matchingRegex) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.matchingRegex = matchingRegex;
        this.required = false;
    }

    public CommandParameterBuilder isRequired(boolean required) {
        this.required = required;
        return this;
    }

    public CommandParameterBuilder setChoices(String[] choices) {
        this.choices = choices;
        return this;
    }

    public CommandParameter build() {
        return new CommandParameter(name, description, type, matchingRegex, required, choices);
    }
}
