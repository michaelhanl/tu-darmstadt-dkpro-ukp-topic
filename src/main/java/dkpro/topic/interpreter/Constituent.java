package dkpro.topic.interpreter;

import org.dom4j.Element;

public abstract interface Constituent {
    public abstract boolean match(Element paramElement);

    public abstract Constituent getParent();

    public abstract String getExpected();
}
