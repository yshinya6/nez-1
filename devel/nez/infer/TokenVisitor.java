package nez.infer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nez.ast.Symbol;
import nez.ast.Tree;
import nez.infer.TokenVisitor.Undefined;
import nez.util.VisitorMap;

public class TokenVisitor extends VisitorMap<Undefined> {
	Map<String, Histogram> histogramMap;
	Map<String, Token> tokenMap;

	public TokenVisitor() {
		this.histogramMap = new HashMap<String, Histogram>();
	}

	public class Undefined implements InferenceTokenSymbol {
		public void accept(Tree<?> node) {

		}
	}

	private final void visit(Tree<?> node) {
		find(node.getTag().toString()).accept(node);
	}

	public List<Histogram> parse(Tree<?> node) {
		visit(node);
		return null;
	}

	public class Chunk extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			for (Tree<?> subnode : node) {
				visit(subnode);
			}
			for (Entry<String, Token> token : tokenMap.entrySet()) {
				token.getValue().getHistogram().commit();
			}
		}
	}

	public class Delim extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			String delim = node.toText();
			transaction(delim);
		}
	}

	public class MetaToken extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(String.format("%s*%s", node.getText(_open, ""), node.getText(_close, "")));
		}
	}

	public class _Integer extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class _Float extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class _String extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class IPv6 extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class IPv4 extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class Email extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class Path extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class Date extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	public class Time extends Undefined {
		@Override
		public void accept(Tree<?> node) {
			transaction(node.getTag().toString());
		}
	}

	private void transaction(String label) {
		if (!tokenMap.containsKey(label)) {
			Token token = new Token(label);
			token.getHistogram().update();
			tokenMap.put(label, token);
		} else {
			tokenMap.get(label).getHistogram().update();
		}
	}
}

interface InferenceTokenSymbol {
	public final static Symbol _name = Symbol.tag("name");
	public final static Symbol _value = Symbol.tag("value");
	public final static Symbol _open = Symbol.tag("open");
	public final static Symbol _close = Symbol.tag("close");
}
