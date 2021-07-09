package commander;

import com.google.common.collect.Maps;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandSpec {
    private final String name;
    private final String[] alias;
    private final String description;
    private final String usage;
    private final Map<String, CommandSpec> children;
    private final CommandParameter[] parameters;
    private final Function<CommandContext, Optional<CommandResponse>> handler;

    // TODO Add way to determine if command should respond immediately or later
    public CommandSpec(String name, String[] alias, String description, String usage, CommandSpec[] children, CommandParameter[] parameters, Function<CommandContext, Optional<CommandResponse>> handler) {
        this.name = name;
        this.alias = alias;
        this.description = description;
        this.usage = usage;
        this.parameters = parameters;
        this.children = Maps.newLinkedHashMap();
        for (CommandSpec child : children) {
            this.children.put(child.getName(), child);
            for (String childrenAlias : child.getAlias()) {
                this.children.put(childrenAlias, child);
            }
        }
        this.handler = handler;
    }

    public CommandParameter[] getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public String[] getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public Function<CommandContext, Optional<CommandResponse>> getHandler() {
        return handler;
    }

    public Collection<CommandSpec> getChildren() {
        return children.values();
    }

    public CommandSpec getChild(String name) {
        return children.get(name);
    }

    public List<SlashCommandOption> getOptions() {
        if (!children.isEmpty()) {
            return children.entrySet().stream().map(stringCommandSpecEntry -> new SlashCommandOptionBuilder()
                    .setName(stringCommandSpecEntry.getKey())
                    .setDescription("a subcommand for " + getName())
                    .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                    .setRequired(true)
                    .setOptions(stringCommandSpecEntry.getValue().getOptions()).build()).collect(Collectors.toList());
        }
        else if (parameters.length != 0) {
            return Arrays.stream(parameters).map(CommandParameter::generateOption).collect(Collectors.toList());
        }
        else
            return new ArrayList<>();
    }
}
