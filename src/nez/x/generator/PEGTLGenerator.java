package nez.x.generator;

import nez.lang.Expression;
import nez.lang.Production;
import nez.lang.expr.Cany;
import nez.lang.expr.Cbyte;
import nez.lang.expr.Cmulti;
import nez.lang.expr.Cset;
import nez.lang.expr.NonTerminal;
import nez.lang.expr.Pand;
import nez.lang.expr.Pchoice;
import nez.lang.expr.Pnot;
import nez.lang.expr.Pone;
import nez.lang.expr.Poption;
import nez.lang.expr.Psequence;
import nez.lang.expr.Pzero;
import nez.lang.expr.Tcapture;
import nez.lang.expr.Tdetree;
import nez.lang.expr.Tlfold;
import nez.lang.expr.Tlink;
import nez.lang.expr.Tnew;
import nez.lang.expr.Treplace;
import nez.lang.expr.Ttag;
import nez.lang.expr.Xblock;
import nez.lang.expr.Xdef;
import nez.lang.expr.Xdefindent;
import nez.lang.expr.Xexists;
import nez.lang.expr.Xif;
import nez.lang.expr.Xindent;
import nez.lang.expr.Xis;
import nez.lang.expr.Xlocal;
import nez.lang.expr.Xmatch;
import nez.lang.expr.Xon;
import nez.parser.GenerativeGrammar;
import nez.parser.ParserGenerator;

public class PEGTLGenerator extends ParserGenerator {

	@Override
	public String getFileExtension() {
		return "hpp";
	}

	@Override
	public void makeHeader(GenerativeGrammar gg) {
		L("// The following is generated by the Nez Grammar Generator ");
		L("#include<pegtl.hh>");
		for (Production p : gg) {
			L("struct " + name(p) + ";");
		}

	}

	@Override
	public void makeFooter(GenerativeGrammar gg) {

	}

	@Override
	protected String name(Production p) {
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
	public void visitProduction(GenerativeGrammar gg, Production p) {
		Expression e = p.getExpression();
		L("struct " + name(p) + " : ");
		Begin("");
		W("pegtl::seq<");
		visitExpression(e);
		W(", pegtl::success> {};");
		End("");
	}

	@Override
	public void visitPempty(Expression e) {
		C("pegtl::success");
	}

	@Override
	public void visitPfail(Expression e) {
		C("pegtl::failure");
	}

	@Override
	public void visitNonTerminal(NonTerminal e) {
		W(name(e.getProduction()));
	}

	@Override
	public void visitCbyte(Cbyte e) {
		C("pegtl::one", e.byteChar);
	}

	@Override
	public void visitCset(Cset e) {
		C("pegtl::one", e.byteMap);
	}

	public void visitString(String s) {
		int cnt = 0;
		W("pegtl::string");
		W(_Open());
		for (int c = 0; c < s.length(); c++) {
			if (cnt > 0) {
				W(_Delim());
			}
			W(String.valueOf((int) s.charAt(c)));
			cnt++;
		}
		W(_Close());
	}

	@Override
	public void visitCany(Cany e) {
		W("pegtl::any");
	}

	@Override
	public void visitPoption(Poption e) {
		C("pegtl::opt", e);
	}

	@Override
	public void visitPzero(Pzero e) {
		C("pegtl::star", e);
	}

	@Override
	public void visitPone(Pone e) {
		C("pegtl::plus", e);
	}

	@Override
	public void visitPand(Pand e) {
		C("pegtl::at", e);
	}

	@Override
	public void visitPnot(Pnot e) {
		C("pegtl::not_at", e);
	}

	@Override
	public void visitPchoice(Pchoice e) {
		C("pegtl::sor", e);
	}

	@Override
	public void visitPsequence(Psequence e) {
		W("pegtl::seq<");
		// super.visitPsequence(e);
		W(">");
	}

	@Override
	public void visitTnew(Tnew e) {
		W("pegtl::success");
		// if(e.lefted) {
		// C("LCapture", e.shift);
		// }
		// else {
		// C("NCapture", e.shift);
		// }
	}

	@Override
	public void visitTcapture(Tcapture e) {
		W("pegtl::success");
		// C("Capture", e.shift);
	}

	@Override
	public void visitTtag(Ttag e) {
		W("pegtl::success");
		// C("Tagging", e.getTagName());
	}

	@Override
	public void visitTreplace(Treplace e) {
		W("pegtl::success");
		// C("Replace", StringUtils.quoteString('"', e.value, '"'));
	}

	@Override
	public void visitTlink(Tlink e) {
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

	@Override
	public void visitCmulti(Cmulti p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXblock(Xblock p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXlocal(Xlocal p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXdef(Xdef p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXexists(Xexists p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXmatch(Xmatch p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXis(Xis p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXdefindent(Xdefindent p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXindent(Xindent p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitTdetree(Tdetree p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXif(Xif p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitXon(Xon p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitTlfold(Tlfold p) {
		// TODO Auto-generated method stub

	}

}
