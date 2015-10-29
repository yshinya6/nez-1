package nez.schema;

import nez.Grammar;
import nez.GrammarBuilder;

public abstract class SchemaBuilder extends GrammarBuilder {

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

	public abstract void generateSchema(Class<?> t);

}
