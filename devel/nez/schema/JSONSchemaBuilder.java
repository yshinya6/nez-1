package nez.schema;

import nez.Grammar;
import nez.lang.Expression;

public class JSONSchemaBuilder extends SchemaBuilder {

	public JSONSchemaBuilder(Grammar g) {
		super(g);
		new JSONGrammarCombinator(g);
		generateSchema(Catalog.class);
		g.dump();
	}

	public static void main(String[] args) {
		new JSONSchemaBuilder(new Grammar());
	}

	@Override
	protected void newRoot(Struct root) {
		String structName = root.getName();
		Expression rootExpression = Link(null, P(structName));
		Expression e = S(E("["), P("S"), rootExpression, P("S"), R1(P("VALUESEP"), rootExpression), P("S"), E("]"));
		define("Root", New(S(rootExpression, OR, e), Tag("Source")));
	}

	@Override
	protected void newStruct(Struct struct) {
		String structName = struct.getName();
		System.out.println(struct.getTableName());
		Expression e = S(E("{"), P("S"), toSet(struct, S(toMemberList(struct))), P("S"), E("}"), P("S"));
		define(structName, New(e, Val(structName), Tag("Struct")));
	}

	@Override
	protected void newElement(Element element) {
		Class<?> type = element.getType();
		Expression prefix = S(E("\""), this.toUniq(element), E("\""), P("S"), P("NAMESEP"));
		Expression suffix = null;
		if (type.isArray()) {
			suffix = toArray(element.getType());
		} else if (type.isEnum()) {
			suffix = toEnum(type.getEnumConstants());
		} else {
			suffix = S(Link(_value, P(type.getSimpleName())), Opt(P("VALUESEP")), P("S"));
		}
		define(element.getUniqueName(), New(prefix, suffix, Tag("Element")));
	}

	// TODO
	@Override
	protected void newElement(Array element) {

	}

	// TODO
	@Override
	protected void newElement(Enum element) {

	}

	@Override
	protected Expression toArray(Class<?> type) {
		Expression tExpr = P(type.getSimpleName().replace("[]", ""));
		return Link(_value, New(S(E("["), P("S"), Link(null, tExpr), R0(P("VALUESEP"), Link(null, tExpr)), E("]"), Tag("Array"))));
	}

	@Override
	protected Expression toEnum(Object[] candidates) {
		Expression[] choice = new Expression[candidates.length];
		int index = 0;
		for (String cand : (String[]) candidates) {
			choice[index++] = E(cand);
		}
		return Choice(choice);
	}

}
