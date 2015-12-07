package nez.schema;

import java.util.List;

import nez.Grammar;
import nez.GrammarBuilder;
import nez.lang.Expression;

public abstract class SchemaBuilder extends GrammarBuilder implements SchemaBuilderSymbols {

	Format format;

	public SchemaBuilder(Grammar g) {
		super(g);
	}

	public SchemaBuilder(Grammar g, Format f) {
		super(g);
		this.format = f;
	}

	public void setFormat(Format f) {
		this.format = f;
	}

	public void generateSchema(Class<?> t) {
		Schema schema = new Schema(t);
		newRoot(schema.getRootStruct());
		for (Struct s : schema.getStructList()) {
			newStruct(s);
		}
	}

	protected abstract void newRoot(Struct root);

	protected abstract void newStruct(Struct struct);

	protected abstract void newElement(Element element);

	protected abstract void newElement(Array arrayElement);

	protected abstract void newElement(Enum enumElement);

	protected Expression toSet(Struct s, Expression inner) {
		List<Element> members = s.getMembers();
		Expression[] l = new Expression[members.size() - s.getOptionalCount()];
		int index = 0;
		for (Element e : s.getMembers()) {
			if (!e.isOptional()) {
				l[index++] = Exists(s.getTableName(), e.getName());
			}
		}
		return Local(s.getTableName(), S(inner, S(l)));
	}

	protected Expression toMemberList(Struct s) {
		List<Element> members = s.getMembers();
		Expression[] l = new Expression[members.size()];
		// for generating elements of symbol table
		Expression[] symbols = new Expression[members.size()];
		int index = 0;
		for (Element e : members) {
			newElement(e);
			l[index] = P(e.getUniqueName());
			symbols[index++] = E(e.getName());
		}
		define(s.getTableName(), Choice(symbols));
		return R0(Choice(l));
	}

	protected abstract Expression toArray(Class<?> type);

	protected Expression toEnum(Object[] candidates) {
		Expression[] choice = new Expression[candidates.length];
		int index = 0;
		for (String cand : (String[]) candidates) {
			choice[index++] = E(cand);
		}
		return Choice(choice);
	}

	protected Expression toUniq(Element e) {
		return S(And(E(e.getName())), Not(Exists(e.getTableName(), e.getName())), Link(_name, Symbol(e.getTableName())));
	}
}
