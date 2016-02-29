package nez.tool.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import nez.ast.Symbol;
import nez.lang.ByteConsumption;
import nez.lang.Expression;
import nez.lang.FunctionName;
import nez.lang.Grammar;
import nez.lang.Nez;
import nez.lang.Nez.ChoicePrediction;
import nez.lang.Nez.IfCondition;
import nez.lang.Nez.Label;
import nez.lang.Nez.LocalScope;
import nez.lang.Nez.OnCondition;
import nez.lang.Nez.SymbolAction;
import nez.lang.Nez.SymbolExists;
import nez.lang.Nez.SymbolMatch;
import nez.lang.Nez.SymbolPredicate;
import nez.lang.NonTerminal;
import nez.lang.Production;
import nez.lang.SymbolDependency;
import nez.lang.SymbolDependency.SymbolDependencyAnalyzer;
import nez.lang.SymbolMutation;
import nez.lang.SymbolMutation.SymbolMutationAnalyzer;
import nez.lang.Typestate;
import nez.lang.Typestate.TypestateAnalyzer;
import nez.parser.MemoPoint;
import nez.parser.Parser;
import nez.parser.ParserCode;
import nez.parser.ParserStrategy;
import nez.parser.io.CommonSource;
import nez.util.ConsoleUtils;
import nez.util.FileBuilder;
import nez.util.StringUtils;
import nez.util.UList;

public abstract class CommonParserGenerator implements SourceGenerator {
	protected Parser parser;
	protected ParserStrategy strategy;
	protected ParserCode<?> code;
	protected String path;
	protected FileBuilder file;
	protected String base = "nez";
	//
	protected ByteConsumption consumption = new ByteConsumption();
	protected TypestateAnalyzer typeState = Typestate.newAnalyzer();
	protected SymbolMutationAnalyzer symbolMutation = SymbolMutation.newAnalyzer();
	protected SymbolDependencyAnalyzer symbolDeps = SymbolDependency.newAnalyzer();

	protected boolean verboseMode = true;
	protected boolean UniqueNumberingSymbol = true;
	protected boolean SupportedSwitchCase = true;
	protected boolean SupportedDoWhile = true;

	@Override
	public final void init(Grammar g, Parser parser, String path) {
		this.parser = parser;
		this.strategy = parser.getParserStrategy();
		this.code = parser.getParserCode();
		if (path == null) {
			this.file = new FileBuilder(null);
		} else {
			this.path = FileBuilder.extractFileName(path);
			this.base = this.path.replace(".nez", "");
			String filename = FileBuilder.changeFileExtension(this.path, this.getFileExtension());
			this.file = new FileBuilder(filename);
			ConsoleUtils.println("generating " + filename + " ... ");
		}
		this.initTypeMap();
	}

	@Override
	public void doc(String command, String urn, String outputFormat) {
		// file.writeIndent(LineComment + "Translated by nez " + command +
		// " -g " + urn + " --format " + outputFormat);
	}

