package nez.parser;

import java.util.HashMap;

import nez.ParserStrategy;
import nez.Verbose;
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
import nez.lang.expr.Xsymbol;
import nez.lang.expr.Xdefindent;
import nez.lang.expr.Xexists;
import nez.lang.expr.Xif;
import nez.lang.expr.Xindent;
import nez.lang.expr.Xis;
import nez.lang.expr.Xlocal;
import nez.lang.expr.Xmatch;
import nez.lang.expr.Xon;
import nez.parser.moz.MozInst;
import nez.util.FileBuilder;
import nez.util.StringUtils;

public abstract class ParserGenerator extends AbstractGenerator {
	// public abstract String getDesc();
	protected String dir;
	protected String grammarName;
	protected FileBuilder file;

	protected String OpenClosure = "("; // C()
	protected String CloseClosure = ")"; // C()
	protected String ClosureDelim = ", "; // C()
	protected String BeginIndent = "{"; // Begin()
	protected String EndIndent = "}"; // End()

	public ParserGenerator() {
		super(null);
		this.file = null;
	}

	public final void init(ParserStrategy strategy, String dir, String grammarName) {
		this.initLocalOption(strategy);
		this.dir = dir;
		this.grammarName = grammarName;
	}

	protected abstract String getFileExtension();

	public void generate(ParserGrammar gg) {
		this.openOutputFile(getFileExtension());
		makeHeader(gg);
		makeBody(gg);
		makeFooter(gg);
		file.writeNewLine();
		file.flush();
	}

	public void makeHeader(ParserGrammar gg) {
	}

	public void makeBody(ParserGrammar gg) {
		for (Production p : gg) {
			visitProduction(gg, p);
		}
	}

	public void makeFooter(ParserGrammar gg) {
	}

	public abstract void visitProduction(ParserGrammar gg, Production p);

	/* inc */

	protected void openOutputFile(String ext) {
		if (grammarName == null) {
			this.file = new FileBuilder(null);
		} else {
			String path = grammarName + "." + ext;
			if (dir != null) {
				path = dir + "/" + path;
			}
			this.file = new FileBuilder(path);
			Verbose.println("generating " + path + " ... ");
		}
	}

	HashMap<String, String> m = new HashMap<String, String>();

	protected String name(String s) {
		String name = m.get(s);
		if (name != null) {
			return name;
		}
		int loc = s.lastIndexOf(':');
		if (loc > 0) {
			name = s.substring(loc + 1).replace("!", "_").replace("-", "PEG");
		} else {
			name = s.replace("!", "_").replace("-", "PEG");
		}
		m.put(s, name);
		return name;
	}

	protected String name(Production p) {
		return name(p.getLocalName());
	}

	protected String unique(Expression e) {
		String key = e.toString() + " ";
		String unique = m.get(key);
		if (unique == null) {
			unique = "e" + m.size();
			m.put(key, unique);
		}
		return unique;
	}

	// Generator Macro

	@Deprecated
	protected ParserGenerator inc() {
		file.incIndent();
		return this;
	}

	@Deprecated
	protected ParserGenerator dec() {
		file.decIndent();
		return this;
	}

	protected ParserGenerator L(String line) {
		file.writeIndent(line);
		return this;
	}

	protected ParserGenerator L() {
		file.writeIndent();
		return this;
	}

	protected ParserGenerator W(String word) {
		file.write(word);
		return this;
	}

	protected ParserGenerator Begin(String t) {
		W(t);
		file.incIndent();
		return this;
	}

	protected ParserGenerator End(String t) {
		file.decIndent();
		if (t != null) {
			L(t);
		}
		return this;
	}

	protected ParserGenerator C(String name, Expression e) {
		int c = 0;
		W(name).W(OpenClosure);
		for (Expression sub : e) {
			if (c > 0) {
				W(ClosureDelim);
			}
			visitExpression(sub);
			c++;
		}
		W(CloseClosure);
		return this;
	}

	protected ParserGenerator C(String name, String first, Expression e) {
		W(name);
		W(OpenClosure);
		W(first);
		W(ClosureDelim);
		for (Expression sub : e) {
			visitExpression(sub);
		}
		W(CloseClosure);
		return this;
	}

