package dkpro.topic.main;

import dkpro.topic.interpreter.SAXParser;
import dkpro.topic.interpreter.TopicSentInterpreter;
import dkpro.topic.interpreter.rules.RuleBook;
import dkpro.topic.interpreter.rules.RuleInstance;
import dkpro.topic.utils.OutputWriter;
import dkpro.topic.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.XMLWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * this class was targeted as a wrapper class for the original learner class.
 *
 * FIXME: buggy for multiple sentences in one document.
 * Plus only recognizes the first sentence, but only at ROOT end Node
 */
public class Learner implements ElementHandler {
    private static final Log _log = LogFactory.getLog(Learner.class);
    private static final int CHOICE_NONE = -1;
    private static final String CHOICE_NONE_KEY = "n";
    private static final int CHOICE_SKIP_ALL = -2;
    private static final String CHOICE_SKIP_ALL_KEY = "w";
    private static final int CHOICE_SKIP = -3;
    private static final String CHOICE_SKIP_KEY = "s";
    private final String[] _args;
    private SAXContentHandler _sink;
    private TopicSentInterpreter _tri;
    private SAXParser _sw;
    private boolean _skipAll = false;

    private Learner(String[] args) {
        this._args = args;
    }

    @Override
    public void onStart(ElementPath elementPath) {
    }

    private static void renderChoices(List<RuleInstance> rulesMatched) {
        System.out.format("Choose which rule is the right one:%n",
                new Object[0]);
        for (int i = 0; i < rulesMatched.size(); ++i) {
            RuleInstance r = rulesMatched.get(i);
            System.out.format(
                    "[%d] - %s [%s]%n",
                    new Object[]{Integer.valueOf(i),
                            r.getDefinition().getTopicType(),
                            XMLUtils.collapseWhitespace(r.getTextMatch())});
        }

        System.out.format("[%s] - %s%n", new Object[]{"n",
                "no rule should ever match here"});
        System.out.format("[%s] - %s%n", new Object[]{"s",
                "skip this question"});
        System.out.format("[%s] - %s%n", new Object[]{"w",
                "skip all further questions and write output"});
    }

    private static int getChoice(int num) {
        int choice = -1;
        try {
            while (true) {
                System.out.format("Type choice and press <enter>: ",
                        new Object[0]);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        System.in));
                String raw = br.readLine();
                try {
                    int n = Integer.parseInt(raw);
                    if ((n >= 0) && (n < num)) {
                        choice = n;
                        break;
                    }
                } catch (NumberFormatException e) {
                    if (raw.equals(CHOICE_NONE_KEY)) {
                        choice = CHOICE_NONE;
                        break;
                    }
                    if (raw.equals(CHOICE_SKIP_ALL_KEY)) {
                        choice = CHOICE_SKIP_ALL;
                        break;
                    }
                    if (raw.equals(CHOICE_SKIP_KEY)) {
                        choice = CHOICE_SKIP;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            label113:
            _log
                    .error("Unable to read choice from standard input", e);
        }
        return choice;
    }

    /**
     * TODO: bug: the rule only matches at the last element (ROOT),
     * but this is wrong since there can be more than one sentence in a document (with root)
     * @param elementPath
     */
    @Override
    public void onEnd(ElementPath elementPath) {
        _log.debug("--------- LEARNING @END ------------");
        Element current = elementPath.getCurrent();
        String parent = "none";


        if (elementPath.getCurrent().getParent() != null)
            parent = elementPath.getCurrent().getParent().getQName().getName();
        /*
         * There was a bug where the object rulesMatched was returned. At this call however this object
         * is always null and thus causes the programme to crash
         */
        List<RuleInstance> rulesMatched = this._tri.getAllRulesMatched();

        if (this._skipAll || this._sw.getCurrent().getNodeExpectation() != null
                || rulesMatched.size() == 0)
            return;

        System.out.format("Sentence read so far: [%s]%n",
                new Object[]{this._tri.getSentence()});
        renderChoices(rulesMatched);
        int choice = getChoice(rulesMatched.size());

        switch (choice) {
            case -3:
                break;
            case -2:
                this._skipAll = true;
                break;
            case -1:
                current.addAttribute("expect", "none");
                break;
            default:
                current.addAttribute("expect",
                        rulesMatched.get(choice).getName());
        }
        OutputWriter.renderXML(current);
    }

    //@Override
    public void run() {
        try {
            String ruleFile = this._args[0];
            String parseFile = this._args[1];
            String outFile = this._args[2];

            File fOutFile = new File(outFile);
            if (!(fOutFile.createNewFile())) {
                System.out.println("Target file [" + fOutFile
                        + "] already exists!");
                System.exit(-1);
            }
            if (!(fOutFile.canWrite())) {
                System.out.println("Target file [" + fOutFile
                        + "] is read-only!");
                System.exit(-1);
            }

            _log.debug("--- Reading in the rules");
            RuleBook rules = new RuleBook();
            rules.read(new File(ruleFile));

            this._sink = new SAXContentHandler(DocumentFactory.getInstance(),
                    this);

            this._tri = new TopicSentInterpreter(rules);
            this._tri.setFilterGeneralRules(false);

            this._sw = new SAXParser(this._tri);
            this._sw.filter(this._sink);
            XMLUtils.parse(new File(parseFile), this._sw);

//            System.out.println("--- Writing output file: " + outFile);
            XMLUtils.dumpDocumentToFile(fOutFile, this._sink.getDocument());
        } catch (Throwable e) {
            _log.fatal("Fatal error", e);
        }
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        new Learner(args).run();
    }

}
