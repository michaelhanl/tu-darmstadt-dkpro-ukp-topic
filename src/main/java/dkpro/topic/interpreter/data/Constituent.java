package dkpro.topic.interpreter.data;

import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author eckart@ukp.informatik.tu-darmstadt.de, micha.hanl@gmail.com
 * @date 11/6/13
 */
public interface Constituent {
    boolean match(Element paramElement);
    Constituent getParent();
    // String identifier for rule, as given in the gold standard data set
    String getNodeExpectation();
    Node getNode();
}
