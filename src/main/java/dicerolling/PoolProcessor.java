package dicerolling;

import org.javacord.api.entity.message.MessageAuthor;
import sheets.SheetsManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class PoolProcessor {
    private final String command;
    private final MessageAuthor author;
    private final String[] options = new String[]{"-fsu", "-fsd", "-mf", "-here", "--keep", "-k", "-discount", "-opportunities"};
    private final DicePool dicePool = new DicePool();

    public PoolProcessor(String command, MessageAuthor author) {
        this.command = command;
        this.author = author;
        preprocess();
    }

    public DicePool getDicePool() {
        return dicePool;
    }

    private void preprocess() {
        String trimmedCommand = command.replaceAll("\\s+", " ");
        String[] paramArray = trimmedCommand.split(" ");
        int nextDiceFacetMod = 0;
        int maxFacets = 12;
        String nextDiceType = "d";
        for (String param : paramArray) {
            if (param.startsWith("-fsu=")) {
                nextDiceFacetMod = Integer.parseInt(param.substring(5));
            }
            else if (param.startsWith("-fsd=")) {
                nextDiceFacetMod = Integer.parseInt(param.substring(5)) * -1;
            }
            else if (param.startsWith("-mf=")) {
                maxFacets = Integer.parseInt(param.substring(4));
            }
            else if (param.startsWith("-difficulty")) {
                processDifficultyLevel(param);
            }
            else if (param.startsWith("-k=")) {
                dicePool.setNumberOfKeptDice(Integer.parseInt(param.substring(3)));
            }
            else if (param.startsWith("-pd")) {
                processPlotPointDiscount(param);
            }
            else if (param.startsWith("-enh=")) {
                dicePool.enableEnhancement(Boolean.parseBoolean(param.substring(9)));
            }
            else if (param.startsWith("-opp=")) {
                dicePool.setOpportunities(Boolean.parseBoolean(param.substring(5)));
            }
            //Skill
            else if (param.chars().allMatch(Character::isLetter)) {
                addSkillFromSpreadsheetToPool(nextDiceFacetMod, maxFacets, nextDiceType, param);
                //Reset default dice settings
                nextDiceFacetMod = 0;
                maxFacets = 12;
                nextDiceType = "d";
            }
            //Any type of dice
            else if (param.matches("\\d*([kp])?d\\d+")) {
                addDiceToPool(nextDiceFacetMod, param);
                //Reset facet modifier
                nextDiceFacetMod = 0;
            }
            //Flat bonus or penalty
            else if (param.matches("([-+])\\d+")) {
                dicePool.addFlatBonus(Integer.parseInt(param.substring(1)));
            }
        }
    }

    private void processDifficultyLevel(String param) {
        dicePool.setDifficulty(param.startsWith("-difficulty=") ? param.substring(12) : "default");
    }

    private void processPlotPointDiscount(String param) {
        if (param.startsWith("-pd="))
            dicePool.setPlotPointDiscount(Integer.parseInt(param.substring(4)));
        else
            dicePool.setPlotPointDiscount(Integer.MAX_VALUE);
    }

    /**
     * Add a collection of dice to a pool. e.g. 3d12, d4, pd6, kd9
     *
     * @param nextDiceFacetMod The number of facets to modify the dice by
     * @param param            The parameter with a valid dice
     */
    private void addDiceToPool(int nextDiceFacetMod, String param) {
        StringBuilder numberOfDice = new StringBuilder();

        //Find number of dice being rolled
        int i = 0;
        while (Character.isDigit(param.charAt(i))) {
            numberOfDice.append(param.charAt(i));
            i++;
        }

        int numberOfDiceInt = numberOfDice.toString().equals("") ? 1 : Integer.parseInt(numberOfDice.toString());
        StringBuilder diceType = new StringBuilder();
        while (Character.isAlphabetic(param.charAt(i))) {
            diceType.append(param.charAt(i));
            i++;
        }

        int diceFacets = Integer.parseInt(param.substring(i));

        if (nextDiceFacetMod != 0) {
            int totalFacets = numberOfDiceInt * diceFacets + nextDiceFacetMod;
            if (totalFacets > diceFacets) {
                for (int j = 0; j < totalFacets / diceFacets; j++) {
                    dicePool.addDice(diceType.toString(), diceFacets);
                }
                if (totalFacets % diceFacets > 2) {
                    dicePool.addDice(diceType.toString(), totalFacets % diceFacets);
                }
            }
            else if (totalFacets > 2) {
                dicePool.addDice(diceType.toString(), totalFacets);
            }
        }
        else {
            for (int j = 0; j < numberOfDiceInt; j++) {
                dicePool.addDice(diceType.toString(), diceFacets);
            }
        }
    }

    /**
     * Adds a skill from a character spreadsheet to the dice pool
     *
     * @param nextDiceFacetMod The amount of facets to modify the next dice by
     * @param maxFacets        The max facets you are rolling for the dice
     * @param nextDiceType     The type of dice (normal, plot, kept) to add to the pool
     * @param param            The name of the skill to fetch
     */
    private void addSkillFromSpreadsheetToPool(int nextDiceFacetMod, int maxFacets, String nextDiceType, String param) {
        int skillFacets = retrieveSkill(param, author.getIdAsString());
        skillFacets += nextDiceFacetMod;
        if (skillFacets > 0) {
            if (skillFacets > maxFacets) {
                for (int i = 0; i < skillFacets / maxFacets; i++) {
                    dicePool.addDice(nextDiceType, maxFacets);
                }
                if (skillFacets % maxFacets > 2) {
                    dicePool.addDice(nextDiceType, skillFacets % maxFacets);
                }
            }
            else {
                dicePool.addDice(nextDiceType, skillFacets);
            }
        }
    }

    /**
     * Retrieves the facet value of the specified skill in a spreadsheet
     *
     * @param skill The skill to search
     * @param id    The id of the message sender as a string
     * @return The facet value of the string, or -1 if the skill was not found
     */
    private int retrieveSkill(String skill, String id) {
        try {
            SheetsManager characterInfo = new SheetsManager(id);
            List<List<Object>> values = characterInfo.getResult().getValues();
            final Object[] skillArray = values.stream().filter(objects -> objects.size() == 2 && ((String) objects.get(1)).matches("\\d+")).toArray();
            HashMap<String, Integer> skillMap = new HashMap<>();
            for (Object o : skillArray) {
                List<String> pair = (List<String>) o;
                skillMap.put(pair.get(0).replace(" ", "").toLowerCase(), Integer.parseInt(pair.get(1)));
            }
            return skillMap.getOrDefault(skill, -1);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
