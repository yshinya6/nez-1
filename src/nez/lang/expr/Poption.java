package nez.lang.expr;

import nez.ast.SourceLocation;
import nez.lang.Expression;
import nez.lang.Nez;

public class Poption extends Nez.Option {
	Poption(SourceLocation s, Expression e) {
		super(e);
		this.setSourceLocation(s);
	}

}