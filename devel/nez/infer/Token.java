package nez.infer;

import nez.ast.Tree;
import nez.lang.Expression;
import nez.lang.expr.ExpressionCommons;
import nez.util.UList;

public class Token implements InferenceTokenSymbol {
	protected Expression expression;
	protected Histogram histogram;

	public Token() {
	}

	public Token(String label) {
		this.expression = ExpressionCommons.newNonTerminal(null, null, label);
	}

	public Histogram getHistogram() {
		return this.histogram;
	}

}

class MetaToken extends Token {
	Tree<?> innerNode;

	public MetaToken(Tree<?> node) {
		this.innerNode = node;
		this.expression = parseParenSequence(node);
	}

	// FIXME
	// assume that there is no nested MetaToken
	private final Expression parseParenSequence(Tree<?> node) {
		UList<Expression> l = new UList<Expression>(new Expression[3]);
		l.add(ExpressionCommons.newCharSet(null, node.getText(_open, "")));
		l.add(ExpressionCommons.newNonTerminal(null, null, node.getText(_value, "")));
		l.add(ExpressionCommons.newCharSet(null, node.getText(_close, "")));
		return ExpressionCommons.newPsequence(null, l);
	}
}
