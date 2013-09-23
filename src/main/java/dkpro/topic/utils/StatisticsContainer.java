package dkpro.topic.utils;

import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleDefinition;
import dkpro.topic.interpreter.rules.RuleInstance;

import java.util.HashMap;
import java.util.Map;

public class StatisticsContainer {
    protected final Map<RuleDefinition, Stats> _stats;
    protected int _sentences;
    protected int _totalExpected;
    protected int _totalMatches;
    protected int _totalMet;
    protected int _totalMismatches;
    protected int _totalUnexpectedMatches;


    public StatisticsContainer() {
        this._sentences = 0;
        this._totalExpected = 0;
        this._totalMatches = 0;
        this._totalMet = 0;
        this._totalMismatches = 0;
        this._totalUnexpectedMatches = 0;

        this._stats = new HashMap();
    }

    public void tallySentence() {
        this._sentences += 1;
    }

    public void tallyExpectation(RuleDefinition expected) {
        getStats(expected).expected();
    }

    public void tallyMatch(RuleInstance rule, Result.Expectation expectation) {
        this._totalMatches += 1;
        getStats(rule.getDefinition()).tally(expectation);
    }

    public void tallyUnfulfilledExpectation(RuleDefinition expected) {
        getStats(expected).unmet();
    }

    private Stats getStats(RuleDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Argument [definition] can not be null");
        }

        Stats s = this._stats.get(definition);
        if (s == null) {
            s = new Stats(definition);
            this._stats.put(definition, s);
        }
        return s;
    }

    public double getPrecision(boolean includeUnexpected) {
//        int v = this._totalMet + this._totalMismatches;
//        if ((v + this._totalUnexpectedMatches) == 0)
//            return 0;
        if (includeUnexpected)
            return this._totalMet / (this._totalMet + this._totalMismatches + this._totalUnexpectedMatches);
//        if (v == 0)
//            return 0;
        return this._totalMet / Float.valueOf(this._totalMet + this._totalMismatches);
    }

    public double getRecall() {
//        if (this._totalExpected == 0)
//            return 0;
        return this._totalMet / Float.valueOf(this._totalExpected);
    }

    public double getFScore(boolean includeUnexpected, float alpha) {
        float precision = new Float(getPrecision(includeUnexpected));
        float recall = new Float(getRecall());

        return (1.0D + alpha) * (precision * recall) / (alpha * precision + recall);
    }


    private class Stats {
        private final RuleDefinition _definition;
        private int _expected = 0;
        private int _unmet = 0;
        private int _met = 0;
        private int _mismatch = 0;
        private int _unexpectedMatch = 0;

        Stats(RuleDefinition definition) {
            this._definition = definition;
        }

        private void tally(Result.Expectation expectation) {
            switch (expectation) {
                case MET:
                    this._met += 1;
                    StatisticsContainer.this._totalMet += 1;
                    break;
                case MISMATCH:
                    this._mismatch += 1;
                    StatisticsContainer.this._totalMismatches += 1;
                    break;
                case UNEXPECTED:
                    this._unexpectedMatch += 1;
                    StatisticsContainer.this._totalUnexpectedMatches += 1;
            }
        }

        private void expected() {
            StatisticsContainer.this._totalExpected += 1;
            this._expected += 1;
        }

        private void unmet() {
            this._unmet += 1;
        }

        private double getPrecision(boolean includeUnexpected) {
            if (includeUnexpected)
                return this._met / Float.valueOf(this._met + this._mismatch + this._unexpectedMatch);
            return this._met / Float.valueOf(this._met + this._mismatch);
        }

        private double getRecall() {
            return this._met / Float.valueOf(this._expected);
        }

        private double getFScore(boolean includeUnexpected, float alpha) {
            float precision = new Float(getPrecision(includeUnexpected));
            float recall = new Float(getRecall());
            return (1.0D + alpha) * (precision * recall) / Float.valueOf(alpha * precision + recall);
        }

        public String toString() {
            return String.format("[%-8s|%-30s|%4d|%4d|%4d|%4d|%4d|%6.2f%%|%6.2f%%|%6.2f%%|%7.4f|%7.4f]",
                    new Object[]{this._definition.getName(), this._definition.getTopicType(), Integer.valueOf(this._expected),
                            Integer.valueOf(this._met), Integer.valueOf(this._unmet), Integer.valueOf(this._mismatch),
                            Integer.valueOf(this._unexpectedMatch), Double.valueOf(getRecall() * 100.0D),
                            Double.valueOf(getPrecision(true) * 100.0D), Double.valueOf(getPrecision(false) * 100.0D),
                            Double.valueOf(getFScore(true, 1.0F)), Double.valueOf(getFScore(false, 1.0F))});
        }
    }
}