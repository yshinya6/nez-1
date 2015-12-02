package nez.infer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nez.ast.Symbol;
import nez.ast.Tree;
import nez.util.ConsoleUtils;

public class TokenVisitor extends nez.util.VisitorMap<nez.infer.TokenVisitor.Undefined> {
	List<TokenSequence> sequenceList;
	int currentSequenceNumber;

	public TokenVisitor() {
		this.sequenceList = new ArrayList<TokenSequence>();
		this.currentSequenceNumber = 0;
	}

	// public final Token[] getTokenList() {
	// return (Token[]) this.tokenMap.values().toArray();
	// }

	public class Undefined implements InferenceTokenSymbol {
		public void accept(Tree<?> node) {
			ConsoleUtils.println(node.formatSourceMessage("error", "unsupproted tag in PEG Learning System #" + node));
		}

		public void accept(Tree<?> node, TokenSequence seq) {
			this.accept(node);
		}
	}

	private final void visit(Tree<?> node) {
		find(node.getTag().toString()).accept(node);
	}

	private final void visit(Tree<?> node, TokenSequence seq) {
		find(node.getTag().toString()).accept(node, seq);
	}

	// public Token[] parse(Tree<?> node) {
	// for (Tree<?> seq : node) {
	// visit(seq);
	// }
	// Token[] tokenList = this.getTokenList();
	// for (Token token : tokenList) {
	// token.getHistogram().normalize();
	// }
	// return tokenList;
	// }

	public List<TokenSequence> parse(Tree<?> node) {
		for (Tree<?> chunk : node) {
			visit(chunk);
		}
		return this.sequenceList;
	}

	public class Chunk extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			extendSequenceList(node.size());
			for (Tree<?> seq : node) {
				visit(seq);
			}
		}
	}

	public class _Sequence extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			TokenSequence seq = sequenceList.get(currentSequenceNumber++);
			for (Tree<?> tokenNode : node) {
				visit(tokenNode, seq);
			}
			seq.commitAllHistograms();
		}
	}

	public class _MetaToken extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			TokenSequence seq = sequenceList.get(currentSequenceNumber++);
			this.transaction(node, seq);
			seq.commitAllHistograms();
		}

		private final void transaction(Tree<?> node, TokenSequence seq) {
			String label = String.format("%s*%s", node.get(_open), node.get(_close));
			Map<String, Token> tokenMap = seq.getTokenMap();
			if (!tokenMap.containsKey(label)) {
				MetaToken token = new MetaToken(node);
				token.getHistogram().update();
				tokenMap.put(label, token);
			} else {
				tokenMap.get(label).getHistogram().update();
			}
		}
	}

	public class Delim extends Undefined {
		@Override
		public void accept(Tree<?> node, TokenSequence seq) {
			this.transaction(node, seq);
		}

		private final void transaction(Tree<?> node, TokenSequence seq) {
			String label = node.toText();
			Map<String, Token> tokenMap = seq.getTokenMap();
			if (!tokenMap.containsKey(label)) {
				DelimToken token = new DelimToken();
				token.getHistogram().update();
				tokenMap.put(label, token);
			} else {
				tokenMap.get(label).getHistogram().update();
			}
		}
	}

	public class SimpleToken extends Undefined {
		@Override
		public void accept(Tree<?> node, TokenSequence seq) {
			seq.transaction(node.getTag().toString());
		}
	}

	public class _Integer extends SimpleToken {
	}

	public class _Float extends SimpleToken {
	}

	public class _String extends SimpleToken {
	}

	public class IPv6 extends SimpleToken {
	}

	public class IPv4 extends SimpleToken {
	}

	public class Email extends SimpleToken {
	}

	public class Path extends SimpleToken {
	}

	public class Date extends SimpleToken {
	}

	public class Time extends SimpleToken {
	}

	private final void extendSequenceList(int newSize) {
		int listSize = sequenceList.size();
		while (newSize > listSize++) {
			sequenceList.add(new TokenSequence());
		}
	}

}

interface InferenceTokenSymbol {
	public final static Symbol _name = Symbol.tag("name");
	public final static Symbol _value = Symbol.tag("value");
	public final static Symbol _open = Symbol.tag("open");
	public final static Symbol _close = Symbol.tag("close");
}
