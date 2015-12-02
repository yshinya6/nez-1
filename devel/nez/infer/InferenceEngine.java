package nez.infer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nez.Grammar;
import nez.Strategy;
import nez.ast.Tree;
import nez.io.SourceContext;
import nez.lang.GrammarFileLoader;

public class InferenceEngine {
	private final double maxMass;
	private final double minCoverage;
	private final double clusterTolerance;

	public InferenceEngine() {
		this.maxMass = 0.01;
		this.minCoverage = 0.9;
		this.clusterTolerance = 0.01;
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

	private final void discoverStructure(Tree<?> tokenTree) {
		// 1. build histograms
		List<TokenSequence> analyzedTokenSequences = new TokenVisitor().parse(tokenTree);
		assert (analyzedTokenSequences == null);

		for (TokenSequence seq : analyzedTokenSequences) {
			// 2. cluster histograms by whose characteristics
			List<Cluster> clusterList = newClusterList(seq.getTokenList());
			// 3. identify structure from clustered histograms
			identifyStructure(clusterList);
		}

	}

	private final List<Cluster> newClusterList(Token[] tokenList) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		boolean clustered;
		for (Token token : tokenList) {
			clustered = false;
			for (Cluster cluster : clusters) {
				if (token.calcHistogramSimilarity(cluster.getToken(0)) < this.clusterTolerance) {
					cluster.addToken(token);
					clustered = true;
				}
			}
			if (!clustered) {
				clusters.add(new Cluster(token));
			}
		}
		return clusters;
	}

	private final void identifyStructure(List<Cluster> clusters) {

	}

	public Grammar generateGrammar() {
		return null;
	}

}
