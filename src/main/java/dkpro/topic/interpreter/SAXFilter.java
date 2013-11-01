package dkpro.topic.interpreter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class SAXFilter extends DefaultHandler
        implements LexicalHandler, DeclHandler {
    private static Log _log = LogFactory.getLog(SAXFilter.class);

    public static Attributes NO_ATTRIBUTES = new AttributesImpl();
    protected ContentHandler _target;
    private boolean debug = false;

    public ContentHandler filter(ContentHandler filtered) {
        this._target = filtered;
        return this;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (this.debug)
            _log.debug("chars");

        if (this._target != null)
            this._target.characters(ch, start, length);
    }

    public void endDocument()
            throws SAXException {
        if (this.debug) {
            _log.debug("endDocument");
        }

        if (this._target != null)
            this._target.endDocument();
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (this.debug) {
            _log.debug("endElement");
        }

        if (this._target != null)
            this._target.endElement(uri, localName, qName);
    }

    public void endPrefixMapping(String prefix)
            throws SAXException {
        if (this.debug) {
            _log.debug("endPrefixMapping");
        }

        if (this._target != null)
            this._target.endPrefixMapping(prefix);
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (this.debug) {
            _log.debug("ignorableWhitespace");
        }

        if (this._target != null)
            this._target.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        if (this.debug) {
            _log.debug("processingInstruction");
        }

        if (this._target != null)
            this._target.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        if (this.debug) {
            _log.debug("setDocumentLocator");
        }

        if (this._target != null)
            this._target.setDocumentLocator(locator);
    }

    public void skippedEntity(String name)
            throws SAXException {
        if (this.debug) {
            _log.debug("skippedEntity");
        }

        if (this._target != null)
            this._target.skippedEntity(name);
    }

    public void startDocument()
            throws SAXException {
        if (this.debug) {
            _log.debug("startDocument");
        }

        if (this._target != null)
            this._target.startDocument();
    }

    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        if (this.debug) {
            _log.debug("startElement");
        }

        if (this._target != null)
            this._target.startElement(uri, localName, qName, atts);
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (this.debug) {
            _log.debug("startPrefixMapping");
        }

        if (this._target != null)
            this._target.startPrefixMapping(prefix, uri);
    }

    public void error(SAXParseException arg0)
            throws SAXException {
        if (this.debug) {
            _log.debug("error");
        }

        if ((this._target instanceof ErrorHandler))
            ((DefaultHandler) this._target).error(arg0);
    }

    public void fatalError(SAXParseException arg0)
            throws SAXException {
        if (this.debug) {
            _log.debug("fatalError");
        }

        if ((this._target instanceof ErrorHandler))
            ((DefaultHandler) this._target).fatalError(arg0);
    }

    public void notationDecl(String arg0, String arg1, String arg2)
            throws SAXException {
        if (this.debug) {
            _log.debug("notationDecl");
        }

        if ((this._target instanceof DTDHandler))
            ((DefaultHandler) this._target).notationDecl(arg0, arg1, arg2);
    }

    public InputSource resolveEntity(String arg0, String arg1)
            throws IOException, SAXException {
        if (this.debug) {
            _log.debug("resolveEntity");
        }

        if ((this._target instanceof EntityResolver)) {
            return ((DefaultHandler) this._target).resolveEntity(arg0, arg1);
        }
        throw new SAXException("Not supported by filter target");
    }

    public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3)
            throws SAXException {
        if (this.debug) {
            _log.debug("unparsedEntityDecl");
        }

        if ((this._target instanceof DTDHandler))
            ((DefaultHandler) this._target).unparsedEntityDecl(arg0, arg1, arg2, arg3);
    }

    public void warning(SAXParseException arg0)
            throws SAXException {
        if (this.debug) {
            _log.debug("warning");
        }

        if ((this._target instanceof ErrorHandler))
            ((DefaultHandler) this._target).warning(arg0);
    }

    public void comment(char[] ch, int start, int length)
            throws SAXException {
        if (this.debug) {
            _log.debug("comment");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).comment(ch, start, length);
    }

    public void endCDATA()
            throws SAXException {
        if (this.debug) {
            _log.debug("endCDATA");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).endCDATA();
    }

    public void endDTD()
            throws SAXException {
        if (this.debug) {
            _log.debug("endDTD");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).endDTD();
    }

    public void endEntity(String name)
            throws SAXException {
        if (this.debug) {
            _log.debug("endEntity");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).endEntity(name);
    }

    public void startCDATA()
            throws SAXException {
        if (this.debug) {
            _log.debug("startCDATA");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).startCDATA();
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        if (this.debug) {
            _log.debug("startDTD");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).startDTD(name, publicId, systemId);
    }

    public void startEntity(String name)
            throws SAXException {
        if (this.debug) {
            _log.debug("startEntity");
        }

        if ((this._target instanceof DeclHandler))
            ((LexicalHandler) this._target).startEntity(name);
    }

    public void attributeDecl(String name, String name2, String type, String mode, String value)
            throws SAXException {
        if (this.debug) {
            _log.debug("attributeDecl");
        }

        if ((this._target instanceof DeclHandler))
            ((DeclHandler) this._target).attributeDecl(name, name2, type, mode, value);
    }

    public void elementDecl(String name, String model)
            throws SAXException {
        if (this.debug) {
            _log.debug("elementDecl");
        }

        if ((this._target instanceof DeclHandler))
            ((DeclHandler) this._target).elementDecl(name, model);
    }

    public void externalEntityDecl(String name, String publicId, String systemId)
            throws SAXException {
        if (this.debug) {
            _log.debug("externalEntityDecl");
        }

        if ((this._target instanceof DeclHandler))
            ((DeclHandler) this._target).externalEntityDecl(name, publicId, systemId);
    }

    public void internalEntityDecl(String name, String value)
            throws SAXException {
        if (this.debug) {
            _log.debug("internalEntityDecl");
        }

        if ((this._target instanceof DeclHandler))
            ((DeclHandler) this._target).internalEntityDecl(name, value);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public ContentHandler getTarget() {
        return this._target;
    }
}
