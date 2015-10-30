package nez.schema;

import java.util.List;

import nez.Grammar;
import nez.lang.Expression;

public class JSONSchemaBuilder extends SchemaBuilder {

	public JSONSchemaBuilder(Grammar g) {
		super(g);
		new JSONGrammarCombinator(g);
		generateSchema(Catalog.class);
		g.dump();
	}

	@Override
	public void generateSchema(Class<?> t) {
		Schema schema = new Schema(t);
		newRoot(schema.getRootStruct());
		for (Struct s : schema.getStructList()) {
			newStruct(s);
		}
	}

	public static void main(String[] args) {
		new JSONSchemaBuilder(new Grammar());
	}

	protected void newRoot(Struct root) {
		String structName = root.getName();
		Expression rootExpression = P(structName);
		Expression e = S(E("["), P("S"), rootExpression, P("S"), R1(P("VALUESEP"), rootExpression), P("S"), E("]"));
		define("Root", S(rootExpression, OR, e));

	}

	protected void newStruct(Struct struct) {
		String structName = struct.getName();
		System.out.println(struct.getTableName());
		Expression e = S(E("{"), P("S"), toSet(struct, S(toMemberList(struct))), P("S"), E("}"), P("S"));
		define(structName, New(e, Tag(structName)));
	}

	protected Expression toSet(Struct s, Expression inner) {
		List<Element> members = s.getMembers();
		Expression[] l = new Expression[members.size() - s.getOptionalCount() + 1];
		int index = 1;
		l[0] = P(s.getName());
		for (Element e : s.getMembers()) {
			if (!e.isOptional()) {
				l[index++] = Exists(s.getTableName(), e.getName());
			}
		}
		return Local(s.getTableName(), S(inner, S(l)));
	}

	protected Expression toMemberList(Struct struct) {
		List<Element> members = struct.getMembers();
		Expression[] l = new Expression[members.size()];
		// for generating symbol table
		Expression[] symbols = new Expression[members.size()];
		int index = 0;
		for (Element e : members) {
			newElement(e);
			l[index] = Link(null, P(e.getUniqueName()));
			symbols[index++] = E(e.getName());
		}
		define(struct.getTableName(), Choice(symbols));
		return R0(Choice(l));
	}

	protected void newElement(Element element) {
		Class<?> type = element.getType();
		Expression prefix = S(E("\""), this.toUniq(element), E("\""), P("S"), P("NAMESEP"));
		Expression suffix = null;
		if (type.isArray()) {
			suffix = toArray(element.getType());
		} else if (type.isEnum()) {
			suffix = toEnum(type.getEnumConstants());
		} else {
			suffix = S(P(type.getSimpleName()), Opt(P("VALUESEP")), P("S"));
		}
		define(element.getUniqueName(), S(prefix, New(suffix, Tag(element.getName()))));
	}

	// TODO
	protected void newElement(Array element) {

	}

	// TODO
	protected void newElement(Enum element) {

	}

	protected Expression toArray(Class<?> type) {
		Expression tExpr = P(type.getSimpleName().replace("[]", ""));
		return S(E("["), P("S"), Link(null, tExpr), R0(P("VALUESEP"), Link(null, tExpr)), E("]"));
	}

	protected Expression toEnum(Object[] candidates) {
		Expression[] choice = new Expression[candidates.length];
		int index = 0;
		for (String cand : (String[]) candidates) {
			choice[index++] = E(cand);
		}
		return Choice(choice);
	}

	protected Expression toUniq(Element e) {
		return S(And(E(e.getName())), Not(Exists(e.getTableName(), e.getName())), Symbol(e.getTableName()));
	}
}
