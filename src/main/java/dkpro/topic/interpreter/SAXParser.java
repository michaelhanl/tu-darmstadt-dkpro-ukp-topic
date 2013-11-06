package dkpro.topic.interpreter;

import dkpro.topic.interpreter.data.XMLConstituent;
import dkpro.topic.utils.NamingParameters;
import dkpro.topic.utils.OutputWriter;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;

/**
 * a wrapper class based on the SAXWalker class, provided by R. Eckart de Castilho
 * @author eckart@ukp.informatik.tu-darmstadt.de, hanl@ids-mannheim.de
 * @date 11/6/13
 */
public class SAXParser extends SAXFilter {
    private final Stack<XMLConstituent> _stack = new Stack();
    private final TopicSentInterpreter _interpreter;
    private String docName;
    private String sentenceID;
    private final boolean render;

    public SAXParser(TopicSentInterpreter i, boolean render) {
        this._interpreter = i;
        this.docName = new String();
        this.sentenceID = new String();
        this.render = render;
    }

    public SAXParser(TopicSentInterpreter i) {
        this(i, false);
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
//        FIXME: docname probably not needed!
        if (localName.equals(NamingParameters.getDocRoot()))
            docName = attributes.getValue(NamingParameters.getAttrDocName());

        if (localName.equals(NamingParameters.getElementSentence()))
            sentenceID = attributes.getValue(NamingParameters.getAttrSentenceID());

        Element e = DocumentHelper.createElement(QName
                .get(localName, uri, name));

        for (int i = 0; i < attributes.getLength(); ++i) {
            QName qname = QName.get(attributes.getQName(i),
                    attributes.getURI(i));
            e.addAttribute(qname, attributes.getValue(i));
        }


        XMLConstituent parent = null;
        if (!(this._stack.isEmpty())) {
            parent = this._stack.peek();
        }
        XMLConstituent constituent = new XMLConstituent(parent, e, sentenceID);

        this._stack.push(constituent);
        this._interpreter.startElement(constituent);

        super.startElement(uri, localName, name, attributes);

    }


    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        XMLConstituent constituent = this._stack.peek();
        this._interpreter.endElement(constituent);
        this._stack.pop();

        if (render)
            OutputWriter.renderXML(constituent.getNode());
        //filter must run after parser, since learner has to resolve the result directly after the parser
        // found the topic type and can return the respective sentence accordingly!
        super.endElement(uri, localName, name);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this._interpreter.chars(getCurrent(), new String(ch, start, length));
    }


    public XMLConstituent getCurrent() {
        if (!this._stack.isEmpty())
            return this._stack.peek();
        else
            return null;
    }

}
