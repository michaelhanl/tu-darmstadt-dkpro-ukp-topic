package dkpro.topic.components;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import dkpro.topic.interpreter.TREEntryPoint;
import dkpro.topic.utils.Configuration;
import dkpro.topic.writers.ConstituentWriter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

/**
 * @author eckart@ukp.informatik.tu-darmstadt.de, hanl@ids-mannheim.de
 * @date 11/6/13
 */
public class UIMAComponents {

    public static final int XML = 1;
    public static final int TEXT = 2;
    private static Logger _log = LoggerFactory.getLogger(UIMAComponents.class);

    /*
     * reads input files from the configured input path!
     * new String[]{"[+]*.txt"}
     */
    public static CollectionReader setupReader(int fileType) throws ResourceInitializationException {
        String[] files = new String[1];
        switch (fileType) {
            case XML:
                files[0] = "[+]*.xml";
                break;
            case TEXT:
                files[0] = "[+]*.txt";
                break;
        }
        _log.debug("initialize FileReader");
        CollectionReader collReader = createCollectionReader(TextReader.class,
                TextReader.PARAM_PATH, Configuration.getInputDir(),
                TextReader.PARAM_LANGUAGE, Configuration.getLang(),
                TextReader.PARAM_PATTERNS, files,
                TextReader.PARAM_ENCODING, Configuration.getEncoding());
        return collReader;
    }

    public static AnalysisEngineDescription setupSegmenter() throws ResourceInitializationException {
        /*
         * loads segmentation annotator for the Stanford Tools
		 */
        AnalysisEngineDescription segmenter = createPrimitiveDescription(StanfordSegmenter.class);
        return segmenter;
    }

    public static AnalysisEngineDescription setupParser() throws ResourceInitializationException {
        /*
         * loads the Stanford Parser and implements parsing parameters and
		 * parsing tree parameters
		 */
        _log.debug("initialize Parser");
        if (Configuration.getModel() == null || Configuration.getModel().isEmpty())
            throw new ResourceInitializationException("Stanford Parser Initialization",
                    "No Parser model given", null);

        AnalysisEngineDescription parser = createPrimitiveDescription(StanfordParser.class,
                StanfordParser.PARAM_LANGUAGE, Configuration.getLang(),
                StanfordParser.PARAM_VARIANT, Configuration.getModel(),
                StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
                StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, false,
                StanfordParser.PARAM_MAX_TOKENS, 200);
        return parser;
    }

    public static AnalysisEngineDescription setupXMIWriter() throws ResourceInitializationException {
        _log.debug("initialize XMI Writer");
        AnalysisEngineDescription cxmi = createPrimitiveDescription(XmiWriter.class,
                XmiWriter.PARAM_PATH, "target/xmi",
                XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml");
        return cxmi;
    }

    public static AnalysisEngineDescription setupConstituentWriter() throws ResourceInitializationException {
        /*
         * loads constituent writer to transform JCas object to XML format,
		 * readable by the TRE
		 */
        _log.debug("initialize Constituent Writer");
        AnalysisEngineDescription constituentXML = createPrimitiveDescription(ConstituentWriter.class,
                ConstituentWriter.PARAM_PATH, Configuration.getOutputDir());
        return constituentXML;
    }

    public static AnalysisEngineDescription setupTreeRuleEngine(String fileDir) throws ResourceInitializationException {
        /*
         * takes XML files as input and produces statistics output in console
		 * for topic identification
		 */
        _log.debug("initialize Tree-Rule-Engine");
        AnalysisEngineDescription treeRuleEngine = createPrimitiveDescription(TREEntryPoint.class,
                TREEntryPoint.PARAM_PATH, fileDir);
        return treeRuleEngine;
    }

}
