package dkpro.topic.interpreter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import dkpro.topic.annotator.TreeAnnotator;
import dkpro.topic.main.Main;
import dkpro.topic.utils.ConfigUtils;
import dkpro.topic.utils.Configuration;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class TREEntryPoint extends JCasConsumer_ImplBase {

    /**
     * This class instantiates the TreeRuleEngine and processes XML-files
     * provided by the JCas-module with the following annotation layers:
     * Syntactic Parsing, Part-of-Speech, Tokens
     *
     * @author hanl
     */

    Logger _log = LoggerFactory.getLogger(TREEntryPoint.class);

    public final static String PARAM_PATH = "input";
    @ConfigurationParameter(name = PARAM_PATH, mandatory = true)
    private String input;
    TopicXMLParserHandler parser;
    TreeAnnotator annotator;

    public TREEntryPoint() {
        try {
            parser = TopicXMLParserHandler.instantiate();
        } catch (ParserConfigurationException e) {
            _log.error("Parsing Configuration Error", e);
        } catch (SAXException e) {
            _log.error("SAX Parser Error", e);
        } catch (IOException e) {
            _log.error("Input/Output Error", e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        _log.debug("Adding parameters to class");

        DocumentMetaData meta = DocumentMetaData.get(aJCas);
        File targetFile = new File(ConfigUtils.buildTargetDocPath(input,
                meta.getDocumentTitle()));
        File annotated = new File(ConfigUtils.getOutputDir() + "/" + TreeAnnotator.outDirEx + "/"
                + targetFile.getName());

        if (!Configuration.isAutoOverEn() && Main.isAEn()
                && annotated.exists()) {
            System.out.println();
            System.out
                    .println("WARNING: topic annotation target File already exists!");
            System.exit(0);
        }
        try {
            _log.info("Running TreeRuleParser on {}!", targetFile);
            parser.process(ConfigUtils.getRuleFile(), targetFile);
        } catch (Exception e) {
            _log.error("TreeRuleParser exits with error!", e);
        }

        if (Main.isAEn()) {
            annotator = TreeAnnotator.instantiate
                    (targetFile, parser.getSentenceResults());
            annotator.process();
        }
    }
}
