package nez.infer;

import nez.ast.Tree;
import nez.lang.Expression;
import nez.lang.expr.ExpressionCommons;
import nez.util.UList;

public class Token implements InferenceTokenSymbol {
	protected String label;
	protected Histogram histogram;

	public Token() {
	}

	public Token(String label) {
		this.label = label;
		this.histogram = new Histogram(label);
	}

	public Histogram getHistogram() {
		return this.histogram;
	}

	// return similarity of histogram against a specified token's one
	public double calcHistogramSimilarity(Token target) {
		return Histogram.calcSimilarity(this.histogram, target.getHistogram());
	}

	public Expression getExpression() {
		return ExpressionCommons.newNonTerminal(null, null, this.label);
	}
}

class DelimToken extends Token {
	@Override
	public Expression getExpression() {
		return ExpressionCommons.newCharSet(null, this.label);
	}
}

class MetaToken extends Token {
	Tree<?> innerNode;

	public MetaToken(Tree<?> node) {
		this.innerNode = node;
	}

	// FIXME
	// assume that there is no nested MetaToken
	public Expression getExpression(Tree<?> node) {
		UList<Expression> l = new UList<Expression>(new Expression[3]);
		l.add(ExpressionCommons.newCharSet(null, node.getText(_open, "")));
		l.add(ExpressionCommons.newNonTerminal(null, null, node.getText(_value, "")));
		l.add(ExpressionCommons.newCharSet(null, node.getText(_close, "")));
		return ExpressionCommons.newPsequence(null, l);
	}
}
