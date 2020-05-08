package dicerolling;

import discord.TwoDee;
import doom.DoomWriter;
import logic.RandomColor;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import sheets.PPManager;
import statistics.RollResultBuilder;
import statistics.resultvisitors.DifficultyVisitor;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class NewDiceRoller {
    public static final String NONE = "*none*";
    private final Random random = new Random();
    private final RollResultBuilder rollResult;
    private final DicePool dicePool;

    public NewDiceRoller(DicePool dicePool) {
        this.dicePool = dicePool;
        rollResult = new RollResultBuilder(dicePool.getKeepHowMany(), false);
    }

    public int getDoom() {
        return rollResult.getDoom();
    }

    public EmbedBuilder generateResults(MessageAuthor author) {
        //Roll the dice
        rollDice(random);
        //Build embed
        return buildResultEmbed(author, rollResult);
    }

    private EmbedBuilder buildResultEmbed(MessageAuthor author, RollResultBuilder result) {
        return new EmbedBuilder()
                .setDescription("Here's the result for" + createDicePoolString(dicePool))
                .setTitle(TwoDee.getRollTitleMessage())
                .setAuthor(author)
                .setColor(RandomColor.getRandomColor())
                .addField("Regular dice", formatResults(result.getDice()), true)
                .addField("Picked", resultsToString(result.getPickedDice()), true)
                .addField("Dropped", resultsToString(result.getDropped()), true)
                .addField("Plot dice", resultsToString(result.getPlotDice()), true)
                .addField("Kept dice", resultsToString(result.getKeptDice()), true)
                .addField("Flat bonuses", resultsToString(result.getFlatBonus()), true)
                .addField("Total", String.valueOf(result.getTotal()), true)
                .addField("Tier Hit", tiersHit(result.getTotal()));
    }

    private String createDicePoolString(DicePool dicePool) {
        StringBuilder dicePoolString = new StringBuilder();
        dicePool.getRegularDice().forEach(integer -> dicePoolString.append(" d").append(integer));
        dicePool.getPlotDice().forEach(integer -> dicePoolString.append(" pd").append(integer));
        dicePool.getKeptDice().forEach(integer -> dicePoolString.append(" kd").append(integer));
        dicePool.getFlatBonuses().stream().map(integer -> (integer > 0) ? ("+" + integer) : integer).forEach(dicePoolString::append);
        return dicePoolString.toString();
    }

    private String tiersHit(int total) {
        DifficultyVisitor difficultyVisitor = new DifficultyVisitor();
        if (total < 3) {
            return "None";
        }
        StringBuilder output = new StringBuilder();
        for (Map.Entry<Integer, String> diffEntry : difficultyVisitor.getDifficultyMap().entrySet()) {
            if (difficultyVisitor.generateStageDifficulty(diffEntry.getKey() + 1) > total) {
                output.append(diffEntry.getValue());
                break;
            }
        }
        if (total < 10) {
            return String.valueOf(output);
        }
        for (Map.Entry<Integer, String> diffEntry : difficultyVisitor.getDifficultyMap().entrySet()) {
            if (difficultyVisitor.generateStageExtraordinaryDifficulty(diffEntry.getKey() + 1) > total) {
                output.append(", Extraordinary ").append(diffEntry.getValue());
                break;
            }
        }
        return String.valueOf(output);
    }

    //Bold 1s to show total doom generated. Runs doom increasing method afterwards.
    private String formatResults(List<Integer> s) {
        StringBuilder resultString = new StringBuilder();
        if (s.size() > 1) {
            for (int i = 0; i < s.size() - 1; i++) {
                if (s.get(i) == 1) {
                    resultString.append("**1**, ");
                }
                else {
                    resultString.append(s.get(i)).append(", ");
                }
            }
            if (s.get(s.size() - 1) == 1) {
                resultString.append("**1**");
            }
            else {
                resultString.append(s.get(s.size() - 1));
            }
        }
        else if (s.size() == 1) {
            if (s.get(0) == 1) {
                resultString.append("**1**");
            }
            else {
                resultString.append(s.get(0));
            }
        }
        else {
            return NONE;
        }
        return resultString.toString();
    }

    /**
     * Add 1 plot point if player generates an opportunity on their roll (at least one 1)
     *
     * @param author The player that rolled a 1
     * @return The EmbedBuilder that shows the change in plot points for the player
     */
    public EmbedBuilder addPlotPoints(MessageAuthor author) {
        if (rollResult.getDoom() != 0) {
            PPManager manager = new PPManager();
            String userID = author.getIdAsString();
            int oldPP = manager.getPlotPoints(userID);
            int newPP = manager.setPlotPoints(userID, oldPP + 1);
            return new EmbedBuilder()
                    .setAuthor(author)
                    .setDescription(oldPP + " → " + newPP);
        }
        else {
            return null;
        }
    }

    public EmbedBuilder addDoom(int doomVal) {
        DoomWriter doomWriter = new DoomWriter();
        return doomWriter.addDoom(doomVal);
    }

    //Roll all of the dice. Plot dice have a minimum value of its maximum roll/2
    private void rollDice(Random random) {
        //Roll dice
        rollDie(random);
        rollKeptDie(random);
        rollPlotDice(random);
        addFlatBonus();
    }

    private void addFlatBonus() {
        dicePool.getFlatBonuses().forEach(rollResult::addFlatBonus);
    }

    private void rollPlotDice(Random random) {
        //A plot die's minimum value is its number of faces / 2
        for (Integer pdice : dicePool.getPlotDice()) {
            int pValue = random.nextInt(pdice) + 1;
            if (pValue < pdice / 2) {
                pValue = pdice / 2;
            }
            rollResult.addPlotResult(pValue);
        }
    }

    private void rollDie(Random random) {
        dicePool.getRegularDice().stream().mapToInt(die -> random.nextInt(die) + 1).forEach(rollResult::addResult);
    }

    private void rollKeptDie(Random random) {
        dicePool.getKeptDice().stream().mapToInt(keptDie -> random.nextInt(keptDie) + 1).forEach(rollResult::addKeptResult);
    }

    //Replaces brackets in the string. If the string is blank, returns "none" in italics
    private String resultsToString(List<Integer> result) {
        return result.isEmpty() ? NONE : result.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}