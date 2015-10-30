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

	public Struct(Class<?> c) {
		this.name = c.getSimpleName();
		this.members = new ArrayList<Element>();
		extract(c);
	}

	public void extract(Class<?> c) {
		for (Field sub : c.getFields()) {
			this.members.add(new Element(this, sub));
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
}

class Element {
	private String name;
	private Struct parent;
	private Class<?> type;

	public Element(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public Element(Struct parent, Field element) {
		this.name = element.getName();
		this.parent = parent;
		this.type = element.getType();
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
		return false;
	}
}

class SchemaWriter {

	Grammar grammar;

	Expression StringData() {
		return null;
	}
}
