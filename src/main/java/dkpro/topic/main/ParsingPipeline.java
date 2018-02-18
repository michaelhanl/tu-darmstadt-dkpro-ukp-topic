package dkpro.topic.main;

import dkpro.topic.components.UIMAComponents;
import dkpro.topic.utils.ConfigParameters;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses UIMAFit pipeline mechanism to create workflow for XML annotation and
 * topic identification
 *
 * @author micha.hanl@gmail.com
 * @date 11/6/13
 */
public class ParsingPipeline {

    private static Logger _log = LoggerFactory.getLogger(ParsingPipeline.class);
    private CollectionReader collReader;
    private List<AnalysisEngineDescription> analysisEngines;

    private ParsingPipeline(CollectionReader reader) {
        this.collReader = reader;
        this.analysisEngines = new ArrayList<>();
    }

    private void addEngineDescription(AnalysisEngineDescription ... engines) {
        for (AnalysisEngineDescription ae : engines)
            this.analysisEngines.add(ae);
    }



    public static ParsingPipeline runTopicEngineOnly(ConfigParameters c) throws ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline(UIMAComponents.setupReader(UIMAComponents.XML, c));
        p.addEngineDescription(UIMAComponents.setupTreeRuleEngine(c, true));
        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline();
        return p;
    }

    // todo: in case XMI writer is needed: UIMAComponents.setupXMIWriter(). It does not work yet though
    public static ParsingPipeline runStanfordParser(ConfigParameters c) throws ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline(UIMAComponents.setupReader(UIMAComponents.TEXT, c));
        p.addEngineDescription(UIMAComponents.setupSegmenter(), UIMAComponents.setupParser(c),
                UIMAComponents.setupConstituentWriter(c));
        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline();
        return p;
    }

    public static ParsingPipeline runFullInterpreter(ConfigParameters c)
            throws ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline(UIMAComponents.setupReader(UIMAComponents.TEXT, c));
        p.addEngineDescription(UIMAComponents.setupSegmenter(), UIMAComponents.setupParser(c),
                UIMAComponents.setupConstituentWriter(c), UIMAComponents.setupTreeRuleEngine(c, false));
        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline();
        return p;
    }

    public void runPipeline() {
        try {
            _log.info("run Analysis Pipeline");
            SimplePipeline.runPipeline(this.collReader, analysisEngines.toArray(new AnalysisEngineDescription[0]));
        } catch (UIMAException e) {
            _log.error("UIMA Exception", e);
        } catch (IOException e) {
            _log.error("IO Exception", e);
        }
    }


}
