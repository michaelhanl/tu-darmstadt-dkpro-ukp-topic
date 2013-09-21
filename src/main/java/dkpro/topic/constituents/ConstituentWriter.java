package dkpro.topic.constituents;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import dkpro.topic.utils.ConfigUtils;
import dkpro.topic.utils.Configuration;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.openfuxml.addon.wiki.FormattingXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class ConstituentWriter extends JCasConsumer_ImplBase {

    /**
     * @author hanl
     */

    Logger _log = LoggerFactory.getLogger(ConstituentWriter.class);
    public final static String PARAM_PATH = "outputPath";

    @ConfigurationParameter(name = PARAM_PATH, mandatory = true)
    private String outputPath;

    /**
     * process document as uima analysis engine and write stand-off-annotation XML file, based on relax NG XML schema.
     *
     * @param aJCas
     * @throws AnalysisEngineProcessException
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        XMLStreamWriter writer;
        File output = new File(outputPath);
        output.mkdirs();
        DocumentMetaData meta = DocumentMetaData.get(aJCas);
        String title = ConfigUtils.getTitle(meta.getDocumentTitle());
        File file = new File(outputPath, title + ".xml");
        if (!Configuration.isAutoOverEn() && file.exists()) {
            System.out.println("");
            System.out
                    .println("WARNING: target file to write XML parse tree to already exists!");
            System.exit(0);
        }

        try {
            _log.debug("Writing Document Root: {}", title);
            writer = getFormattedWriter(file);
            writer.writeStartDocument();
            writer.writeStartElement(Configuration.getDocRoot());
            writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation",
                    Configuration.getSchemaPath());
            writer.writeAttribute(Configuration.getAttrDocName(), title);
            ConfigUtils.resetCiteID();
            ConfigUtils.resetSentID();

            /*
             * process segments of sentence
             */
            for (ROOT r : JCasUtil.select(aJCas, ROOT.class)) {
                processSegments(writer, r);
            }

            writer.writeEndDocument();
            writer.flush();
            writer.close();

        } catch (FileNotFoundException e) {
            _log.error("File not found!", e.getMessage(), e);
        } catch (XMLStreamException e) {
            _log.error("Could not write XML file", e.getMessage(), e);
        }

    }


    /**
     * process all segments of clause
     *
     * @param xmlstream
     * @param ann       annotation of type constituent or token
     * @throws AnalysisEngineProcessException
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    private void processSegments(XMLStreamWriter xmlstream, Annotation ann)
            throws AnalysisEngineProcessException {
        try {
            if (ann instanceof Token) {
                processPOS(xmlstream, (Token) ann);
            }

            if (ann instanceof Constituent) {
                Constituent c = (Constituent) ann;
                processConstituents(xmlstream, c);

                for (FeatureStructure child : c.getChildren().toArray())
                    processSegments(xmlstream, (Annotation) child);

                if (!c.getConstituentType().equals("ROOT"))
                    xmlstream.writeEndElement();
            }
        } catch (XMLStreamException e) {
            _log.error("Could not write XML file", e.getMessage(), e);

        }

    }


    /**
     * process phrase constituents and phrase types
     *
     * @param xmlstream Outputstream
     * @param c         Constituent to process
     * @throws AnalysisEngineProcessException
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    private void processConstituents(XMLStreamWriter xmlstream, Constituent c)
            throws AnalysisEngineProcessException {
        _log.debug("Processing constituent: " + c.getCoveredText());
        if (!c.getConstituentType().equals("ROOT")) {
            /*
             * sentence parameter receives sentence id
             */
            try {
                if (c.getParent().getType().getShortName().equals("ROOT")) {
                    xmlstream.writeStartElement(Configuration
                            .getElementSentence());

                    _log.debug("Writing sentence ID " + ConfigUtils.getSentID() +
                            ": " + c.getCoveredText());

                    xmlstream.writeAttribute(Configuration.getAttrSentenceID(),
                            ConfigUtils.getSentID());
                    ConfigUtils.incrementSentID();

                } else
                    xmlstream.writeStartElement(Configuration
                            .getElementConstituent());

                xmlstream.writeAttribute(Configuration.getAttrConstType(),
                        c.getConstituentType());

            } catch (XMLStreamException e) {
                _log.error("Could not write XML file", e.getMessage(), e);
            }
        }

    }


    /**
     * process tokens and POS-tags
     *
     * @param xmlstream Outputstream
     * @param t         Token to process
     * @throws AnalysisEngineProcessException
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    private void processPOS(XMLStreamWriter xmlstream, Token t)
            throws AnalysisEngineProcessException {
        _log.debug("Processing token: " + t.getCoveredText());

        String pos = null;
        String lemma = null; // lemmas are only available for English!

        if (t.getPos().getPosValue() != null)
            pos = t.getPos().getPosValue();
        if (t.getLemma() != null)
            lemma = t.getLemma().getValue();

        try {
            xmlstream.writeStartElement(Configuration.getElementConstituent());
            xmlstream.writeAttribute(Configuration.getAttrConstType(), pos);

            if (lemma != null)
                xmlstream.writeAttribute(Configuration.getAttrLemma(), lemma);

            /*
             * uses the notation for citation marks of the stanford parser to
             * identify citations; annotates citations with an id attribute
             */
            if (pos.equals("$*LRB*") &&
                    t.getCoveredText().trim().charAt(0) == '"') {
                _log.debug("Writing ID for citation: " + ConfigUtils.getCiteID());
                xmlstream.writeAttribute(Configuration.getAttrCitationID(),
                        ConfigUtils.getCiteID());
                ConfigUtils.incrementCiteID();
            }

            xmlstream.writeCharacters(t.getCoveredText());
            xmlstream.writeEndElement();
        } catch (XMLStreamException e) {
            _log.error("Could not write XML file", e.getMessage(), e);
            e.printStackTrace();
        }
    }


    private XMLStreamWriter getFormattedWriter(File outputFile)
            throws FileNotFoundException, XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter out;
        out = xof.createXMLStreamWriter(new FileOutputStream(outputFile),
                "UTF-8");
        return new FormattingXMLStreamWriter(out);
    }

}
