package nez.lang;

import nez.util.UList;

public class Manipulator {
	public final static Manipulator RemoveASTandRename = new ASTConstructionEliminator(true);
	public final static Manipulator RemoveAST = new ASTConstructionEliminator(false);

	public Expression reshapeUndefined(Expression e) {
		return e;
	}

	public Expression reshapeProduction(Production p) {
		return p.getExpression().reshape(this);
	}

	public void updateProductionAttribute(Production origProduction, Production newProduction) {
	}

	public Expression reshapeEmpty(Empty e) {
		return e;
	}

	public Expression reshapeFailure(Failure e) {
		return e;
	}

	public Expression reshapeByteChar(ByteChar e) {
		return e;
	}

	public Expression reshapeByteMap(ByteMap e) {
		return e;
	}

	public Expression reshapeAnyChar(AnyChar e) {
		return e;
	}

	public Expression reshapeNonTerminal(NonTerminal e) {
		return e;
	}

	public Expression reshapeSequence(Sequence e) {
		int i = 0;
		boolean updated = false;
		for(i = 0; i < e.size(); i++) {
			Expression s = e.get(i);
			Expression r = s.reshape(this);
			if(r != s) {
				updated = true;
				break;
			}
		}
		if(!updated) {
			return e;
		}
		UList<Expression> l = e.newList();
		for(int j = 0; j < i; j++) {
			l.add(e.get(j));
		}
		for(int j = i; j < e.size(); j++) {
			GrammarFactory.addSequence(l, e.get(j).reshape(this));
			l.add(e.get(i));
		}
		return GrammarFactory.newSequence(e.s, l);
	}

	public Expression reshapeChoice(Choice e) {
		UList<Expression> l = e.newList();
		boolean updated = false;
		for(Expression s: e) {
			Expression ns = s.reshape(this);
			if(s != ns) {
				updated = true;
			}
			GrammarFactory.addChoice(l, ns);
		}
		if(updated) {
			return GrammarFactory.newChoice(e.s, l);
		}
		return e;
	}

	public Expression reshapeOption(Option e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeRepetition(Repetition e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeRepetition1(Repetition1 e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeAnd(And e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeNot(Not e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeMatch(Match e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeNew(New e) {
		return e;
	}

	public Expression reshapeLink(Link e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeTagging(Tagging e) {
		return e;
	}

	public Expression reshapeReplace(Replace e) {
		return e;
	}

	public Expression reshapeCapture(Capture e) {
		return e;
	}
	
	public Expression reshapeBlock(Block e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeLocalTable(LocalTable e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeDefSymbol(DefSymbol e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	public Expression reshapeIsSymbol(IsSymbol e) {
		return e;
	}

	public Expression reshapeExistsSymbol(ExistsSymbol e) {
		return e;
	}

	public Expression reshapeIsIndent(IsIndent e) {
		return e;
	}
	
	public Expression reshapeIfFlag(IfFlag e) {
		return e;
	}
	
	public Expression reshapeOnFlag(OnFlag e) {
		Expression inner = e.get(0).reshape(this);
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return updateInner(e, inner);
	}

	protected final Expression empty(Expression e) {
		return GrammarFactory.newEmpty(null);
	}

	protected final Expression fail(Expression e) {
		return GrammarFactory.newFailure(null);
	}

	protected final Expression updateInner(Option e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newOption(e.s, inner) : e;
	}
	protected final Expression updateInner(Repetition e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newRepetition(e.s, inner) : e;
	}
	protected final Expression updateInner(Repetition1 e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newRepetition1(e.s, inner) : e;
	}
	protected final Expression updateInner(And e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newAnd(e.s, inner) : e;
	}
	protected final Expression updateInner(Not e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newNot(e.s, inner) : e;
	}
	protected final Expression updateInner(Match e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newMatch(e.s, inner) : e;
	}
	protected final Expression updateInner(Link e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newLink(e.s, inner, e.index) : e;
	}
	protected final Expression updateInner(Block e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newBlock(e.s, inner) : e;
	}
	protected final Expression updateInner(LocalTable e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newLocal(e.s, e.getNameSpace(), e.getTable(), inner) : e;
	}
	protected final Expression updateInner(DefSymbol e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newDefSymbol(e.s, e.getNameSpace(), e.getTable(), inner) : e;
	}
	protected final Expression updateInner(OnFlag e, Expression inner) {
		if(!e.isInterned()) {
			e.inner = inner;
			return e;
		}
		return (e.get(0) != inner) ? GrammarFactory.newOnFlag(e.s, e.predicate, e.flagName, inner) : e;
	}
}

class ASTConstructionEliminator extends Manipulator {
	boolean renaming ;
	ASTConstructionEliminator(boolean renaming) {
		this.renaming = renaming;
	}
	public void updateProductionAttribute(Production origProduction, Production newProduction) {
		newProduction.transType = Typestate.BooleanType;
		newProduction.minlen = origProduction.minlen;
	}
	
	@Override
	public Expression reshapeNonTerminal(NonTerminal e) {
		if(renaming) {
			Production r = removeASTOperator(e.getProduction());
			if(!e.getLocalName().equals(r.getLocalName())) {
				return GrammarFactory.newNonTerminal(e.s, r.getNameSpace(), r.getLocalName());
			}
		}
		return e;
	}
	
	private Production removeASTOperator(Production p) {
		if(p.inferTypestate(null) == Typestate.BooleanType) {
			return p;
		}
		String name = "~" + p.getLocalName();
		Production r = p.getNameSpace().getProduction(name);
		if(r == null) {
			r = p.getNameSpace().newReducedProduction(name, p, this);
		}
		return r;
	}
	
	public Expression reshapeMatch(Match e) {
		return e.get(0).reshape(this);
	}
	
	public Expression reshapeNew(New e) {
		return empty(e);
	}
	
	public Expression reshapeLink(Link e) {
		return e.get(0).reshape(this);
	}

	public Expression reshapeTagging(Tagging e) {
		return empty(e);
	}

	public Expression reshapeReplace(Replace e) {
		return empty(e);
	}

	public Expression reshapeCapture(Capture e) {
		return empty(e);
	}
	
}
