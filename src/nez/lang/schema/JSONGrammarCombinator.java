package nez.lang.schema;

import nez.lang.Expression;
import nez.lang.GrammarFile;

public class JSONGrammarCombinator extends PredefinedGrammarCombinator {
	public JSONGrammarCombinator(GrammarFile grammar) {
		super(grammar, "File");
	}

	public final Expression pFile() {
		Expression[] l = { _NonTerminal("S"), _NonTerminal("Root"), _NonTerminal("S") };
		return newSequence(l);
	}

	public final Expression pAny() {
		return newSequence(_NonTerminal("Member"), _NonTerminal("S"), newOption(_NonTerminal("VALUESEP")));
	}

	public final Expression pMember() {
		Expression[] l = { _NonTerminal("String"), _NonTerminal("NAMESEP"), _NonTerminal("Value"), };
		return newSequence(l);
	}

	public final Expression pValue() {
		Expression[] l = { _NonTerminal("String"), _NonTerminal("Number"), _NonTerminal("JSONObject"), _NonTerminal("Array"), _NonTerminal("Null"), _NonTerminal("True"), _NonTerminal("False") };
		return newChoice(l);
	}

	public final Expression pJSONObject() {
		Expression[] l = { newByteChar('{'), _NonTerminal("S"), _NonTerminal("Member"), _NonTerminal("S"), newRepetition(_NonTerminal("VALUESEP"), _NonTerminal("Member"), _NonTerminal("S")), newByteChar('}'), };
		return newSequence(l);
	}

	public final Expression pArray() {
		Expression[] valueSeq = { _NonTerminal("S"), _NonTerminal("Value"), _NonTerminal("S"), newRepetition(_NonTerminal("VALUESEP"), _NonTerminal("Value")) };
		Expression[] l = { newByteChar('['), newSequence(valueSeq), _NonTerminal("S"), newByteChar(']') };
		return newSequence(l);
	}

	public final Expression pString() {
		Expression notSeq = newSequence(newNot(newByteChar('"')), newAnyChar());
		Expression strValue = newChoice(newString("\\\""), newString("\\\\"), notSeq);
		Expression[] seq = { newByteChar('"'), newRepetition(strValue), newByteChar('"'), _NonTerminal("S") };
		return newSequence(seq);
	}

	public final Expression pNumber() {
		Expression choice = newChoice(newSequence(_NonTerminal("FRAC"), newOption(_NonTerminal("EXP"))), newEmpty());
		Expression[] l = { newOption(newByteChar('-')), _NonTerminal("INT"), choice, _NonTerminal("S") };
		return newSequence(l);
	}

	public final Expression pTrue() {
		return newString("true");
	}

	public final Expression pFalse() {
		return newString("false");
	}

	public final Expression pNull() {
		return newString("null");
	}

	public final Expression pNAMESEP() {
		Expression[] l = { newByteChar(':'), _NonTerminal("S") };
		return newSequence(l);

	}

	public final Expression pVALUESEP() {
		Expression[] l = { newByteChar(','), _NonTerminal("S") };
		return newSequence(l);
	}

	public final Expression pBOOLEAN() {
		Expression[] l = { newString("true"), newString("false") };
		return newChoice(l);
	}

	public final Expression pINT() {
		Expression[] l = { newByteChar('0'), newSequence(newCharSet("1-9"), newRepetition(_NonTerminal("DIGIT"))) };
		return newChoice(l);
	}

	public final Expression pDIGIT() {
		return newCharSet("0-9");
	}

	public final Expression pFRAC() {
		Expression[] l = { newByteChar('.'), newRepetition1(_NonTerminal("DIGIT")) };
		return newSequence(l);
	}

	public final Expression pEXP() {
		Expression choice = newChoice(newByteChar('-'), newByteChar('+'));
		Expression[] l = { newCharSet("Ee"), newOption(choice), newRepetition1(_NonTerminal("DIGIT")) };
		return newSequence(l);
	}

	public final Expression pSTRING() {
		Expression notSeq = newSequence(newNot(newByteChar('"')), newAnyChar());
		Expression strValue = newChoice(newString("\\\""), newString("\\\\"), notSeq);
		Expression[] seq = { newByteChar('"'), newRepetition(strValue), newByteChar('"'), _NonTerminal("S") };
		return newSequence(seq);
	}

	public final Expression pS() {
		Expression spacing = newCharSet("\t\n\r ");
		return newRepetition(spacing);
	}

}
