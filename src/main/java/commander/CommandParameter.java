package commander;

import org.javacord.api.interaction.ApplicationCommandOptionType;

public class CommandParameter {
    private final String name;
    private final String description;
    private final ApplicationCommandOptionType type;
    private final String matchingRegex;
    private final boolean required;
    private final String[] choices;

    public CommandParameter(String name, String description, ApplicationCommandOptionType type, String matchingRegex, boolean required, String[] choices) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.matchingRegex = matchingRegex;
        this.required = required;
        this.choices = choices;
    }
}
