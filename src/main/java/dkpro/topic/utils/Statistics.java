package dkpro.topic.utils;

import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleDefinition;
import dkpro.topic.interpreter.rules.RuleInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.*;

public class Statistics {
    private final Map<RuleDefinition, Stats> _stats;
    protected int _sentences;
    protected int _totalExpected;
    protected int _totalMatches;
    protected int _totalMet;
    protected int _totalMismatches;
    protected int _totalUnexpectedMatches;
    Logger log = LoggerFactory.getLogger(Statistics.class);

    public Statistics() {
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

        Stats s = (Stats) this._stats.get(definition);
        if (s == null) {
            s = new Stats(definition);
            this._stats.put(definition, s);
        }
        return s;
    }

    public double getPrecision(boolean includeUnexpected) {
        if (includeUnexpected) {
            return this._totalMet / (this._totalMet + this._totalMismatches + this._totalUnexpectedMatches);
        }
        return this._totalMet / (this._totalMet + this._totalMismatches);
    }

    public double getRecall() {
        return this._totalMet / this._totalExpected;
    }

    public double getFScore(boolean includeUnexpected, double alpha) {
        double precision = getPrecision(includeUnexpected);
        double recall = getRecall();

        return (1.0D + alpha) * (precision * recall) / (alpha * precision + recall);
    }

    public void dumpStats(PrintStream os) {
        os.format("====== STATISTICS ======%n", new Object[0]);
        os.format("%n===== Overall statistics =====%n", new Object[0]);
        os.format("| Sentences found                        | %7d |%n", new Object[]{Integer.valueOf(this._sentences)});
        os.format("| Avg matches per sentence               | %7.2f |%n", new Object[]{Double.valueOf(this._totalMatches / this._sentences)});
        os.format("| Total matches                          | %7d |%n", new Object[]{Integer.valueOf(this._totalMatches)});
        os.format("| Total matches MET             (true +) | %7d |%n", new Object[]{Integer.valueOf(this._totalMet)});
        os.format("| Total matches UNEXPECTED     (false +) | %7d |%n", new Object[]{Integer.valueOf(this._totalUnexpectedMatches)});
        os.format("| Total matches MISMATCHed     (false +) | %7d |%n", new Object[]{Integer.valueOf(this._totalMismatches)});
        os.format("| Overall recall                         | %6.2f%% |%n", new Object[]{Double.valueOf(getRecall() * 100.0D)});
        os.format("| Overall precision    (with unexpected) | %6.2f%% |%n", new Object[]{Double.valueOf(getPrecision(true) * 100.0D)});
        os.format("| Overall precision (without unexpected) | %6.2f%% |%n", new Object[]{Double.valueOf(getPrecision(false) * 100.0D)});
        os.format("| Overall f-score      (with unexpected) | %7.4f |%n", new Object[]{Double.valueOf(getFScore(true, 1.0D))});
        os.format("| Overall f-score   (without unexpected) | %7.4f |%n", new Object[]{Double.valueOf(getFScore(false, 1.0D))});

        os.format("%n===== Detail statistics =====%n", new Object[]{Integer.valueOf(this._sentences)});
        os.format(" %-8s %-30s %-4s %-4s %-4s %-4s %-4s %-7s %-7s %-7s %-7s %-7s%n", new Object[]{"RuleInstance", "Label", "Exp", "Met", "UMet", "MisM", "UExp", "Rec", "Prec+", "Prec-", "FSco+", "FSco-"});

        List<RuleDefinition> rdefs = new ArrayList(this._stats.keySet());
        Collections.sort(rdefs, new Comparator<RuleDefinition>() {
            public int compare(RuleDefinition o1, RuleDefinition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (RuleDefinition def : rdefs)
            os.println(this._stats.get(def));
    }

    public class Stats {
        private final RuleDefinition _definition;
        private int _expected = 0;
        private int _unmet = 0;
        private int _met = 0;
        private int _mismatch = 0;
        private int _unexpectedMatch = 0;

        Stats(RuleDefinition definition) {
            this._definition = definition;
        }

        void tally(Result.Expectation expectation) {
            switch (expectation) {
                case MET:
                    this._met += 1;
                    Statistics.this._totalMet += 1;
                    break;
                case MISMATCH:
                    this._mismatch += 1;
                    Statistics.this._totalMismatches += 1;
                    break;
                case UNEXPECTED:
                    this._unexpectedMatch += 1;
                    Statistics.this._totalUnexpectedMatches += 1;
            }
        }

        void expected() {
            Statistics.this._totalExpected += 1;
            this._expected += 1;
        }

        void unmet() {
            this._unmet += 1;
        }

        public double getPrecision(boolean includeUnexpected) {
            if (includeUnexpected) {
                return this._met / (this._met + this._mismatch + this._unexpectedMatch);
            }
            return this._met / (this._met + this._mismatch);
        }

        public double getRecall() {
            return this._met / this._expected;
        }

        public double getFScore(boolean includeUnexpected, double alpha) {
            double precision = getPrecision(includeUnexpected);
            double recall = getRecall();

            return (1.0D + alpha) * (precision * recall) / (alpha * precision + recall);
        }

        public String toString() {
            return String.format("[%-8s|%-30s|%4d|%4d|%4d|%4d|%4d|%6.2f%%|%6.2f%%|%6.2f%%|%7.4f|%7.4f]", new Object[]{this._definition.getName(), this._definition.getTopicType(), Integer.valueOf(this._expected), Integer.valueOf(this._met), Integer.valueOf(this._unmet), Integer.valueOf(this._mismatch), Integer.valueOf(this._unexpectedMatch), Double.valueOf(getRecall() * 100.0D), Double.valueOf(getPrecision(true) * 100.0D), Double.valueOf(getPrecision(false) * 100.0D), Double.valueOf(getFScore(true, 1.0D)), Double.valueOf(getFScore(false, 1.0D))});
        }
    }
}