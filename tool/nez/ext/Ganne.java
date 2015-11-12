package nez.ext;

import java.io.IOException;

import nez.Grammar;
import nez.Parser;
import nez.Strategy;
import nez.ast.Symbol;
import nez.ast.Tree;
import nez.lang.Expression;
import nez.lang.GrammarFileLoader;
import nez.lang.expr.ExpressionCommons;
import nez.util.ConsoleUtils;
import nez.util.UList;

public class Ganne extends GrammarFileLoader {

	public Ganne() {
		init(Ganne.class, new Undefined());
	}

	public class Undefined extends DefaultVisitor {
		@Override
		public Expression toExpression(Tree<?> node) {
			ConsoleUtils.println(node.formatSourceMessage("error", "unsupproted in ANNE #" + node));
			return null;
		}
	}

	static Parser anneParser;

	@Override
	public Parser getLoaderParser(String start) {
		if (anneParser == null) {
			try {
				Strategy option = Strategy.newSafeStrategy();
				Grammar g = GrammarFileLoader.loadGrammar("anne.nez", option);
				anneParser = g.newParser(option);
				strategy.report();
			} catch (IOException e) {
				ConsoleUtils.exit(1, "unload: " + e.getMessage());
			}
			assert (anneParser != null);
		}
		return anneParser;
	}

	public final static Symbol _Name = Symbol.tag("name");
	public final static Symbol _Content = Symbol.tag("content");

	@Override
	public void parse(Tree<?> node) {
		this.loadPredefinedProduction();
		for (Tree<?> nonterminal : node) {
			visit(nonterminal);
		}
	}

	private final void loadPredefinedProduction() {
		GrammarFileLoader fl = new Gnez();
		try {
			fl.load(getGrammar(), "log_pre.nez", Strategy.newSafeStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Expression visit(Tree<?> node) {
		return find(node.getTag().toString()).toExpression(node);
	}

	public class _Nonterminal extends Undefined {
		@Override
		public Expression toExpression(Tree<?> node) {
			String localName = node.getText(_Name, "");
			UList<Expression> l = new UList<Expression>(new Expression[5]);
			for (Tree<?> content : node.get(_Content)) {
				l.add(visit(content));
			}
			Expression inner = ExpressionCommons.newPsequence(null, l);
			getGrammarFile().addProduction(null, localName, inner);
			return inner;
		}
	}

	public class _Preamble extends Undefined {
		@Override
		public Expression toExpression(Tree<?> node) {
			// TODO Auto-generated method stub
			return super.toExpression(node);
		}
	}

	public class _Annotation extends Undefined {
		@Override
		public Expression toExpression(Tree<?> node) {
			// TODO Auto-generated method stub
			return super.toExpression(node);
		}
	}
}
