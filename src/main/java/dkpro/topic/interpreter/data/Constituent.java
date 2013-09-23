package dkpro.topic.interpreter.data;

import org.dom4j.Element;

public abstract interface Constituent {
    public abstract boolean match(Element paramElement);

    public abstract Constituent getParent();

    // String identifier for rule, as given in the gold standard data set
    public abstract String getNodeExpectation();
}
