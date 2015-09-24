package nez.lang.expr;

import nez.ast.SourcePosition;
import nez.lang.Expression;
import nez.lang.GrammarTransducer;
import nez.lang.PossibleAcceptance;
import nez.lang.Typestate;
import nez.lang.Visa;
import nez.parser.AbstractGenerator;
import nez.parser.Instruction;

public class Pand extends Unary {
	Pand(SourcePosition s, Expression e) {
		super(s, e);
	}

	@Override
	public final boolean equalsExpression(Expression o) {
		if (o instanceof Pand) {
			return this.get(0).equalsExpression(o.get(0));
		}
		return false;
	}

	@Override
	public final void format(StringBuilder sb) {
		this.formatUnary(sb, "&", this.inner, null);
	}

	@Override
	public Expression reshape(GrammarTransducer m) {
		return m.reshapePand(this);
	}

	@Override
	public boolean isConsumed() {
		return false;
	}

	@Override
	public int inferTypestate(Visa v) {
		int t = this.inner.inferTypestate(v);
		if (t == Typestate.ObjectType) { // typeCheck needs to report error
			return Typestate.BooleanType;
		}
		return t;
	}

	@Override
	public short acceptByte(int ch) {
		return PossibleAcceptance.acceptAnd(this, ch);
	}

	@Override
	public Instruction encode(AbstractGenerator bc, Instruction next, Instruction failjump) {
		return bc.encodePand(this, next, failjump);
	}

}