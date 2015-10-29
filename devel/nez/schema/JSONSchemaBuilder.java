package nez.schema;

import java.util.ArrayList;
import java.util.List;

import nez.Grammar;
import nez.lang.Expression;

public class JSONSchemaBuilder extends SchemaBuilder {
	private Schema schema;

	public JSONSchemaBuilder(Grammar g) {
		super(g);
		new JSONGrammarCombinator(g);
		generateSchema(Catalog.class);
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
		newSymbols(members, struct.getTableName());
		Expression[] seq = new Expression[members.size()];
		int index = 0;
		for (Element element : members) {
			seq[index++] = P(element.getUniqueName());
			newElement(element);
		}
		System.out.println(struct.getTableName());
		Expression e = S(E("{"), P("S"), S(seq), P("S"), E("}"), P("S"));
		define(structName, e);
	}

	public void newElement(Element element) {
		Class<?> type = element.getType();
		Expression e = null;
		if (type.isArray()) {
			e = toArray(element.getType());
		} else if (type.isEnum()) {
			e = toEnum(type.getEnumConstants());
		} else {
			e = S(E("\""), E(element.getName()), E("\""), P("S"), P("NAMESEP"), P(type.getSimpleName()), Opt(P("VALUESEP")), P("S"));
		}
		define(element.getUniqueName(), e);
	}

	public void newSymbols(List<Element> members, String tableName) {
		Expression[] l = new Expression[members.size()];
		int index = 0;
		for (Element e : members) {
			l[index++] = E(e.getName());
		}
		define(tableName, S(l));
	}

	public Expression toArray(Class<?> type) {
		Expression tExpr = P(type.getSimpleName().replace("[]", ""));
		return S(E("["), P("S"), tExpr, R0(P("VALUESEP"), tExpr), E("]"));
	}

	public Expression toEnum(Object[] candidates) {
		Expression[] choice = new Expression[candidates.length];
		int index = 0;
		for (String cand : (String[]) candidates) {
			choice[index++] = E(cand);
		}
		return Choice(choice);
	}

	protected Expression toMemberList(List<Element> members, String tableName) {
		Expression[] l = new Expression[members.size()];
		// for symbol table
		Expression[] symbols = new Expression[members.size()];
		int index = 0;
		for (Element e : members) {
			l[index] = Link(null, P(e.getUniqueName()));
			symbols[index++] = E(e.getName());
		}
		define(tableName, S(symbols));
		return R0(Choice(l));
	}

	protected Expression toSet(Struct s) {
		List<Element> members = s.getMembers();
		Expression[] l = new Expression[members.size() - s.getOptionalCount() + 1];
		int index = 1;
		l[0] = P(s.getName());
		for (Element e : s.getMembers()) {
			if (!e.isOptional()) {
				l[index++] = Exists(s.getTableName(), e.getName());
			}
		}
		return Local(s.getTableName(), S(l));
	}

	protected Expression toUniq(Element e, String tableName) {
		return S(And(E(e.getName())), Not(Exists(tableName, e.getName())), Symbol(tableName));
	}
}