	protected ParserGenerator C(String name) {
		W(name);
		W(OpenClosure);
		W(CloseClosure);
		return this;
	}

	protected ParserGenerator C(String name, String arg) {
		if (arg.length() > 1 && arg.startsWith("\"") && arg.endsWith("\"")) {
		} else {
			arg = StringUtils.quoteString('"', arg, '"');
		}
		W(name);
		W(OpenClosure);
		W(arg);
		W(CloseClosure);
		return this;
	}

	protected ParserGenerator C(String name, int arg) {
		W(name);
		W(OpenClosure);
		W(String.valueOf(arg));
		W(CloseClosure);
		return this;
	}

	protected ParserGenerator C(String name, boolean[] arg) {
		int cnt = 0;
		W(name);
		W(OpenClosure);
		for (int c = 0; c < arg.length; c++) {
			if (arg[c]) {
				if (cnt > 0) {
					W(ClosureDelim);
				}
				W(String.valueOf(c));
				cnt++;
			}
		}
		W(CloseClosure);
		return this;
	}

	public final void visitExpression(Expression e) {
		e.encode(this, null, null);
	}

	public void visitUndefined(Expression p) {

	}

	public abstract void visitPempty(Expression p);

	public abstract void visitPfail(Expression p);

	public abstract void visitCany(Cany p);

	public abstract void visitCbyte(Cbyte p);

	public abstract void visitCset(Cset p);

	public abstract void visitCmulti(Cmulti p);

	public abstract void visitPoption(Poption p);

	public abstract void visitPzero(Pzero p);

	public abstract void visitPone(Pone p);

	public abstract void visitPand(Pand p);

	public abstract void visitPnot(Pnot p);

	public abstract void visitPsequence(Psequence p);

	public abstract void visitPchoice(Pchoice p);

	public abstract void visitNonTerminal(NonTerminal p);

	// AST Construction
	public abstract void visitTlink(Tlink p);

	public abstract void visitTnew(Tnew p);

	public abstract void visitTlfold(Tlfold p);

	public abstract void visitTcapture(Tcapture p);

	public abstract void visitTtag(Ttag p);

	public abstract void visitTreplace(Treplace p);

	public abstract void visitTdetree(Tdetree p);

	// Symbol Tables
	public abstract void visitXblock(Xblock p);

	public abstract void visitXlocal(Xlocal p);

	public abstract void visitXdef(Xsymbol p);

	public abstract void visitXexists(Xexists p);

	public abstract void visitXmatch(Xmatch p);

	public abstract void visitXis(Xis p);

	public abstract void visitXif(Xif p);

	public abstract void visitXon(Xon p);

	public abstract void visitXdefindent(Xdefindent p);

	public abstract void visitXindent(Xindent p);

	@Override
	public final MozInst encode(Expression e, MozInst next, MozInst failjump) {
		return e.encode(this, next, failjump);
	}

	@Override
	public final MozInst encodeCany(Cany p, MozInst next, MozInst failjump) {
		this.visitCany(p);
		return null;
	}

	@Override
	public final MozInst encodeCbyte(Cbyte p, MozInst next, MozInst failjump) {
		this.visitCbyte(p);
		return null;
	}

	@Override
	public final MozInst encodeCset(Cset p, MozInst next, MozInst failjump) {
		this.visitCset(p);
		return null;
	}

	@Override
	public final MozInst encodeCmulti(Cmulti p, MozInst next, MozInst failjump) {
		this.visitCmulti(p);
		return null;
	}

	@Override
	public final MozInst encodePfail(Expression p) {
		this.visitPfail(p);
		return null;
	}

	@Override
	public final MozInst encodePoption(Poption p, MozInst next) {
		this.visitPoption(p);
		return null;
	}

	@Override
	public final MozInst encodePzero(Pzero p, MozInst next) {
		this.visitPzero(p);
		return null;
	}

	@Override
	public final MozInst encodePone(Pone p, MozInst next, MozInst failjump) {
		this.visitPone(p);
		return null;
	}

