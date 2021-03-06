package nez.lang;

import java.util.List;

import nez.lang.Nez.Byte;
import nez.lang.Nez.LeftFold;
import nez.lang.expr.Expressions;
import nez.util.UList;

public class ExpressionTransformer extends Expression.Visitor {

	protected Expression visit(Expression e, Object a) {
		return (Expression) e.visit(this, null);
	}

	@Override
	public Expression visitEmpty(Nez.Empty e, Object a) {
		return e;
	}

	@Override
	public Expression visitFail(Nez.Fail e, Object a) {
		return e;
	}

	@Override
	public Expression visitByte(Byte e, Object a) {
		return e;
	}

	@Override
	public Expression visitByteSet(Nez.ByteSet e, Object a) {
		return e;
	}

	@Override
	public Expression visitAny(Nez.Any e, Object a) {
		return e;
	}

	@Override
	public Expression visitMultiByte(Nez.MultiByte e, Object a) {
		return e;
	}

	@Override
	public Expression visitNonTerminal(NonTerminal e, Object a) {
		return e;
	}

	@Override
	public Expression visitPair(Nez.Pair e, Object a) {
		e.set(0, visit(e.get(0), a));
		e.set(1, visit(e.get(1), a));
		return e;
	}

	@Override
	public Expression visitSequence(Nez.Sequence e, Object a) {
		boolean reduced = false;
		int i = 0;
		for (i = 0; i < e.size(); i++) {
			Expression sub = visit(e.get(i), a);
			e.set(i, sub);
			if (sub instanceof Nez.Empty || sub instanceof Nez.Fail || sub instanceof Nez.Sequence || sub instanceof Nez.Pair) {
				reduced = true;
			}
		}
		if (reduced == true) {
			List<Expression> l = Expressions.newList2(e.size());
			for (Expression sub : e) {
				Expressions.addSequence(l, sub);
			}
			return Expressions.newSequence(e.getSourceLocation(), l);
		}
		return e;
	}

	@Override
	public Expression visitChoice(Nez.Choice e, Object a) {
		boolean reduced = false;
		int i = 0;
		for (i = 0; i < e.size(); i++) {
			Expression sub = visit(e.get(i), a);
			e.set(i, sub);
			if (sub instanceof Nez.Empty || sub instanceof Nez.Fail || sub instanceof Nez.Choice) {
				reduced = true;
			}
		}
		if (reduced == true) {
			UList<Expression> l = Expressions.newList(e.size());
			for (Expression sub : e) {
				Expressions.addChoice(l, sub);
			}
			return Expressions.newChoice(e.getSourceLocation(), l);
		}
		return e;
	}

	@Override
	public Expression visitOption(Nez.Option e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitZeroMore(Nez.ZeroMore e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitOneMore(Nez.OneMore e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitAnd(Nez.And e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitNot(Nez.Not e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitDetree(Nez.Detree e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitBeginTree(Nez.BeginTree e, Object a) {
		return e;
	}

	@Override
	public Object visitLeftFold(LeftFold e, Object a) {
		return e;
	}

	@Override
	public Expression visitLink(Nez.Link e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitTag(Nez.Tag e, Object a) {
		return e;
	}

	@Override
	public Expression visitReplace(Nez.Replace e, Object a) {
		return e;
	}

	@Override
	public Expression visitEndTree(Nez.EndTree e, Object a) {
		return e;
	}

	@Override
	public Expression visitBlockScope(Nez.BlockScope e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitLocalScope(Nez.LocalScope e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitSymbolAction(Nez.SymbolAction e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitSymbolMatch(Nez.SymbolMatch e, Object a) {
		return e;
	}

	@Override
	public Expression visitSymbolPredicate(Nez.SymbolPredicate e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

	@Override
	public Expression visitSymbolExists(Nez.SymbolExists e, Object a) {
		return e;
	}

	@Override
	public Expression visitIf(Nez.If e, Object a) {
		return e;
	}

	@Override
	public Expression visitOn(Nez.On e, Object a) {
		e.set(0, this.visit(e.get(0), a));
		return e;
	}

}
