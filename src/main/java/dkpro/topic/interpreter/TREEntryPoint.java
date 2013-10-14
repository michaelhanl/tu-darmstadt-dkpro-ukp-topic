package dkpro.topic.interpreter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import dkpro.topic.annotator.TRAnnotator;
import dkpro.topic.main.Main;
import dkpro.topic.utils.ConfigUtils;
import dkpro.topic.utils.Configuration;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import java.io.File;

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

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        _log.debug("Adding parameters to class");

        DocumentMetaData meta = DocumentMetaData.get(aJCas);
        File targetFile = new File(ConfigUtils.buildTargetDocPath(input,
                meta.getDocumentTitle()));
        File annotated = new File(ConfigUtils.getOutputDir() + "/" + TRAnnotator.outDirEx + "/"
                + targetFile.getName());

        if (!Configuration.isAutoOverEn() && Main.isAEn()
                && annotated.exists()) {
            System.out.println("");
            System.out
                    .println("WARNING: topic annotation target File already exists!");
            System.exit(0);
        }

        try {
            System.out.println("file to interpret " + targetFile);
            _log.info("Running TreeRuleEngine!");
            TopicXMLParserHandler walker = TopicXMLParserHandler.runner(
                    ConfigUtils.getRuleFile(), targetFile);

            if (Main.isAEn())
                TRAnnotator.annotate(targetFile, walker.getSentenceResults());
        } catch (Exception e) {
            _log.error("TreeRuleEngine exit with error!", e);
        }

    }
}
