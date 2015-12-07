package nez.schema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nez.Grammar;
import nez.Verbose;
import nez.ast.Symbol;
import nez.lang.Expression;
import nez.lang.GrammarHacks;
import nez.lang.Production;
import nez.lang.expr.ExpressionCommons;

public abstract class PredefinedGrammarCombinator extends GrammarHacks implements PredefinedGrammarSymbols {
	Grammar grammar;

	public PredefinedGrammarCombinator(Grammar grammar, String start) {
		this.grammar = grammar;
		load(start);
	}

	public final void load(String start) {
		Class<?> c = this.getClass();
		Method startMethod = null;
		try {
			startMethod = c.getMethod("p" + start);
			addProduction(start, startMethod);
		} catch (NoSuchMethodException e2) {
			Verbose.println(e2.toString());
		} catch (SecurityException e2) {
			Verbose.traceException(e2);
		}
		for (Method m : c.getDeclaredMethods()) {
			if (m.getReturnType() == Expression.class && m.getParameterTypes().length == 0) {
				String name = m.getName();
				if (name.startsWith("p")) {
					name = name.substring(1);
				}
				addProduction(name, m);
			}
		}
	}

	private void addProduction(String name, Method m) {
		try {
			Expression e = (Expression) m.invoke(this);
			grammar.newProduction(e.getSourcePosition(), 0, name, e);
		} catch (IllegalAccessException e1) {
			Verbose.traceException(e1);
		} catch (IllegalArgumentException e1) {
			Verbose.traceException(e1);
		} catch (InvocationTargetException e1) {
			Verbose.traceException(e1);
		}
	}

	protected final Expression _NonTerminal(String name) {
		return ExpressionCommons.newNonTerminal(null, grammar, name);
	}

	@Override
	public void addProduction(Production p) {
		// TODO Auto-generated method stub

	}

	@Override
	public Production get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}

interface PredefinedGrammarSymbols {
	static final Symbol _Key = Symbol.tag("key");
	static final Symbol _Value = Symbol.tag("value");
}
