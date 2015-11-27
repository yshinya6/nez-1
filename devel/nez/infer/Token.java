package nez.infer;

import nez.ast.Tree;
import nez.lang.Expression;
import nez.lang.expr.ExpressionCommons;
import nez.util.UList;

public class Token implements InferenceTokenSymbol {
	protected Expression inner;
	protected Histogram histogram;

	public Token() {
	}

	public Token(String label) {
		this.inner = ExpressionCommons.newNonTerminal(null, null, label);
	}

	public Histogram getHistogram() {
		return this.histogram;
	}

}

class MetaToken extends Token {

	public MetaToken(Tree<?> node) {
		this.inner = parseParendSequence(node);
	}

	private final Expression parseParendSequence(Tree<?> node) {
		UList<Expression> l = new UList<Expression>(new Expression[3]);
		l.add(ExpressionCommons.newCharSet(null, node.getText(_open, "")));
		l.add(ExpressionCommons.newNonTerminal(null, null, node.getText(_value, "")));
		l.add(ExpressionCommons.newCharSet(null, node.getText(_close, "")));
		return ExpressionCommons.newPsequence(null, l);
	}
}
