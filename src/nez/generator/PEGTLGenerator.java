package nez.generator;

import nez.lang.Expression;
import nez.lang.Grammar;
import nez.lang.Production;
import nez.lang.expr.Uand;
import nez.lang.expr.Cany;
import nez.lang.expr.Cbyte;
import nez.lang.expr.Cset;
import nez.lang.expr.Tcapture;
import nez.lang.expr.Choice;
import nez.lang.expr.Tlink;
import nez.lang.expr.Tnew;
import nez.lang.expr.NonTerminal;
import nez.lang.expr.Unot;
import nez.lang.expr.Uoption;
import nez.lang.expr.Uzero;
import nez.lang.expr.Uone;
import nez.lang.expr.Treplace;
import nez.lang.expr.Sequence;
import nez.lang.expr.Ttag;

public class PEGTLGenerator extends GrammarGenerator {

	@Override
	public String getDesc() {
		return "Parsing Expression Grammar Template Library for C++11";
	}

	public void makeHeader(Grammar g) {
		L("// The following is generated by the Nez Grammar Generator ");
		L("#include<pegtl.hh>");

		for (Production p : g.getProductionList()) {
			L("struct " + _NonTerminal(p) + ";");
		}

	}

	public void makeFooter(Grammar g) {

	}

	protected String _NonTerminal(Production p) {
		return "p" + p.getLocalName().replace("~", "_").replace("!", "NOT").replace(".", "DOT");
	}

	protected String _Open() {
		return "<";
	};

	protected String _Close() {
		return ">";
	};

	protected String _Delim() {
		return ",";
	};

	public void visitGrouping(Expression e) {
		// W(_OpenGrouping());
		visitExpression(e);
		// W(_CloseGrouping());
	}

	@Override
	public void visitProduction(Production p) {
		Expression e = p.getExpression();
		L("struct " + _NonTerminal(p) + " : ");
		inc();
		L("");
		W("pegtl::seq<");
		visitExpression(e);
		W(", pegtl::success> {};");
		dec();
	}

	public void visitEmpty(Expression e) {
		C("pegtl::success");
	}

	public void visitFailure(Expression e) {
		C("pegtl::failure");
	}

	public void visitNonTerminal(NonTerminal e) {
		W(_NonTerminal(e.getProduction()));
	}

	public void visitByteChar(Cbyte e) {
		C("pegtl::one", e.byteChar);
	}

	public void visitByteMap(Cset e) {
		C("pegtl::one", e.byteMap);
	}

	public void visitString(String s) {
		int cnt = 0;
		W("pegtl::string").W(_Open());
		for (int c = 0; c < s.length(); c++) {
			if (cnt > 0) {
				W(_Delim());
			}
			W(String.valueOf((int) s.charAt(c)));
			cnt++;
		}
		W(_Close());
	}

	public void visitAnyChar(Cany e) {
		W("pegtl::any");
	}

	public void visitOption(Uoption e) {
		C("pegtl::opt", e);
	}

	public void visitRepetition(Uzero e) {
		C("pegtl::star", e);
	}

	public void visitRepetition1(Uone e) {
		C("pegtl::plus", e);
	}

	public void visitAnd(Uand e) {
		C("pegtl::at", e);
	}

	public void visitNot(Unot e) {
		C("pegtl::not_at", e);
	}

	public void visitChoice(Choice e) {
		C("pegtl::sor", e);
	}

	public void visitSequence(Sequence e) {
		W("pegtl::seq<");
		super.visitSequence(e);
		W(">");
	}

	public void visitNew(Tnew e) {
		W("pegtl::success");
		// if(e.lefted) {
		// C("LCapture", e.shift);
		// }
		// else {
		// C("NCapture", e.shift);
		// }
	}

	public void visitCapture(Tcapture e) {
		W("pegtl::success");
		// C("Capture", e.shift);
	}

	public void visitTagging(Ttag e) {
		W("pegtl::success");
		// C("Tagging", e.getTagName());
	}

	public void visitReplace(Treplace e) {
		W("pegtl::success");
		// C("Replace", StringUtils.quoteString('"', e.value, '"'));
	}

	public void visitLink(Tlink e) {
		// if(e.index != -1) {
		// C("Link", String.valueOf(e.index), e);
		// }
		// else {
		// C("Link", e);
		// }
		visitExpression(e.get(0));
	}

	@Override
	public void visitUndefined(Expression e) {
		if (e.size() > 0) {
			visitExpression(e.get(0));
		} else {
			W("pegtl::success");
		}
		// W("<");
		// W(e.getPredicate());
		// for(Expression se : e) {
		// W(" ");
		// visit(se);
		// }
		// W(">");
	}

}
