package nez;

import nez.ast.Symbol;
import nez.lang.Expression;
import nez.lang.expr.ExpressionCommons;
import nez.lang.expr.NonTerminal;
import nez.util.UList;

public class GrammarBuilder {
	Grammar g;

	public GrammarBuilder(Grammar g) {
		this.g = g;
	}

	public void sampleProduction() {
		define("A", "AB", //
				OR, S("B", OR, "C"), //
				OR, "ccc");
	}

	public Expression sampleExpression() {
		return S("AB", //
				OR, S("B", OR, "C"), //
				OR, "ccc");
	}

	protected final Expression ANY = ExpressionCommons.newCany(null, false);
	protected final Expression OR = null;

	protected void define(String name, Object... args) {
		g.newProduction(name, S(args));
	}

	protected final Expression S(Object... args) {
		if (args.length == 0) {
			return ExpressionCommons.newEmpty(null);
		}
		if (args.length == 1) {
			return E(args[0]);
		}
		UList<Expression> l = new UList<>(new Expression[args.length]);
		UList<Expression> choice = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				if (choice == null) {
					choice = new UList<>(new Expression[args.length]);
				}
				ExpressionCommons.addChoice(choice, ExpressionCommons.newPsequence(null, l));
				l.clear(0);
			} else {
				ExpressionCommons.addSequence(l, E(args[i]));
			}
		}
		if (choice != null) {
			ExpressionCommons.addChoice(choice, ExpressionCommons.newPsequence(null, l));
			return ExpressionCommons.newPchoice(null, choice);
		}
		return ExpressionCommons.newPsequence(null, l);
	}

	protected final Expression E(Object value) {
		if (value instanceof Expression) {
			return (Expression) value;
		}
		if (value instanceof Symbol) {
			return ExpressionCommons.newTtag(null, (Symbol) value);
		}
		return ExpressionCommons.newString(null, value.toString());
	}

	protected final NonTerminal P(String name) {
		return g.newNonTerminal(null, name);
	}

	protected final Expression C(String t) {
		return ExpressionCommons.newCharSet(null, t);
	}

	protected final Expression Choice(Expression... l) {
		UList<Expression> seq = new UList<Expression>(new Expression[8]);
		for (Expression p : l) {
			ExpressionCommons.addChoice(seq, p);
		}
		return ExpressionCommons.newPchoice(null, seq);
	}

	protected final Expression R0(Object... args) {
		return ExpressionCommons.newPzero(null, S(args));
	}

	protected final Expression R1(Object... args) {
		return ExpressionCommons.newPone(null, S(args));
	}

	protected final Expression Opt(Object... args) {
		return ExpressionCommons.newPoption(null, S(args));
	}

	protected final Expression And(Object... args) {
		return ExpressionCommons.newPand(null, S(args));
	}

	protected final Expression Not(Object... args) {
		return ExpressionCommons.newPnot(null, S(args));
	}

	protected final Expression Link(Symbol label, Object... args) {
		return ExpressionCommons.newTlink(null, label, S(args));
	}

	protected final Expression New(Object... args) {
		return S(ExpressionCommons.newTnew(null, 0), S(args), ExpressionCommons.newTcapture(null, 0));
	}

	protected final Expression Set(String name, Object... args) {
		return ExpressionCommons.newTlink(null, Symbol.tag(name), S(args));
	}

	protected final Expression Add(Object... args) {
		return ExpressionCommons.newTlink(null, null, S(args));
	}

	protected final Expression Tag(String t) {
		return ExpressionCommons.newTtag(null, Symbol.tag(t));
	}

	protected final Expression Val(String t) {
		return ExpressionCommons.newTreplace(null, t);
	}

	protected final Expression Symbol(String tableName) {
		return ExpressionCommons.newXsymbol(null, P(tableName));
	}

	protected final Expression Exists(String table, String symbol) {
		return ExpressionCommons.newXexists(null, Symbol.tag(table), symbol);
	}

	protected final Expression Local(String table, Object... args) {
		return ExpressionCommons.newXlocal(null, Symbol.tag(table), S(args));
	}

}
