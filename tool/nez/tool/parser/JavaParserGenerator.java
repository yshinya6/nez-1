package nez.tool.parser;

import nez.lang.Grammar;

public class JavaParserGenerator extends CommonParserGenerator {

	@Override
	protected void initTypeMap() {
		// this.UniqueNumberingSymbol = false;
		this.addType("$parse", "boolean");
		this.addType("$tag", "int");
		this.addType("$label", "int");
		this.addType("$table", "int");
		this.addType("$text", "byte[]");
		this.addType("$index", "byte[]");
		this.addType("$set", "boolean[]");
		this.addType("$string", "String[]");

		this.addType("memo", "int");
		this.addType(_set(), "boolean[]");
		this.addType(_index(), "byte[]");
		this.addType(_temp(), "boolean");
		this.addType(_pos(), "int");
		this.addType(_tree(), "T");
		this.addType(_log(), "int");
		this.addType(_table(), "int");
		this.addType(_state(), "ParserContext<T>");
	}

	@Override
	protected String getFileExtension() {
		return "java";
	}

	@Override
	protected void generateHeader(Grammar g) {
		BeginDecl("public class " + _basename());
		ImportFile("/nez/tool/parser/ext/java-parser-runtime.txt");
	}

	@Override
	protected void generateFooter(Grammar g) {
		BeginDecl("public final static void main(String[] a)");
		{
			Statement("SimpleTree t = parse(a[0])");
			Statement("System.out.println(t)");
		}
		EndDecl();
		BeginDecl("public final static boolean start(ParserContext<?> c)");
		{
			Return(_funccall(_funcname(g.getStartProduction())));
		}
		EndDecl();
		EndDecl(); // end of class
		file.writeIndent("/*EOF*/");
	}

	@Override
	protected String _defun(String type, String name) {
		return "private static <T> " + type + " " + name;
	}

}
