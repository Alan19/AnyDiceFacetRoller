package commander;


import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CommandParameter {
    private final String name;
    private final String description;
    private final SlashCommandOptionType type;
    private final String matchingRegex;
    private final boolean required;
    private final String[] choices;

    public CommandParameter(String name, String description, SlashCommandOptionType type, String matchingRegex, boolean required, String[] choices) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.matchingRegex = matchingRegex;
        this.required = required;
        this.choices = choices;
    }

    public SlashCommandOption generateOption() {
        return new SlashCommandOptionBuilder()
                .setName(name)
                .setDescription(description)
                .setType(type)
                .setRequired(required)
                .setChoices(Arrays.stream(choices).map(s -> SlashCommandOptionChoice.create(name, s)).collect(Collectors.toList()))
                .build();
    }
}
