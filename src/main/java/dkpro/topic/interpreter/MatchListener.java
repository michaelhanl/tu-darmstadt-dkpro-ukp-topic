package dkpro.topic.interpreter;

import dkpro.topic.interpreter.rules.RuleInstance;

public abstract interface MatchListener {
    public abstract void match(RuleInstance paramRuleInstance, Constituent paramConstituent);
}
