package commander;

import org.javacord.api.interaction.ApplicationCommandOption;
import org.javacord.api.interaction.ApplicationCommandOptionBuilder;
import org.javacord.api.interaction.ApplicationCommandOptionChoice;
import org.javacord.api.interaction.ApplicationCommandOptionType;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public ApplicationCommandOption generateOption() {
        return new ApplicationCommandOptionBuilder()
                .setName(name)
                .setDescription(description)
                .setType(type)
                .setRequired(required)
                .setChoices(Arrays.stream(choices).map(s -> ApplicationCommandOptionChoice.create(name, s)).collect(Collectors.toList()))
                .build();
    }
}