	protected final void ImportFile(String path) {
		try {
			InputStream s = CommonSource.class.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(s));
			String line = null;
			while ((line = reader.readLine()) != null) {
				file.writeIndent(line);
			}
			reader.close();
		} catch (Exception e) {
			ConsoleUtils.exit(1, "cannot load " + path + "; " + e);
		}
	}

	protected abstract String getFileExtension();

	protected abstract void generateHeader(Grammar g);

	protected abstract void generateFooter(Grammar g);

	@Override
	public void generate() {
		Grammar g = this.parser.getGrammar();
		this.generateHeader(g);
		ConstVisitor constDecl = new ConstVisitor();
		constDecl.decl(g.getStartProduction());
		this.generateSymbolTables();
		this.generatePrototypes();

		this.generate(g);
		this.generateFooter(g);
		file.writeNewLine();
		file.flush();
	}

	protected void generatePrototypes() {

	}

	private void generate(Grammar g) {
		ParserGeneratorVisitor gen = new ParserGeneratorVisitor();
		gen.generate();
	}

	protected void Verbose(String stmt) {
		if (verboseMode) {
			file.writeIndent(_Comment() + " " + stmt);
		}
	}

	protected String _funcname(Production p) {
		return _funcname(p.getUniqueName());
	}

	protected String _funcname(String uname) {
		return "p" + uname.replace("!", "NOT").replace("~", "_").replace("&", "AND");
	}

	/* Types */

	protected HashMap<String, String> typeMap = new HashMap<>();

	protected abstract void initTypeMap();

	protected void addType(String name, String type) {
		typeMap.put(name, type);
	}

	protected String type(String name) {
		return typeMap.get(name);
	}

	/* Symbols */

	protected HashMap<String, String> nameMap = new HashMap<>();

	protected UList<String> tagList = new UList<>(new String[8]);
	protected HashMap<String, Integer> tagMap = new HashMap<>();

	protected HashMap<String, Integer> labelMap = new HashMap<>();
	protected UList<String> labelList = new UList<>(new String[8]);

	protected UList<String> tableList = new UList<>(new String[8]);
	protected HashMap<String, Integer> tableMap = new HashMap<>();

	final String _set(boolean[] b) {
		String key = StringUtils.stringfyBitmap(b);
		return nameMap.get(key);
	}

	final void DeclSet(boolean[] b) {
		String key = StringUtils.stringfyBitmap(b);
		String name = nameMap.get(key);
		if (name == null) {
			name = _set() + nameMap.size();
			nameMap.put(key, name);
			DeclConst(type("$set"), name, b.length, _initBooleanArray(b));
		}
	}

	final String _index(byte[] b) {
		String key = key(b);
		return nameMap.get(key);
	}

	final void DeclIndex(byte[] b) {
		String key = key(b);
		String name = nameMap.get(key);
		if (name == null) {
			name = _index() + nameMap.size();
			nameMap.put(key, name);
			DeclConst(type("$index"), name, b.length, _initByteArray(b));
		}
	}

	private String key(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (byte c : b) {
			sb.append(c);
			sb.append(",");
		}
		return sb.toString();
	}

	protected String _text(byte[] text) {
		String key = new String(text);
		return nameMap.get(key);
	}

	protected String _text(String key) {
		if (key == null) {
			return _Null();
		}
		return nameMap.get(key);
	}

	final void DeclText(byte[] text) {
		String key = new String(text);
		String name = nameMap.get(key);
		if (name == null) {
			name = _text() + nameMap.size();
			nameMap.put(key, name);
			DeclConst(type("$text"), name, text.length, _initByteArray(text));
		}
	}

	final void DeclByteSet(String name, boolean b[]) {
		Verbose(StringUtils.stringfyCharacterClass(b));
		DeclConst(type(_set()), name, b.length, _initBooleanArray(b));
	}

	final void DeclIndexMap(String name, byte b[]) {
		DeclConst(type(_index()), name, b.length, _initByteArray(b));
	}

	final String _tag(Symbol s) {
		if (!this.UniqueNumberingSymbol && s == null) {
			return _Null();
		}
		return _tagname(s == null ? "" : s.getSymbol());
	}

	final void DeclTag(String s) {
		if (!tagMap.containsKey(s)) {
			int n = tagMap.size();
			tagMap.put(s, n);
			tagList.add(s);
			DeclConst(this.type("$tag"), _tagname(s), _initTag(n, s));
		}
	}

	final String _label(Symbol s) {
		if (!this.UniqueNumberingSymbol && s == null) {
			return _Null();
		}
		return _labelname(s == null ? "" : s.getSymbol());
	}

	final void DeclLabel(String s) {
		if (!labelMap.containsKey(s)) {
			int n = labelMap.size();
			labelMap.put(s, n);
			labelList.add(s);
			if (this.UniqueNumberingSymbol || !s.equals("_")) {
				DeclConst(type("$label"), _labelname(s), _initLabel(n, s));
			}
		}
	}

	final String _table(Symbol s) {
		if (!this.UniqueNumberingSymbol && s.equals("")) {
			return _Null();
		}
		return _tablename(s == null ? "" : s.getSymbol());
	}

	final void DeclTable(Symbol t) {
		String s = t.getSymbol();
		if (!tableMap.containsKey(s)) {
			int n = tableMap.size();
			tableMap.put(s, n);
			tableList.add(s);
			DeclConst(type("$table"), _tablename(s), _initTable(n, s));
		}
	}

	final void generateSymbolTables() {
		if (UniqueNumberingSymbol) {
			generateSymbolTable("_tags", tagList);
			generateSymbolTable("_labels", labelList);
			generateSymbolTable("_tables", tableList);
		}
	}

	private void generateSymbolTable(String name, UList<String> l) {
		if (l.size() > 0) {
			DeclConst(this.type("$string"), name, l.size(), _initStringArray(l.ArrayValues, l.size()));
		}
	}

	protected String _basename() {
		return base;
	}

	protected String _ns() {
		return base + "_";
	}

	protected String _quote(String s) {
		if (s == null) {
			return "\"\"";
		}
		return StringUtils.quoteString('"', s.toString(), '"');
	}

	protected String _initBooleanArray(boolean[] b) {
		StringBuilder sb = new StringBuilder();
		sb.append(_BeginArray());
		for (int i = 0; i < b.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			if (b[i]) {
				sb.append(_True());
			} else {
				sb.append(_False());
			}
		}
		sb.append(_EndArray());
		return sb.toString();
	}

	protected String _initByteArray(byte[] b) {
		StringBuilder sb = new StringBuilder();
		sb.append(_BeginArray());
		for (int i = 0; i < b.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(_int(b[i]));
		}
		sb.append(_EndArray());
		return sb.toString();
	}

	protected String _initStringArray(String[] a, int size) {
		StringBuilder sb = new StringBuilder();
		sb.append(_BeginArray());
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(_quote(a[i]));
		}
		sb.append(_EndArray());
		return sb.toString();
	}

	protected String _tagname(String name) {
		return "_T" + name;
	}

	protected String _labelname(String name) {
		return "_L" + name;
	}

	protected String _tablename(String name) {
		return "_S" + name;
	}

	protected String _initTag(int id, String s) {
		return UniqueNumberingSymbol ? "" + id : _quote(s);
	}

	protected String _initLabel(int id, String s) {
		return UniqueNumberingSymbol ? "" + id : _quote(s);
	}

	protected String _initTable(int id, String s) {
		return UniqueNumberingSymbol ? "" + id : _quote(s);
	}

	/* function */
	HashMap<String, String> exprMap = new HashMap<>();
	HashMap<String, Expression> funcMap = new HashMap<>();
	UList<String> funcList = new UList<String>(new String[128]);
	HashSet<String> crossSet = new HashSet<>();
	HashMap<String, Integer> memoPointMap = new HashMap<>();

	private boolean checkFuncName(Production p) {
		String f = _funcname(p.getUniqueName());
		if (!funcMap.containsKey(f)) {
			funcMap.put(f, p.getExpression());
			funcList.add(f);
			MemoPoint memoPoint = code.getMemoPoint(p.getUniqueName());
			if (memoPoint != null) {
				memoPointMap.put(f, memoPoint.id);
				checkFuncName(p.getExpression());
			}
			return true;
		}
		crossSet.add(f);
		return false;
	}

	private String _funcname(Expression e) {
		String key = e.toString();
		return exprMap.get(key);
	}

	private void checkFuncName(Expression e) {
		String key = e.toString();
		String f = exprMap.get(key);
		if (f == null) {
			f = "e" + exprMap.size();
			exprMap.put(key, f);
			funcList.add(f);
			funcMap.put(f, e);
			return;
		}
		// System.out.println("cross: " + e);
		crossSet.add(f);
	}

	private void checkNonLexicalFuncName(Expression e) {
		if (e instanceof Nez.Byte || e instanceof Nez.ByteSet || e instanceof Nez.MultiByte || e instanceof Nez.Any) {
			return;
		}
		checkFuncName(e);
	}

	class ConstVisitor extends Expression.Visitor {

		ConstVisitor() {
			DeclTag("");
			DeclLabel("");
		}

		private Object decl(Production p) {
			if (checkFuncName(p)) {
				p.getExpression().visit(this, null);
			}
			return null;
		}

		private Object check(Expression e) {
			for (Expression sub : e) {
				sub.visit(this, null);
			}
			return null;
		}

		@Override
		public Object visitNonTerminal(NonTerminal e, Object a) {
			Production p = e.getProduction();
			return decl(p);
		}

		@Override
		public Object visitEmpty(Nez.Empty e, Object a) {
			return null;
		}

		@Override
		public Object visitFail(Nez.Fail e, Object a) {
			return null;
		}

		@Override
		public Object visitByte(Nez.Byte e, Object a) {
			return null;
		}

		@Override
		public Object visitByteSet(Nez.ByteSet e, Object a) {
			DeclSet(e.byteMap);
			return null;
		}

		@Override
		public Object visitAny(Nez.Any e, Object a) {
			return null;
		}

		@Override
		public Object visitMultiByte(Nez.MultiByte e, Object a) {
			DeclText(e.byteSeq);
			return null;
		}

		@Override
		public Object visitPair(Nez.Pair e, Object a) {
			return check(e);
		}

		@Override
		public Object visitSequence(Nez.Sequence e, Object a) {
			return check(e);
		}

		@Override
		public Object visitChoice(Nez.Choice e, Object a) {
			if (e.predicted != null) {
				DeclIndex(e.predicted.indexMap);
			}
			for (Expression sub : e) {
				checkFuncName(sub);
			}
			return check(e);
		}

		@Override
		public Object visitOption(Nez.Option e, Object a) {
			checkNonLexicalFuncName(e.get(0));
			return check(e);
		}

		@Override
		public Object visitZeroMore(Nez.ZeroMore e, Object a) {
			checkNonLexicalFuncName(e.get(0));
			return check(e);
		}

		@Override
		public Object visitOneMore(Nez.OneMore e, Object a) {
			checkNonLexicalFuncName(e.get(0));
			return check(e);
		}

		@Override
		public Object visitAnd(Nez.And e, Object a) {
			checkNonLexicalFuncName(e.get(0));
			return check(e);
		}

		@Override
		public Object visitNot(Nez.Not e, Object a) {
			checkNonLexicalFuncName(e.get(0));
			return check(e);
		}

		@Override
		public Object visitBeginTree(Nez.BeginTree e, Object a) {
			return check(e);
		}

		@Override
		public Object visitEndTree(Nez.EndTree e, Object a) {
			if (e.tag != null) {
				DeclTag(e.tag.getSymbol());
			}
			if (e.value != null) {
				DeclText(StringUtils.toUtf8(e.value));
			}
			return check(e);
		}

		@Override
		public Object visitFoldTree(Nez.FoldTree e, Object a) {
			if (e.label != null) {
				DeclLabel(e.label.getSymbol());
			}
			return check(e);
		}

		@Override
		public Object visitLinkTree(Nez.LinkTree e, Object a) {
			if (e.label != null) {
				DeclLabel(e.label.getSymbol());
			}
			return check(e);
		}

		@Override
		public Object visitTag(Nez.Tag e, Object a) {
			DeclTag(e.tag.getSymbol());
			return check(e);
		}

		@Override
		public Object visitReplace(Nez.Replace e, Object a) {
			DeclText(StringUtils.toUtf8(e.value));
			return check(e);
		}

		@Override
		public Object visitDetree(Nez.Detree e, Object a) {
			return check(e);
		}

		@Override
		public Object visitBlockScope(Nez.BlockScope e, Object a) {
			return check(e);
		}

		@Override
		public Object visitLocalScope(LocalScope e, Object a) {
			return check(e);
		}

		@Override
		public Object visitSymbolAction(SymbolAction e, Object a) {
			DeclTable(e.tableName);
			return check(e);
		}

		@Override
		public Object visitSymbolPredicate(SymbolPredicate e, Object a) {
			DeclTable(e.tableName);
			return check(e);
		}

		@Override
		public Object visitSymbolMatch(SymbolMatch e, Object a) {
			DeclTable(e.tableName);
			return check(e);
		}

		@Override
		public Object visitSymbolExists(SymbolExists e, Object a) {
			DeclTable(e.tableName);
			DeclText(StringUtils.toUtf8(e.symbol));
			return check(e);
		}

		@Override
		public Object visitScan(Nez.Scan e, Object a) {
			return check(e);
		}

		@Override
		public Object visitRepeat(Nez.Repeat e, Object a) {
			return check(e);
		}

		@Override
		public Object visitIf(IfCondition e, Object a) {
			return check(e);
		}

		@Override
		public Object visitOn(OnCondition e, Object a) {
			return check(e);
		}

		@Override
		public Object visitLabel(Label e, Object a) {
			return check(e);
		}
	}

	class ParserGeneratorVisitor extends Expression.Visitor {

		void generate() {
			int i;
			for (i = funcList.size() - 1; i >= 0; i--) {
				String f = funcList.get(i);
				generateFunction(f, funcMap.get(f));
			}
		}

		private String _eval(Expression e) {
			return _funccall(_funcname(e));
		}

		private String _eval(String uname) {
			return _funccall(_funcname(uname));
		}

		private void generateFunction(String name, Expression e) {
			Integer memoPoint = memoPointMap.get(name);
			Verbose(e.toString());
			initLocal();
			BeginFunc(name);
			{
				if (memoPoint != null) {
					String memoLookup = "memoLookupStateTree";
					String memoSucc = "memoStateTreeSucc";
					String memoFail = "memoStateFail";
					if (!typeState.isTree(e)) {
						memoLookup = memoLookup.replace("Tree", "");
						memoSucc = memoSucc.replace("Tree", "");
						memoFail = memoFail.replace("Tree", "");
					}
					if (!strategy.StatefulPackratParsing || !symbolDeps.isDependent(e)) {
						memoLookup = memoLookup.replace("State", "");
						memoSucc = memoSucc.replace("State", "");
						memoFail = memoFail.replace("State", "");
					}
					InitVal("memo", _Func(memoLookup, _int(memoPoint)));
					If("memo", _Eq(), "0");
					{
						String f = _eval(e);
						String[] n = SaveState(e);
						If(f);
						{
							Statement(_Func(memoSucc, _int(memoPoint), n[0]));
							Succ();
						}
						Else();
						{
							BackState(e, n);
							Statement(_Func(memoFail, _int(memoPoint)));
							Fail();
						}
						EndIf();
					}
					EndIf();
					Return(_Binary("memo", _Eq(), "1"));
				} else {
					visit(e, null);
					Succ();
				}
			}
			EndFunc();
		}

		void initFunc(Expression e) {

		}

		int nested = -1;

		private void visit(Expression e, Object a) {
			int lnested = this.nested;
			this.nested++;
			e.visit(this, a);
			this.nested--;
			this.nested = lnested;
		}

		protected void BeginScope() {
			if (nested > 0) {
				BeginLocalScope();
			}
		}

		protected void EndScope() {
			if (nested > 0) {
				EndLocalScope();
			}
		}

		HashMap<String, String> localMap;

		private void initLocal() {
			localMap = new HashMap<>();
		}

		private String local(String name) {
			if (!localMap.containsKey(name)) {
				localMap.put(name, name);
				return name;
			}
			return local(name + localMap.size());
		}

		private String InitVal(String name, String expr) {
			String type = type(name);
			String lname = local(name);
			VarDecl(type, lname, expr);
			return lname;
		}

		private String SavePos() {
			return InitVal(_pos(), _Field(_state(), "pos"));
		}

		private void BackPos(String lname) {
			VarAssign(_Field(_state(), "pos"), lname);
		}

		private String SaveTree() {
			return InitVal(_tree(), _Func("saveTree"));
		}

		private void BackTree(String lname) {
			Statement(_Func("backTree", lname));
		}

		private String SaveLog() {
			return InitVal(_log(), _Func("saveLog"));
		}

		private void BackLog(String lname) {
			Statement(_Func("backLog", lname));
		}

		private String SaveSymbolTable() {
			return InitVal(_table(), _Func("saveSymbolPoint"));
		}

		private void BackSymbolTable(String lname) {
			Statement(_Func("backSymbolPoint", lname));
		}

		private String[] SaveState(Expression inner) {
			String[] names = new String[4];
			names[0] = SavePos();
			if (typeState.inferTypestate(inner) != Typestate.Unit) {
				names[1] = SaveTree();
				names[2] = SaveLog();
			}
			if (symbolMutation.isMutated(inner)) {
				names[3] = SaveSymbolTable();
			}
			return names;
		}

		private void BackState(Expression inner, String[] names) {
			BackPos(names[0]);
			if (names[1] != null) {
				BackTree(names[1]);
			}
			if (names[2] != null) {
				BackLog(names[2]);
			}
			if (names[3] != null) {
				BackSymbolTable(names[3]);
			}
		}

		@Override
		public Object visitNonTerminal(NonTerminal e, Object a) {
			String f = _eval(e.getUniqueName());
			If(_Not(f));
			{
				Fail();
			}
			EndIf();
			return null;
		}

		@Override
		public Object visitEmpty(Nez.Empty e, Object a) {
			return null;
		}

		@Override
		public Object visitFail(Nez.Fail e, Object a) {
			Fail();
			return null;
		}

		@Override
		public Object visitByte(Nez.Byte e, Object a) {
			If(_Func("read"), _NotEq(), _byte(e.byteChar));
			{
				Fail();
			}
			EndIf();
			checkBinaryEOF(e.byteChar == 0);
			return null;
		}

		@Override
		public Object visitByteSet(Nez.ByteSet e, Object a) {
			If(_Not(MatchByteArray(e.byteMap, _Func("read"))));
			{
				Fail();
			}
			EndIf();
			checkBinaryEOF(e.byteMap[0]);
			return null;
		}

		private void checkBinaryEOF(boolean checked) {
			if (strategy.BinaryGrammar && checked) {
				If(_Func("eof"));
				{
					Fail();
				}
				EndIf();
			}
		}

		private String MatchByteArray(boolean[] byteMap, String c) {
			return _GetArray(_set(byteMap), c);
		}

		@Override
		public Object visitAny(Nez.Any e, Object a) {
			if (strategy.BinaryGrammar) {
				Statement(_Func("move", "1"));
				If(_Func("eof"));
				{
					Fail();
				}
				EndIf();
			} else {
				If(_Func("read"), _Eq(), "0");
				{
					Fail();
				}
				EndIf();
			}
			return null;
		}

		@Override
		public Object visitMultiByte(Nez.MultiByte e, Object a) {
			If(_Not(_Match(e.byteSeq)));
			{
				Fail();
			}
			EndIf();
			return null;
		}

		@Override
		public Object visitPair(Nez.Pair e, Object a) {
			for (Expression sub : e) {
				visit(sub, a);
			}
			return null;
		}

		@Override
		public Object visitSequence(Nez.Sequence e, Object a) {
			for (Expression sub : e) {
				visit(sub, a);
			}
			return null;
		}

		@Override
		public Object visitChoice(Nez.Choice e, Object a) {
			if (e.predicted != null && SupportedSwitchCase) {
				generateSwitchPrediction(e, e.predicted);
			} else {
				BeginScope();
				String unchoiced = InitVal(_temp(), _True());
				for (Expression sub : e) {
					String f = _eval(sub);
					If(unchoiced);
					{
						String[] n = SaveState(sub);
						Verbose(sub.toString());
						If(f);
						{
							VarAssign(unchoiced, _False());
						}
						Else();
						{
							BackState(sub, n);
						}
						EndIf();
					}
					EndIf();
				}
				If(unchoiced);
				{
					Fail();
				}
				EndIf();
				EndScope();
			}
			return null;
		}

		private void generateSwitchPrediction(Nez.Choice choice, ChoicePrediction p) {
			String temp = InitVal(_temp(), _True());
			Switch(_GetArray(_index(p.indexMap), _Func("prefetch")));
			Case("0");
			Fail();
			for (int i = 0; i < choice.size(); i++) {
				Case(_int(i + 1));
				Expression sub = choice.get(i);
				String f = _eval(sub);
				if (p.striped[i]) {
					Verbose(". " + sub);
					Statement(_Func("move", "1"));
				} else {
					Verbose(sub.toString());
				}
				VarAssign(temp, f);
				Break();
				EndCase();
			}
			EndSwitch();
			If(_Not(temp)); // FIXME slow?
			{
				Return(_False());
			}
			EndIf();
		}

		@Override
		public Object visitOption(Nez.Option e, Object a) {
			Expression sub = e.get(0);
			if (!tryOptionOptimization(sub)) {
				String f = _eval(sub);
				String[] n = SaveState(sub);
				Verbose(sub.toString());
				If(_Not(f));
				{
					BackState(sub, n);
				}
				EndIf();
			}
			return null;
		}

		@Override
		public Object visitZeroMore(Nez.ZeroMore e, Object a) {
			generateWhile(e, a);
			return null;
		}

		private void generateWhile(Expression e, Object a) {
			Expression sub = e.get(0);
			if (!this.tryRepetitionOptimization(sub, false)) {
				String f = _eval(sub);
				While(_True());
				{
					String[] n = SaveState(sub);
					Verbose(sub.toString());
					If(_Not(f));
					{
						BackState(sub, n);
						Break();
					}
					EndIf();
					CheckInfiniteLoop(sub, n[0]);
				}
				EndWhile();
			}
		}

		@Override
		public Object visitOneMore(Nez.OneMore e, Object a) {
			if (SupportedDoWhile) {
				generateDoWhile(e, a);
			} else {
				visit(e.get(0), a);
				generateWhile(e, a);
			}
			return null;
		}

		private void generateDoWhile(Expression e, Object a) {
			Expression sub = e.get(0);
			if (!this.tryRepetitionOptimization(sub, true)) {
				String f = _eval(sub);
				Do();
				{
					String[] n = SaveState(sub);
					Verbose(sub.toString());
					If(_Not(f));
					{
						BackState(sub, n);
						Break();
					}
					EndIf();
					CheckInfiniteLoop(sub, n[0]);
				}
				DoWhile(_True());
			}
		}

		private void CheckInfiniteLoop(Expression e, String var) {
			if (!consumption.isConsumed(e)) {
				If(var, _Eq(), _Field(_state(), "pos"));
				{
					Break();
				}
				EndIf();
			}
		}

		@Override
		public Object visitAnd(Nez.And e, Object a) {
			Expression sub = e.get(0);
			if (!this.tryAndOptimization(sub)) {
				String f = _funcname(sub);
				BeginScope();
				String n = SavePos();
				Verbose(sub.toString());
				If(_Not(f));
				{
					Fail();
				}
				EndIf();
				BackPos(n);
				EndScope();
			}
			return null;
		}

		@Override
		public Object visitNot(Nez.Not e, Object a) {
			Expression sub = e.get(0);
			if (!this.tryNotOptimization(sub)) {
				String f = _funcname(sub);
				BeginScope();
				String[] n = SaveState(sub);
				Verbose(sub.toString());
				If(f);
				{
					Fail();
				}
				EndIf();
				BackState(sub, n);
				EndScope();
			}
			return null;
		}

		private boolean tryOptionOptimization(Expression inner) {
			if (strategy.Olex) {
				if (inner instanceof Nez.Byte) {
					Nez.Byte e = (Nez.Byte) inner;
					If(_Func("prefetch"), _Eq(), _byte(e.byteChar));
					{
						if (strategy.BinaryGrammar && e.byteChar == 0) {
							If(_Not(_Func("eof")));
							{
								Statement(_Func("move", "1"));
							}
							EndIf();
						} else {
							Statement(_Func("move", "1"));
						}
					}
					EndIf();
					return true;
				}
				if (inner instanceof Nez.ByteSet) {
					Nez.ByteSet e = (Nez.ByteSet) inner;
					If(MatchByteArray(e.byteMap, _Func("prefetch")));
					{
						if (strategy.BinaryGrammar && e.byteMap[0]) {
							If(_Not(_Func("eof")));
							{
								Statement(_Func("move", "1"));
							}
							EndIf();
						} else {
							Statement(_Func("move", "1"));
						}
					}
					EndIf();
					return true;
				}
				if (inner instanceof Nez.MultiByte) {
					Nez.MultiByte e = (Nez.MultiByte) inner;
					Statement(_Match(e.byteSeq));
					return true;
				}
				if (inner instanceof Nez.Any) {
					// Nez.Any e = (Nez.Any) inner;
					If(_Not(_Func("eof")));
					{
						Statement(_Func("move", "1"));
					}
					EndIf();
					return true;
				}
			}
			return false;
		}

		private boolean tryRepetitionOptimization(Expression inner, boolean OneMore) {
			if (strategy.Olex) {
				if (inner instanceof Nez.Byte) {
					Nez.Byte e = (Nez.Byte) inner;
					if (OneMore) {
						visit(inner, null);
					}
					While(_Binary(_Func("prefetch"), _Eq(), _byte(e.byteChar)));
					{
						if (strategy.BinaryGrammar && e.byteChar == 0) {
							If(_Func("eof"));
							{
								Break();
							}
							EndIf();
						}
						Statement(_Func("move", "1"));
					}
					EndWhile();
					return true;
				}
				if (inner instanceof Nez.ByteSet) {
					Nez.ByteSet e = (Nez.ByteSet) inner;
					if (OneMore) {
						visit(inner, null);
					}
					While(MatchByteArray(e.byteMap, _Func("prefetch")));
					{
						if (strategy.BinaryGrammar && e.byteMap[0]) {
							If(_Func("eof"));
							{
								Break();
							}
							EndIf();
						}
						Statement(_Func("move", "1"));
					}
					EndWhile();
					return true;
				}
				if (inner instanceof Nez.MultiByte) {
					Nez.MultiByte e = (Nez.MultiByte) inner;
					if (OneMore) {
						visit(inner, null);
					}
					While(_Match(e.byteSeq));
					{
						EmptyStatement();
					}
					EndWhile();
					return true;
				}
				if (inner instanceof Nez.Any) {
					// Nez.Any e = (Nez.Any) inner;
					if (OneMore) {
						visit(inner, null);
					}
					While(_Not(_Func("eof")));
					{
						Statement(_Func("move", "1"));
					}
					EndWhile();
					return true;
				}
			}
			return false;
		}

		private boolean tryAndOptimization(Expression inner) {
			if (strategy.Olex) {
				if (inner instanceof Nez.Byte) {
					Nez.Byte e = (Nez.Byte) inner;
					If(_Func("prefetch"), _NotEq(), _byte(e.byteChar));
					{
						Fail();
					}
					EndIf();
					this.checkBinaryEOF(e.byteChar == 0);
					return true;
				}
				if (inner instanceof Nez.ByteSet) {
					Nez.ByteSet e = (Nez.ByteSet) inner;
					If(_Not(MatchByteArray(e.byteMap, _Func("prefetch"))));
					{
						Fail();
					}
					EndIf();
					this.checkBinaryEOF(e.byteMap[0]);
					return true;
				}
				if (inner instanceof Nez.MultiByte) {
					Nez.MultiByte e = (Nez.MultiByte) inner;
					If(_Not(_Match(e.byteSeq)));
					{
						Fail();
					}
					EndIf();
					return true;
				}
				if (inner instanceof Nez.Any) {
					// Nez.Any e = (Nez.Any) inner;
					If(_Func("eof"));
					{
						Fail();
					}
					EndIf();
					return true;
				}
			}
			return false;
		}

		private boolean tryNotOptimization(Expression inner) {
			if (strategy.Olex) {
				if (inner instanceof Nez.Byte) {
					Nez.Byte e = (Nez.Byte) inner;
					If(_Func("prefetch"), _Eq(), _byte(e.byteChar));
					{
						Fail();
					}
					EndIf();
					this.checkBinaryEOF(e.byteChar != 0);
					return true;
				}
				if (inner instanceof Nez.ByteSet) {
					Nez.ByteSet e = (Nez.ByteSet) inner;
					If(MatchByteArray(e.byteMap, _Func("prefetch")));
					{
						Fail();
					}
					EndIf();
					this.checkBinaryEOF(!e.byteMap[0]);
					return true;
				}
				if (inner instanceof Nez.MultiByte) {
					Nez.MultiByte e = (Nez.MultiByte) inner;
					If(_Match(e.byteSeq));
					{
						Fail();
					}
					EndIf();
					return true;
				}
				if (inner instanceof Nez.Any) {
					// Nez.Any e = (Nez.Any) inner;
					If(_Not(_Func("eof")));
					{
						Fail();
					}
					EndIf();
					return true;
				}
			}
			return false;
		}

		/* Tree Construction */

		@Override
		public Object visitBeginTree(Nez.BeginTree e, Object a) {
			Statement(_Func("beginTree", _int(e.shift)));
			return null;
		}

		@Override
		public Object visitEndTree(Nez.EndTree e, Object a) {
			Statement(_Func("endTree", _int(e.shift), _tag(e.tag), _text(e.value)));
			return null;
		}

		@Override
		public Object visitFoldTree(Nez.FoldTree e, Object a) {
			Statement(_Func("foldTree", _int(e.shift), _label(e.label)));
			return null;
		}

		@Override
		public Object visitLinkTree(Nez.LinkTree e, Object a) {
			BeginScope();
			String tree = SaveTree();
			visit(e.get(0), a);
			Statement(_Func("linkTree", /* _Null(), */_label(e.label)));
			BackTree(tree);
			EndScope();
			return null;
		}

		@Override
		public Object visitTag(Nez.Tag e, Object a) {
			Statement(_Func("tagTree", _tag(e.tag)));
			return null;
		}

		@Override
		public Object visitReplace(Nez.Replace e, Object a) {
			Statement(_Func("valueTree", _text(e.value)));
			return null;
		}

		@Override
		public Object visitDetree(Nez.Detree e, Object a) {
			BeginScope();
			String n1 = SaveTree();
			String n2 = SaveLog();
			visit(e.get(0), a);
			BackTree(n1);
			BackLog(n2);
			EndScope();
			return null;
		}

		@Override
		public Object visitBlockScope(Nez.BlockScope e, Object a) {
			BeginScope();
			String n = SaveSymbolTable();
			visit(e.get(0), a);
			BackSymbolTable(n);
			EndScope();
			return null;
		}

		@Override
		public Object visitLocalScope(LocalScope e, Object a) {
			BeginScope();
			String n = SaveSymbolTable();
			Statement(_Func("addSymbolMask", _table(e.tableName)));
			visit(e.get(0), a);
			BackSymbolTable(n);
			EndScope();
			return null;
		}

		@Override
		public Object visitSymbolAction(SymbolAction e, Object a) {
			BeginScope();
			String ppos = SavePos();
			visit(e.get(0), a);
			Statement(_Func("addSymbol", _table(e.tableName), ppos));
			EndScope();
			return null;
		}

		@Override
		public Object visitSymbolPredicate(SymbolPredicate e, Object a) {
			BeginScope();
			String ppos = SavePos();
			visit(e.get(0), a);
			if (e.op == FunctionName.is) {
				If(_Not(_Func("equals", _table(e.tableName), ppos)));
				{
					Fail();
				}
				EndIf();
			} else {
				If(_Not(_Func("contains", _table(e.tableName), ppos)));
				{
					Fail();
				}
				EndIf();
			}
			EndScope();
			return null;
		}

		@Override
		public Object visitSymbolMatch(SymbolMatch e, Object a) {
			If(_Not(_Func("matchSymbol", _table(e.tableName))));
			{
				Fail();
			}
			EndIf();
			return null;
		}

		@Override
		public Object visitSymbolExists(SymbolExists e, Object a) {
			if (e.symbol == null) {
				If(_Not(_Func("exists", _table(e.tableName))));
				{
					Fail();
				}
				EndIf();
			} else {
				If(_Not(_Func("existsSymbol", _table(e.tableName), _text(e.symbol))));
				{
					Fail();
				}
				EndIf();
			}
			return null;
		}

		@Override
		public Object visitScan(Nez.Scan e, Object a) {
			BeginScope();
			String ppos = SavePos();
			visit(e.get(0), a);
			Statement(_Func("scanCount", ppos, _long(e.mask), _int(e.shift)));
			EndScope();
			return null;
		}

		@Override
		public Object visitRepeat(Nez.Repeat e, Object a) {
			While(_Func("decCount"));
			{
				visit(e.get(0), a);
			}
			EndWhile();
			return null;
		}

		@Override
		public Object visitIf(IfCondition e, Object a) {
			return null;
		}

		@Override
		public Object visitOn(OnCondition e, Object a) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object visitLabel(Label e, Object a) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/* Syntax */

	protected String _Comment() {
		return "//";
	}

	protected String _Comment(String c) {
		return "/*" + c + "*/";
	}

	protected String _And() {
		return "&&";
	}

	protected String _Or() {
		return "||";
	}

	protected String _Not(String expr) {
		return "!" + expr;
	}

	protected String _Eq() {
		return "==";
	}

	protected String _NotEq() {
		return "!=";
	}

	protected String _True() {
		return "true";
	}

	protected String _False() {
		return "false";
	}

	protected String _Null() {
		return "null";
	}

	/* Expression */

	private String _GetArray(String array, String c) {
		return array + "[" + c + "]";
	}

	protected String _BeginArray() {
		return "{";
	}

	protected String _EndArray() {
		return "}";
	}

	protected String _BeginBlock() {
		return " {";
	}

	protected String _EndBlock() {
		return "}";
	}

	protected String _Field(String o, String name) {
		return o + "." + name;
	}

	protected String _Func(String name, String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(_state());
		sb.append(".");
		sb.append(name);
		sb.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(args[i]);
		}
		sb.append(")");
		return sb.toString();
	}

	protected String _Match(byte[] byteSeq) {
		return _Func("match", _text(byteSeq));
	}

	protected String _int(int n) {
		return "" + n;
	}

	protected String _long(long n) {
		return "" + n + "L";
	}

	protected String _byte(int ch) {
		// if (ch < 128 && (!Character.isISOControl(ch))) {
		// return "'" + (char) ch + "'";
		// }
		return "" + ch;
	}

	/* Expression */

	protected String _defun(String type, String name) {
		return "private static <T> " + type + "name";
	}

	protected String _argument(String var, String type) {
		if (type == null) {
			return var;
		}
		return type + " " + var;
	}

	protected String _argument() {
		return _argument(_state(), type(_state()));
	}

	protected String _funccall(String name) {
		return name + "(" + _state() + ")";
	}

	/* Statement */

	protected void BeginDecl(String line) {
		file.writeIndent(line);
		Begin();
	}

	protected void EndDecl() {
		End();
	}

	protected void Begin() {
		file.write(" {");
		file.incIndent();
	}

	protected void End() {
		file.decIndent();
		file.writeIndent();
		file.write("}");
	}

	protected void BeginFunc(String type, String name, String args) {
		file.writeIndent();
		file.write(_defun(type, name));
		file.write("(");
		file.write(args);
		file.write(")");
		Begin();
	}

	protected final void BeginFunc(String f, String args) {
		BeginFunc(type("$parse"), f, args);
	}

	protected final void BeginFunc(String f) {
		BeginFunc(type("$parse"), f, _argument());
	}

	protected void EndFunc() {
		End();
	}

	protected void BeginLocalScope() {
		file.writeIndent("{");
		file.incIndent();
	}

	protected void EndLocalScope() {
		file.decIndent();
		file.writeIndent();
		file.write("}");
	}

	protected void Line(String stmt) {
		file.writeIndent(stmt);
	}

	protected void Statement(String stmt) {
		file.writeIndent(stmt);
		Semicolon();
	}

	protected void EmptyStatement() {
		file.writeIndent();
		Semicolon();
	}

	protected void Semicolon() {
		file.write(";");
	}

	protected void LineComment(String stmt) {
		file.writeIndent(_Comment() + " " + stmt);
	}

	protected void Return(String expr) {
		Statement("return " + expr);
	}

	protected void Succ() {
		Return(_True());
	}

	protected void Fail() {
		Return(_False());
	}

	protected void If(String cond) {
		file.writeIndent("if (");
		file.write(cond);
		file.write(")");
		Begin();
	}

	protected String _Binary(String a, String op, String b) {
		return a + " " + op + " " + b;
	}

	protected void If(String a, String op, String b) {
		If(a + " " + op + " " + b);
	}

	protected void Else() {
		End();
		file.write(" else");
		Begin();
	}

	protected void EndIf() {
		End();
	}

	protected void While(String cond) {
		file.writeIndent();
		file.write("while (");
		file.write(cond);
		file.write(")");
		Begin();
	}

	protected void EndWhile() {
		End();
	}

	protected void Do() {
		file.writeIndent();
		file.write("do");
		Begin();
	}

	protected void DoWhile(String cond) {
		End();
		file.write("while (");
		file.write(cond);
		file.write(")");
		Semicolon();
	}

	protected void Break() {
		file.writeIndent("break");
		Semicolon();
	}

	protected void Switch(String c) {
		Line("switch(" + c + ")");
		Begin();
	}

	protected void EndSwitch() {
		End();
	}

	protected void Case(String n) {
		Line("case " + n + ": ");
	}

	protected void EndCase() {
	}

	protected void VarDecl(String name, String expr) {
		VarDecl(this.type(name), name, expr);
	}

	protected void VarDecl(String type, String name, String expr) {
		if (name == null) {
			VarAssign(name, expr);
		} else {
			Statement(type + " " + name + " = " + expr);
		}
	}

	protected void VarAssign(String v, String expr) {
		Statement(v + " = " + expr);
	}

	protected void DeclConst(String type, String name, String val) {
		if (type == null) {
			Statement("private final static " + name + " = " + val);
		} else {
			Statement("private final static " + type + " " + name + " = " + val);
		}
	}

	protected String _arity(int arity) {
		return "[" + arity + "]";
	}

	protected void DeclConst(String type, String name, int arity, String val) {
		if (type("$arity") != null) {
			DeclConst(type, name + _arity(arity), val);
		} else {
			DeclConst(type, name, val);
		}
	}

	protected void GCinc(String expr) {
	}

	protected void GCdec(String expr) {
	}

	/* Variables */

	protected String _state() {
		return "c";
	}

	protected String _pos() {
		return "pos";
	}

	protected String _cpos() {
		return _Field(_state(), "pos");
	}

	protected String _tree() {
		return "left";
	}

	protected String _log() {
		return "log";
	}

	protected String _table() {
		return "sym";
	}

	protected String _temp() {
		return "temp";
	}

	protected String _index() {
		return "_index";
	}

	protected String _set() {
		return "_byteset";
	}

	protected String _text() {
		return "_text";
	}

	// protected String _arity(String name) {
	// return name + "_len";
	// }

	protected void InitMemoPoint() {
		if (code.getMemoPointSize() > 0) {
			Statement(_Func("initMemo", _int(strategy.SlidingWindow), _int(code.getMemoPointSize())));
		}
	}
}