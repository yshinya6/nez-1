package nez.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;

import nez.Grammar;
import nez.lang.Expression;

public class Schema {
	ArrayList<Struct> structList;
	Struct root;

	public Schema(Class<?> root) {
		this.structList = new ArrayList<Struct>();
		this.root = new Struct(root);
		addStruct(root);
		extract(root);
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
	ArrayList<Element> members;
	String name;

	public Struct(Class<?> c) {
		this.name = c.getSimpleName();
		this.members = new ArrayList<Element>();
		extract(c);
	}

	public void extract(Class<?> c) {
		for (Field sub : c.getFields()) {
			this.members.add(new Element(sub));
		}
	}

	public ArrayList<Element> getMembers() {
		return this.members;
	}

	public String getName() {
		return this.name;
	}

}

class Element {
	public String name;
	public String parentName;
	public Class<?> type;

	public Element(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public Element(Field element) {
		this.name = element.getName();
		this.parentName = element.getDeclaringClass().getSimpleName();
		this.type = element.getType();
	}

	public String getUniqueName() {
		return String.format("%s_%s", parentName, name);
	}

	@Override
	public String toString() {
		return String.format("%s : %s", name, type.getSimpleName());
	}
}

class SchemaWriter {

	Grammar grammar;

	Expression StringData() {
		return null;
	}
}
