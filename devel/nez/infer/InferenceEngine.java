package nez.infer;

import java.io.IOException;
import java.util.List;

import nez.Grammar;
import nez.Strategy;
import nez.ast.Tree;
import nez.io.SourceContext;
import nez.lang.GrammarFileLoader;

public class InferenceEngine {

	public InferenceEngine() {
	}

	public Grammar infer(String filePath) throws IOException {
		Tree<?> tokenTree = tokenize(filePath);
		this.discoverStructure(tokenTree);
		return this.generateGrammar();
	}

	public Tree<?> tokenize(String filePath) throws IOException {
		Strategy strategy = Strategy.newSafeStrategy();
		Grammar g = GrammarFileLoader.loadGrammar("inference_log.nez", strategy);
		SourceContext sc = SourceContext.newFileContext(filePath);
		return g.newParser(strategy).parseCommonTree(sc);
	}

	public void discoverStructure(Tree<?> tokenTree) {
		// 1. build histograms
		List<Histogram> histograms = new TokenVisitor().parse(tokenTree);
		assert (histograms == null);

		// 2. cluster histograms by whose characteristics
		// 3. identify structure from clustered histograms
	}

	public Grammar generateGrammar() {
		return null;
	}

}
