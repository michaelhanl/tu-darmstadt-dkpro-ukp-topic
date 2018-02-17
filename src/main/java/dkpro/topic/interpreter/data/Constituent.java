package dkpro.topic.interpreter.data;

import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author eckart@ukp.informatik.tu-darmstadt.de, micha.hanl@gmail.com
 * @date 11/6/13
 */
public abstract interface Constituent {
    public abstract boolean match(Element paramElement);

    public abstract Constituent getParent();

    // String identifier for rule, as given in the gold standard data set
    public abstract String getNodeExpectation();
    public abstract Node getNode();
}
