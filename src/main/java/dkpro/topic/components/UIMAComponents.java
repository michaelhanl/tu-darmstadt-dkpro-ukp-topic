package dkpro.topic.components;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import dkpro.topic.interpreter.TreeParser;
import dkpro.topic.utils.ConfigParameters;
import dkpro.topic.writers.ConstituentWriter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

/**
 * @author eckart@ukp.informatik.tu-darmstadt.de, micha.hanl@gmail.com
 * @date 11/6/13
 */
public class UIMAComponents {

    public static final int XML = 1;
    public static final int TEXT = 2;
    private static Logger _log = LoggerFactory.getLogger(UIMAComponents.class);

    /**
     * reads input files from the configured input path!
     * new String[]{"[+]*.txt"}
     */
    public static CollectionReader setupReader(int fileType, ConfigParameters c) throws ResourceInitializationException {
        String[] filePattern = new String[1];
        switch (fileType) {
            case XML:
                filePattern[0] = "[+]*.xml";
                break;
            case TEXT:
                filePattern[0] = "[+]*.txt";
                break;
        }
        _log.debug("initialize FileReader");

        CollectionReader collReader = createReader(TextReader.class,
                TextReader.PARAM_PATH, c.getInputDir(),
                TextReader.PARAM_LANGUAGE, c.getLang(),
                TextReader.PARAM_PATTERNS, filePattern,
                TextReader.PARAM_ENCODING, c.getEncoding());
        return collReader;
    }

    public static AnalysisEngineDescription setupSegmenter() throws ResourceInitializationException {
        /*
         * loads segmentation annotator for the Stanford Tools
		 */

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        return segmenter;
    }

    public static AnalysisEngineDescription setupParser(ConfigParameters c) throws ResourceInitializationException {
        /*
         * loads the Stanford Parser and implements parsing parameters and
		 * parsing tree parameters
		 */
        _log.debug("initialize Parser");
        if (c.getModel() == null || c.getModel().isEmpty())
            throw new ResourceInitializationException("Stanford Parser Initialization",
                    "No Parser model given", null);


        AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class,
                StanfordParser.PARAM_LANGUAGE, c.getLang(),
                StanfordParser.PARAM_VARIANT, c.getModel(),
                StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                StanfordParser.PARAM_WRITE_DEPENDENCY, false,
                StanfordParser.PARAM_MAX_TOKENS, 200);
        return parser;
    }

    public static AnalysisEngineDescription setupXMIWriter() throws ResourceInitializationException {
        _log.debug("initialize XMI Writer");
        AnalysisEngineDescription cxmi = createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, "target/xmi",
                XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml");
        return cxmi;
    }

    public static AnalysisEngineDescription setupConstituentWriter(ConfigParameters c) throws ResourceInitializationException {
        /*
         * loads constituent writer to transform JCas object to XML format,
		 * readable by the TRE
		 */
        _log.debug("initialize Constituent Writer");
        AnalysisEngineDescription constituentXML = createEngineDescription(ConstituentWriter.class,
                ConstituentWriter.PARAM_PATH, c.getOutputDir());
        return constituentXML;
    }

    public static AnalysisEngineDescription setupTreeRuleEngine(ConfigParameters c) throws ResourceInitializationException {
        /*
         * takes XML files as input and produces statistics output in console
		 * for topic identification
		 */
        _log.debug("initialize Tree-Rule-Engine");
        AnalysisEngineDescription treeRuleEngine = createEngineDescription(TreeParser.class,
                TreeParser.PARAM_PATH, c.getOutputDir());
        return treeRuleEngine;
    }

}
