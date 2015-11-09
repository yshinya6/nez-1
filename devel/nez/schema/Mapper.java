package nez.schema;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import nez.Grammar;
import nez.Parser;
import nez.Strategy;
import nez.ast.Symbol;
import nez.ast.Tree;
import nez.io.SourceContext;
import nez.schema.Catalog.Book;
import nez.schema.Mapper.Undefined;
import nez.util.ConsoleUtils;
import nez.util.VisitorMap;

public class Mapper extends VisitorMap<Undefined> {
	private Class<?> schema;
	private Object root;
	private HashMap<String, Class<?>> classMap;

	public static final Symbol _name = Symbol.tag("name");
	public static final Symbol _value = Symbol.tag("value");

	public Mapper(Class<?> schema) {
		this.schema = schema;
		this.classMap = new HashMap<String, Class<?>>();
		for (Class<?> inner : schema.getClasses()) {
			classMap.put(inner.getSimpleName(), inner);
		}
		init(Mapper.class, new Undefined());
	}

	public Object visit(Tree<?> node) {
		return find(node.getTag().toString()).accept(node);
	}

	public class Undefined {
		public Object accept(Tree<?> node) {
			ConsoleUtils.println("undefined: " + node);
			return null;
		}
	}

	public class Source extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			Tree<?> rootNode = node.get(0);
			try {
				root = schema.newInstance();
				for (Tree<?> element : rootNode) {
					Field f = schema.getField(element.getText(_name, ""));
					f.set(root, visit(element));
				}
				return root;
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public class Struct extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			String structName = (String) node.getValue();
			try {
				Class<?> target = classMap.get(structName);
				Object struct = target.getConstructor(schema).newInstance(root);
				for (Tree<?> element : node) {
					Field f = target.getField(element.getText(_name, ""));
					f.set(struct, visit(element));
				}
				return struct;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public class Element extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return visit(node.get(_value));
		}
	}

	public class TypedValue extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			ConsoleUtils.println("undefined: " + node);
			return null;
		}
	}

	public class _Integer extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return Integer.parseInt(node.toText());
		}
	}

	public class _Float extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return Float.parseFloat(node.toText());
		}
	}

	public class _String extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return node.toText();
		}
	}

	public class _True extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return true;
		}
	}

	public class _False extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return false;
		}
	}

	public class _Null extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			return null;
		}
	}

	public class _Array extends Undefined {
		@Override
		public Object accept(Tree<?> node) {
			Object[] newArray = new Object[node.size()];
			int index = 0;
			for (Tree<?> subnode : node) {
				newArray[index++] = visit(subnode);
			}
			Class<?> type = newArray[0].getClass();
			Object typedArray = Array.newInstance(type, node.size());
			index = 0;
			for (Object o : newArray) {
				Array.set(typedArray, index++, o);
			}
			return typedArray;
		}
	}

	public static void main(String[] args) throws IOException {
		SourceContext sc = SourceContext.newFileContext(args[0]);

		Grammar g = new Grammar();
		new JSONSchemaBuilder(g);
		Parser schematicParser = g.newParser(Strategy.newSafeStrategy());
		Tree<?> node = schematicParser.parseCommonTree(sc);
		if (node == null) {
			ConsoleUtils.exit(1, sc.getSyntaxErrorMessage());
		}
		ConsoleUtils.println("   ", node);
		Mapper m = new Mapper(Catalog.class);
		Catalog catalog = (Catalog) m.visit(node);
		for (Book book : catalog.list) {
			System.out.println(book.title);
			System.out.println(book.id);
			System.out.println(book.author);
			System.out.println("");
		}
	}
}
