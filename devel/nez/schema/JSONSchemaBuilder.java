package nez.schema;

import java.util.ArrayList;

import nez.Grammar;
import nez.lang.Expression;

public class JSONSchemaBuilder extends SchemaBuilder {
	Schema schema;
	ArrayList<Schema> schemas;

	public JSONSchemaBuilder(Grammar g) {
		super(g);
		new JSONGrammarCombinator(g);
		generateSchema(Book.class);
		g.dump();
	}

	@Override
	public void generateSchema(Class<?> t) {
		schema = new Schema(t);
		String rootClassName = t.getSimpleName();
		newRoot(rootClassName);
		for (Struct s : schema.getStructList()) {
			newStruct(s);
		}
	}

	public static void main(String[] args) {
		new JSONSchemaBuilder(new Grammar());
	}

	public void newRoot(String structName) {
		Expression root = P(structName);
		Expression e = S(E("["), P("S"), root, P("S"), R1(P("VALUESEP"), root), P("S"), E("]"));
		define("Root", S(root, OR, e));
	}

	public void newStruct(Struct struct) {
		String structName = struct.getName();
		ArrayList<Element> members = struct.getMembers();
		Expression[] seq = new Expression[members.size()];
		int index = 0;
		for (Element element : members) {
			seq[index++] = P(element.getUniqueName());
			newElement(element);
		}
		Expression e = S(E("{"), P("S"), S(seq), P("S"), E("}"), P("S"));
		define(structName, e);
	}

	public void newElement(Element element) {
		Expression e = S(E("\""), E(element.name), E("\""), P("S"), P("NAMESEP"), P(element.type.getSimpleName()), Opt(P("VALUESEP")), P("S"));
		define(element.getUniqueName(), e);
	}

}
