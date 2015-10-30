package nez.lang.schema;

import nez.lang.Expression;
import nez.lang.GrammarFile;

public class JSONSchemaGrammarGenerator extends SchemaGrammarGenerator {

	public JSONSchemaGrammarGenerator(GrammarFile gfile) {
		super(gfile);
	}

	@Override
	public void loadPredefinedRules() {
		new JSONGrammarCombinator(gfile);
	}

	@Override
	public void newRoot(String structName) {
		Expression root = _NonTerminal(structName);
		Expression[] seq = { _NonTerminal("VALUESEP"), root };
		Expression[] array = { _OpenSquare(), _S(), root, _S(), _OneMore(seq), _S(), _CloseSquare() };
		gfile.addProduction(null, "Root", _Choice(_Sequence(root), _Sequence(array)));
	}

	@Override
	public Element newElement(String elementName, String structName, Schema t) {
		Expression seq = _Sequence(_DQuat(), t.getSchemaExpression(), _DQuat(), _S(), _NonTerminal("NAMESEP"), t.next().getSchemaExpression(), _Option(_NonTerminal("VALUESEP")), _S());
		Element element = new Element(elementName, structName, seq, false);
		gfile.addProduction(null, element.getUniqueName(), seq);
		return element;
	}

	@Override
	public void newStruct(String structName, Schema t) {
		Expression[] l = { _OpenWave(), _S(), t.getSchemaExpression(), _S(), _CloseWave(), _S() };
		gfile.addProduction(null, structName, _Sequence(l));
	}

	@Override
	public Schema newTObject() {
		return new Schema(_NonTerminal("JSONObject"));
	}

	@Override
	public Schema newTStruct(String structName) {
		return new Schema(_NonTerminal(structName));
	}

	@Override
	public Schema newTArray(Schema t) {
		Expression tExpr = t.getSchemaExpression();
		Expression[] array = { _OpenSquare(), _S(), tExpr, _ZeroMore(_NonTerminal("VALUESEP"), tExpr), _CloseSquare() };
		return new Schema(_Sequence(array));
	}

	@Override
	public Schema newTEnum(String[] candidates) {
		Expression[] choice = new Expression[candidates.length];
		int index = 0;
		for (String cand : candidates) {
			choice[index++] = _String(cand);
		}
		return new Schema(_Choice(choice));
	}

	@Override
	public Schema newOthers() {
		return new Schema(_NonTerminal("Member"));
	}

	private final Expression _OpenSquare() {
		return _Char('[');
	}

	private final Expression _CloseSquare() {
		return _Char(']');
	}

	private final Expression _OpenWave() {
		return _Char('{');
	}

	private final Expression _CloseWave() {
		return _Char('}');
	}

}
