package nez.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;

import nez.Grammar;
import nez.lang.Expression;

public class Schema {
	private ArrayList<Struct> structList;
	private Struct root;

	public Schema(Class<?> root) {
		this.structList = new ArrayList<Struct>();
		this.root = new Struct(root);
		addStruct(root);
		extract(root);
	}

	public Struct getRootStruct() {
		return this.root;
	}

	public void addStruct(Class<?> struct) {
		this.structList.add(new Struct(struct));
	}

	public ArrayList<Struct> getStructList() {
		return this.structList;
	}

	public void extract(Class<?> root) {
		for (Class<?> sub : root.getClasses()) {
			this.structList.add(new Struct(sub));
		}
	}

}

class Struct {
	private ArrayList<Element> members;
	private String name;
	private boolean ordered = false; // default

	public Struct(Class<?> c) {
		this.name = c.getSimpleName();
		this.members = new ArrayList<Element>();
		this.ordered = c.isAnnotationPresent(Ordered.class) ? true : false;
		extract(c);
	}

	public void extract(Class<?> c) {
		for (Field sub : c.getFields()) {
			if (sub.isAnnotationPresent(Schematic.class)) {
				this.members.add(new Element(this, sub));
			}
		}
	}

	public ArrayList<Element> getMembers() {
		return this.members;
	}

	public String getName() {
		return this.name;
	}

	public String getTableName() {
		return String.format("T%.5h", this);
	}

	public int getOptionalCount() {
		return 0;
	}

	public boolean hasOrderedSequence() {
		return this.ordered;
	}
}

class Element {
	private String name;
	private Struct parent;
	private Class<?> type;
	private boolean optional = false;
	private Length length;
	private Range range;

	public Element(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public Element(Struct parent, Field element) {
		this.name = element.getName();
		this.parent = parent;
		this.type = element.getType();
		this.optional = element.isAnnotationPresent(Option.class) ? true : false;
		extractRange(element);

	}

	public void extractRange(Field element) {
		if (element.isAnnotationPresent(Range.class)) {
			this.range = element.getAnnotation(Range.class);
		}
		if (element.isAnnotationPresent(Length.class)) {
			this.length = element.getAnnotation(Length.class);
		}
	}

	public String getUniqueName() {
		return String.format("%s_%s", parent.getName(), name);
	}

	@Override
	public String toString() {
		return String.format("%s : %s", name, type.getSimpleName());
	}

	public String getName() {
		return this.name;
	}

	public Class<?> getType() {
		return this.type;
	}

	public String getTableName() {
		return this.parent.getTableName();
	}

	public boolean isOptional() {
		return this.optional;
	}

	public Range getRange() {
		return this.range;
	}

	public Length getLength() {
		return this.length;
	}
}

class Array extends Element {

	public Array(Struct parent, Field element) {
		super(parent, element);
	}

}

class Enum extends Element {

	public Enum(Struct parent, Field element) {
		super(parent, element);
	}

}

class SchemaWriter {

	Grammar grammar;

	Expression StringData() {
		return null;
	}
}
