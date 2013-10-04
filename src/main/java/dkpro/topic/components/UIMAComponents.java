package dkpro.topic.components;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import dkpro.topic.interpreter.TREEntryPoint;
import dkpro.topic.utils.ConfigUtils;
import dkpro.topic.writers.ConstituentWriter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

/**
 * User: hanl
 * Date: 9/29/13
 * Time: 11:06 AM
 */
public class UIMAComponents {

    private static Logger _log = LoggerFactory.getLogger(UIMAComponents.class);
    public static final int XML = 1;
    public static final int TEXT = 2;

    public static CollectionReader setupReader(int fileType) throws ResourceInitializationException {
        String[] files = new String[1];
        switch(fileType) {
            case XML:
                files[0] = "[+]*.xml";
                break;
            case TEXT:
                files[0] = "[+]*.txt";
                break;
        }

        /*
         * reads input files from Param_Path default: "src/test/resources"
         * new String[]{"[+]*.txt"}
		 */
        _log.debug("initialize FileReader");
        CollectionReader collReader = createCollectionReader(TextReader.class,
                TextReader.PARAM_PATH, ConfigUtils.getInputDir(),
                TextReader.PARAM_LANGUAGE, ConfigUtils.getLang(),
                TextReader.PARAM_PATTERNS, files);
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
        // StanfordParser.PARAM_MAX_ITEMS, 5000000
        AnalysisEngineDescription parser = createPrimitiveDescription(StanfordParser.class,
                StanfordParser.PARAM_LANGUAGE, ConfigUtils.getLang(),
                StanfordParser.PARAM_VARIANT, ConfigUtils.getModal(),
                StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
                StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, false);
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
                ConstituentWriter.PARAM_PATH, ConfigUtils.getOutputDir());
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