	@Override
	public final MozInst encodePand(Pand p, MozInst next, MozInst failjump) {
		this.visitPand(p);
		return null;
	}

	@Override
	public final MozInst encodePnot(Pnot p, MozInst next, MozInst failjump) {
		this.visitPnot(p);
		return null;
	}

	@Override
	public final MozInst encodePsequence(Psequence p, MozInst next, MozInst failjump) {
		this.visitPsequence(p);
		return null;
	}

	@Override
	public final MozInst encodePchoice(Pchoice p, MozInst next, MozInst failjump) {
		this.visitPchoice(p);
		return null;
	}

	@Override
	public final MozInst encodeNonTerminal(NonTerminal p, MozInst next, MozInst failjump) {
		this.visitNonTerminal(p);
		return null;
	}

	// AST Construction

	@Override
	public final MozInst encodeTdetree(Tdetree p, MozInst next, MozInst failjump) {
		if (enabledASTConstruction) {
			this.visitTdetree(p);
		} else {
			this.visitExpression(p.get(0));
		}
		return null;
	}

	@Override
	public final MozInst encodeTlink(Tlink p, MozInst next, MozInst failjump) {
		if (enabledASTConstruction) {
			this.visitTlink(p);
		} else {
			this.visitExpression(p.get(0));
		}
		return null;
	}

	@Override
	public final MozInst encodeTnew(Tnew p, MozInst next) {
		if (enabledASTConstruction) {
			this.visitTnew(p);
		}
		return null;
	}

	@Override
	public final MozInst encodeTlfold(Tlfold p, MozInst next) {
		if (enabledASTConstruction) {
			this.visitTlfold(p);
		}
		return null;
	}

	@Override
	public final MozInst encodeTcapture(Tcapture p, MozInst next) {
		if (enabledASTConstruction) {
			this.visitTcapture(p);
		}
		return null;
	}

	@Override
	public final MozInst encodeTtag(Ttag p, MozInst next) {
		if (enabledASTConstruction) {
			this.visitTtag(p);
		}
		return null;
	}

	@Override
	public final MozInst encodeTreplace(Treplace p, MozInst next) {
		if (enabledASTConstruction) {
			this.visitTreplace(p);
		}
		return null;
	}

	@Override
	public final MozInst encodeXblock(Xblock p, MozInst next, MozInst failjump) {
		this.visitXblock(p);
		return null;
	}

	@Override
	public final MozInst encodeXlocal(Xlocal p, MozInst next, MozInst failjump) {
		this.visitXlocal(p);
		return null;
	}

	@Override
	public final MozInst encodeXsymbol(Xsymbol p, MozInst next, MozInst failjump) {
		this.visitXdef(p);
		return null;
	}

	@Override
	public final MozInst encodeXexists(Xexists p, MozInst next, MozInst failjump) {
		this.visitXexists(p);
		return null;
	}

	@Override
	public final MozInst encodeXmatch(Xmatch p, MozInst next, MozInst failjump) {
		this.visitXmatch(p);
		return null;
	}

	@Override
	public final MozInst encodeXis(Xis p, MozInst next, MozInst failjump) {
		this.visitXis(p);
		return null;
	}

	@Override
	public final MozInst encodeXdefindent(Xdefindent p, MozInst next, MozInst failjump) {
		this.visitXdefindent(p);
		return null;
	}

	@Override
	public final MozInst encodeXindent(Xindent p, MozInst next, MozInst failjump) {
		this.visitXindent(p);
		return null;
	}

	@Override
	public final MozInst encodePempty(Expression p, MozInst next) {
		this.visitPempty(p);
		return null;
	}

	@Override
	public final MozInst encodeXon(Xon p, MozInst next, MozInst failjump) {
		this.visitXon(p);
		return null;
	}

	@Override
	public final MozInst encodeXif(Xif p, MozInst next, MozInst failjump) {
		this.visitXif(p);
		return null;
	}

	@Override
	public final MozInst encodeExtension(Expression p, MozInst next, MozInst failjump) {
		this.visitUndefined(p);
		return null;
	}

}
