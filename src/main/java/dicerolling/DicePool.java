package dicerolling;

import java.util.ArrayList;
import java.util.List;

public class DicePool {
    //Dice Types
    private List<Integer> regularDice = new ArrayList<>();
    private List<Integer> plotDice = new ArrayList<>();
    private List<Integer> keptDice = new ArrayList<>();
    private List<Integer> flatBonuses = new ArrayList<>();
    //Configs
    private int keepHowMany = 2;
    private boolean enableEnhancementEmojis = true; //TODO Switch to reading from configs
    private String difficulty = "";
    private int plotPointDiscount = 0;
    private boolean enableOpportunities = true;
    private int minFacets = 0;

    public List<Integer> getRegularDice() {
        return regularDice;
    }

    public List<Integer> getPlotDice() {
        return plotDice;
    }

    public List<Integer> getKeptDice() {
        return keptDice;
    }

    public List<Integer> getFlatBonuses() {
        return flatBonuses;
    }

    public int getKeepHowMany() {
        return keepHowMany;
    }

    public boolean isEnableEnhancementEmojis() {
        return enableEnhancementEmojis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public DicePool setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public int getPlotPointDiscount() {
        return plotPointDiscount;
    }

    public DicePool setPlotPointDiscount(int num) {
        plotPointDiscount = num;
        return this;
    }

    public boolean isEnableOpportunities() {
        return enableOpportunities;
    }

    public DicePool addDice(int dice) {
        regularDice.add(dice);
        return this;
    }

    public DicePool addPlotDice(int dice) {
        plotDice.add(dice);
        return this;
    }

    public DicePool addKeptDice(int dice) {
        keptDice.add(dice);
        return this;
    }

    public DicePool setNumberOfKeptDice(int num) {
        keepHowMany = num;
        return this;
    }

    public DicePool setOpportunities(boolean enable) {
        enableOpportunities = enable;
        return this;
    }

    public DicePool addDice(String diceType, int dice) {
        switch (diceType) {
            case "d":
                regularDice.add(dice);
                break;
            case "pd":
                plotDice.add(dice);
                break;
            case "kd":
                keptDice.add(dice);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + diceType);
        }
        return this;
    }

    public DicePool addFlatBonus(int bonus) {
        flatBonuses.add(bonus);
        return this;
    }

    public DicePool enableEnhancement(boolean enable) {
        enableEnhancementEmojis = enable;
        return this;
    }

    public DicePool setMinFacets(int minFacets) {
        this.minFacets = minFacets;
        return this;
    }
}