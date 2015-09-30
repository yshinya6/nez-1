package nez.ast.script.asm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.lang.model.type.NullType;

import nez.ast.jcode.JCodeTree;
import nez.ast.script.CommonSymbols;
import nez.ast.script.TypeSystem;

import org.objectweb.asm.Opcodes;

public class ScriptCompilerAsm implements CommonSymbols {
	// private Map<String, Class<?>> generatedClassMap = new HashMap<String,
	// Class<?>>();
	private TypeSystem typeSystem;
	private ScriptClassLoader cLoader;
	private ClassBuilder cBuilder;
	private MethodBuilder mBuilder;

	// private Stack<MethodBuilder> mBuilderStack = new Stack<MethodBuilder>();

	public ScriptCompilerAsm(TypeSystem typeSystem, ScriptClassLoader cLoader) {
		this.typeSystem = typeSystem;
		this.cLoader = cLoader;

	}

	public void openClass(String name) {
		this.cBuilder = new ClassBuilder(name, null, null, null);
	}

	public Class<?> closeClass() {
		// loader.setDump(true);
		Class<?> c = cLoader.definedAndLoadClass(this.cBuilder.getQualifiedClassName(), cBuilder.toByteArray());
		this.cBuilder = null; //
		return c;
	}

	HashMap<String, Method> methodMap = new HashMap<String, Method>();
	private VarEntry var;

	public final void visit(JCodeTree node) {
		Method m = lookupMethod("visit", node.getTag().getSymbol());
		if (m != null) {
			try {
				m.invoke(this, node);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			visitUndefined(node);
		}
	}

	protected final Method lookupMethod(String method, String tagName) {
		Method m = this.methodMap.get(tagName);
		if (m == null) {
			String name = method + tagName;
			try {
				m = this.getClass().getMethod(name, JCodeTree.class);
			} catch (NoSuchMethodException e) {
				return null;
			} catch (SecurityException e) {
				return null;
			}
			this.methodMap.put(tagName, m);
		}
		return m;
	}

	public Class<?> compileFuncDecl(String className, JCodeTree node) {
		this.openClass(className);
		this.visitFuncDecl(node);
		return this.closeClass();
	}

	private Class<?> t(JCodeTree node) {
		// node.getTypedClass();
		return typeSystem.typeof(node);
	}

	public void visitFuncDecl(JCodeTree node) {
		// this.mBuilderStack.push(this.mBuilder);
		JCodeTree nameNode = node.get(_name);
		JCodeTree args = node.get(_param);
		String name = nameNode.toText();
		Class<?> funcType = nameNode.getTypedClass();
		Class<?>[] paramTypes = new Class<?>[args.size()];
		for (int i = 0; i < paramTypes.length; i++) {
			paramTypes[i] = t(args.get(i));
		}
		this.mBuilder = this.cBuilder.newMethodBuilder(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, funcType, name, paramTypes);
		this.mBuilder.enterScope();// FIXME
		for (JCodeTree arg : args) {
			this.mBuilder.defineArgument(arg.toText(), t(arg));
		}
		this.mBuilder.loadArgs();
		visit(node.get(_body));
		this.mBuilder.exitScope();
		this.mBuilder.returnValue();
		this.mBuilder.endMethod();
	}

	// FIXME Block scope
	public void visitBlock(JCodeTree node) {
		this.mBuilder.enterScope();
		for (JCodeTree stmt : node) {
			visit(stmt);
		}
		this.mBuilder.exitScope();
	}

	public void visitVarDecl(JCodeTree node) {
		if (node.size() > 1) {
			JCodeTree varNode = node.get(_name);
			JCodeTree valueNode = node.get(_expr);
			visit(valueNode);
			varNode.setType(t(valueNode));
			this.mBuilder.createNewVarAndStore(varNode.toText(), valueNode.getTypedClass());
		}
	}

	public void visitVarDeclList(JCodeTree node) {
		visit(node.get(0));
	}

	// public void visitName(JCodeTree node) {
	// VarEntry var = this.mBuilder.getLocalVar(node.toText());
	// node.setType(var.getVarClass());
	// this.mBuilder.loadFromVar(var);
	// }
	//
	//
	//
	//
	// public void visitIf(JCodeTree node) {
	// visit(node.get(0));
	// this.mBuilder.push(true);
	//
	// Label elseLabel = this.mBuilder.newLabel();
	// Label mergeLabel = this.mBuilder.newLabel();
	//
	// this.mBuilder.ifCmp(Type.BOOLEAN_TYPE, this.mBuilder.NE, elseLabel);
	//
	// // then
	// visit(node.get(1));
	// this.mBuilder.goTo(mergeLabel);
	//
	// // else
	// this.mBuilder.mark(elseLabel);
	// visit(node.get(2));
	//
	// // merge
	// this.mBuilder.mark(mergeLabel);
	// }
	//
	// public void visitWhile(JCodeTree node) {
	// Label beginLabel = this.mBuilder.newLabel();
	// Label condLabel = this.mBuilder.newLabel();
	//
	// this.mBuilder.goTo(condLabel);
	//
	// // Block
	// this.mBuilder.mark(beginLabel);
	// visit(node.get(1));
	//
	// // Condition
	// this.mBuilder.mark(condLabel);
	// visit(node.get(0));
	// this.mBuilder.push(true);
	//
	// this.mBuilder.ifCmp(Type.BOOLEAN_TYPE, this.mBuilder.EQ, beginLabel);
	// }
	//
	// public void visitDoWhile(JCodeTree node) {
	// Label beginLabel = this.mBuilder.newLabel();
	//
	// // Do
	// this.mBuilder.mark(beginLabel);
	// visit(node.get(0));
	//
	// // Condition
	// visit(node.get(1));
	// this.mBuilder.push(true);
	//
	// this.mBuilder.ifCmp(Type.BOOLEAN_TYPE, this.mBuilder.EQ, beginLabel);
	// }
	//
	// public void visitFor(JCodeTree node) {
	// Label beginLabel = this.mBuilder.newLabel();
	// Label condLabel = this.mBuilder.newLabel();
	// node.requirePop();
	//
	// // Initialize
	// visit(node.get(0));
	//
	// this.mBuilder.goTo(condLabel);
	//
	// // Block
	// this.mBuilder.mark(beginLabel);
	// visit(node.get(3));
	// visit(node.get(2));
	//
	// // Condition
	// this.mBuilder.mark(condLabel);
	// visit(node.get(1));
	// this.mBuilder.push(true);
	// this.mBuilder.ifCmp(Type.BOOLEAN_TYPE, this.mBuilder.EQ, beginLabel);
	// }
	//
	// public void visitContinue(JCodeTree node) {
	// }
	//
	// public void visitBreak(JCodeTree node) {
	// }
	//
	// public void visitReturn(JCodeTree node) {
	// this.mBuilder.returnValue();
	// }
	//
	// public void visitThrow(JCodeTree node) {
	// }
	//
	// public void visitWith(JCodeTree node) {
	// }
	//
	// public void visitExpression(JCodeTree node) {
	// this.visit(node.get(0));
	// }
	//
	// public void visitAssign(JCodeTree node) {
	// JCodeTree nameNode = node.get(0);
	// JCodeTree valueNode = node.get(1);
	// VarEntry var = this.scope.getLocalVar(nameNode.toText());
	// visit(valueNode);
	// if (var != null) {
	// this.mBuilder.storeToVar(var);
	// } else {
	// this.scope.setLocalVar(nameNode.toText(),
	// this.mBuilder.createNewVarAndStore(valueNode.getTypedClass()));
	// }
	// }
	//
	// public void visitApply(JCodeTree node) {
	// // JCodeTree fieldNode = node.get(_recv"));
	// JCodeTree argsNode = node.get(_param);
	// JCodeTree name = node.get(_name);
	// // VarEntry var = null;
	//
	// Class<?>[] argTypes = new Class<?>[argsNode.size()];
	// for (int i = 0; i < argsNode.size(); i++) {
	// JCodeTree arg = argsNode.get(i);
	// this.visit(arg);
	// argTypes[i] = arg.getTypedClass();
	// }
	// org.objectweb.asm.commons.Method method =
	// Methods.method(node.getTypedClass(), name.toText(), argTypes);
	// this.mBuilder.invokeStatic(this.cBuilder.getTypeDesc(), method);
	// // var = this.scope.getLocalVar(top.toText());
	// // if (var != null) {
	// // this.mBuilder.loadFromVar(var);
	// //
	// // } else {
	// // this.generateRunTimeLibrary(top, argsNode);
	// // this.popUnusedValue(node);
	// // return;
	// // }
	// }
	//
	// public void generateRunTimeLibrary(JCodeTree fieldNode, JCodeTree
	// argsNode) {
	// String classPath = "";
	// String methodName = null;
	// for (int i = 0; i < fieldNode.size(); i++) {
	// if (i < fieldNode.size() - 2) {
	// classPath += fieldNode.get(i).toText();
	// classPath += ".";
	// } else if (i == fieldNode.size() - 2) {
	// classPath += fieldNode.get(i).toText();
	// } else {
	// methodName = fieldNode.get(i).toText();
	// }
	// }
	// Type[] argTypes = new Type[argsNode.size()];
	// for (int i = 0; i < argsNode.size(); i++) {
	// JCodeTree arg = argsNode.get(i);
	// this.visit(arg);
	// argTypes[i] = Type.getType(arg.getTypedClass());
	// }
	// this.mBuilder.callDynamicMethod("nez/ast/jcode/StandardLibrary",
	// "bootstrap", methodName, classPath, argTypes);
	// }
	//
	// public void visitField(JCodeTree node) {
	// JCodeTree top = node.get(0);
	// VarEntry var = null;
	// if (_Name.equals(top.getTag())) {
	// var = this.scope.getLocalVar(top.toText());
	// if (var != null) {
	// this.mBuilder.loadFromVar(var);
	// } else {
	// // TODO
	// return;
	// }
	// } else {
	// visit(top);
	// }
	// for (int i = 1; i < node.size(); i++) {
	// JCodeTree member = node.get(i);
	// if (_Name.equals(member.getTag())) {
	// this.mBuilder.getField(Type.getType(var.getVarClass()), member.toText(),
	// Type.getType(Object.class));
	// visit(member);
	// }
	// }
	// }
	//
	// public void visitBinaryNode(JCodeTree node) {
	// JCodeTree left = node.get(0);
	// JCodeTree right = node.get(1);
	// this.visit(left);
	// this.visit(right);
	// node.setType(typeInfferBinary(node, left, right));
	// this.mBuilder.callStaticMethod(JCodeOperator.class, node.getTypedClass(),
	// node.getTag().getSymbol(), left.getTypedClass(), right.getTypedClass());
	// this.popUnusedValue(node);
	// }
	//
	// public void visitCompNode(JCodeTree node) {
	// JCodeTree left = node.get(0);
	// JCodeTree right = node.get(1);
	// this.visit(left);
	// this.visit(right);
	// node.setType(boolean.class);
	// this.mBuilder.callStaticMethod(JCodeOperator.class, node.getTypedClass(),
	// node.getTag().getSymbol(), left.getTypedClass(), right.getTypedClass());
	// this.popUnusedValue(node);
	// }
	//
	// private Class<?> typeInfferBinary(JCodeTree binary, JCodeTree left,
	// JCodeTree right) {
	// Class<?> leftType = left.getTypedClass();
	// Class<?> rightType = right.getTypedClass();
	// if (leftType == int.class) {
	// if (rightType == int.class) {
	// if (binary.getTag().getSymbol().equals("Div")) {
	// return double.class;
	// }
	// return int.class;
	// } else if (rightType == double.class) {
	// return double.class;
	// } else if (rightType == String.class) {
	// return String.class;
	// }
	// } else if (leftType == double.class) {
	// if (rightType == int.class) {
	// return double.class;
	// } else if (rightType == double.class) {
	// return double.class;
	// } else if (rightType == String.class) {
	// return String.class;
	// }
	// } else if (leftType == String.class) {
	// return String.class;
	// } else if (leftType == boolean.class) {
	// if (rightType == boolean.class) {
	// return boolean.class;
	// } else if (rightType == String.class) {
	// return String.class;
	// }
	// }
	// throw new RuntimeException("type error: " + left + ", " + right);
	// }
	//
	// public void visitAdd(JCodeTree node) {
	// this.visitBinaryNode(node);
	// }
	//
	// public void visitSub(JCodeTree node) {
	// this.visitBinaryNode(node);
	// }
	//
	// public void visitMul(JCodeTree node) {
	// this.visitBinaryNode(node);
	// }
	//
	// public void visitDiv(JCodeTree node) {
	// this.visitBinaryNode(node);
	// }
	//
	// public void visitNotEquals(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitLessThan(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitLessThanEquals(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitGreaterThan(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitGreaterThanEquals(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitLogicalAnd(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitLogicalOr(JCodeTree node) {
	// this.visitCompNode(node);
	// }
	//
	// public void visitUnaryNode(JCodeTree node) {
	// JCodeTree child = node.get(0);
	// this.visit(child);
	// node.setType(this.typeInfferUnary(node.get(0)));
	// this.mBuilder.callStaticMethod(JCodeOperator.class, node.getTypedClass(),
	// node.getTag().getSymbol(), child.getTypedClass());
	// this.popUnusedValue(node);
	// }
	//
	// public void visitPlus(JCodeTree node) {
	// this.visitUnaryNode(node);
	// }
	//
	// public void visitMinus(JCodeTree node) {
	// this.visitUnaryNode(node);
	// }
	//
	// private void evalPrefixInc(JCodeTree node, int amount) {
	// JCodeTree nameNode = node.get(0);
	// VarEntry var = this.scope.getLocalVar(nameNode.toText());
	// if (var != null) {
	// node.setType(int.class);
	// this.mBuilder.callIinc(var, amount);
	// if (!node.requiredPop) {
	// this.mBuilder.loadFromVar(var);
	// }
	// } else {
	// throw new RuntimeException("undefined variable " + nameNode.toText());
	// }
	// }
	//
	// private void evalSuffixInc(JCodeTree node, int amount) {
	// JCodeTree nameNode = node.get(0);
	// VarEntry var = this.scope.getLocalVar(nameNode.toText());
	// if (var != null) {
	// node.setType(int.class);
	// if (!node.requiredPop) {
	// this.mBuilder.loadFromVar(var);
	// }
	// this.mBuilder.callIinc(var, amount);
	// } else {
	// throw new RuntimeException("undefined variable " + nameNode.toText());
	// }
	// }
	//
	// public void visitSuffixInc(JCodeTree node) {
	// this.evalSuffixInc(node, 1);
	// }
	//
	// public void visitSuffixDec(JCodeTree node) {
	// this.evalSuffixInc(node, -1);
	// }
	//
	// public void visitPrefixInc(JCodeTree node) {
	// this.evalPrefixInc(node, 1);
	// }
	//
	// public void visitPrefixDec(JCodeTree node) {
	// this.evalPrefixInc(node, -1);
	// }
	//
	// private Class<?> typeInfferUnary(JCodeTree node) {
	// Class<?> nodeType = node.getTypedClass();
	// if (nodeType == int.class) {
	// return int.class;
	// } else if (nodeType == double.class) {
	// return double.class;
	// }
	// throw new RuntimeException("type error: " + node);
	// }

	public void visitNull(JCodeTree p) {
		p.setType(NullType.class);
		this.mBuilder.pushNull();
	}

	// void visitArray(JCodeTree p){
	// this.mBuilder.newArray(Object.class);
	// }

	public void visitList(JCodeTree node) {
		for (JCodeTree element : node) {
			visit(element);
		}
	}

	public void visitTrue(JCodeTree p) {
		p.setType(boolean.class);
		this.mBuilder.push(true);
	}

	public void visitFalse(JCodeTree p) {
		p.setType(boolean.class);
		this.mBuilder.push(false);
	}

	public void visitInt(JCodeTree p) {
		p.setType(int.class);
		this.mBuilder.push(Integer.parseInt(p.toText()));
	}

	public void visitInteger(JCodeTree p) {
		this.visitInt(p);
	}

	public void visitOctalInteger(JCodeTree p) {
		p.setType(int.class);
		this.mBuilder.push(Integer.parseInt(p.toText(), 8));
	}

	public void visitHexInteger(JCodeTree p) {
		p.setType(int.class);
		this.mBuilder.push(Integer.parseInt(p.toText(), 16));
	}

	public void visitDouble(JCodeTree p) {
		p.setType(double.class);
		this.mBuilder.push(Double.parseDouble(p.toText()));
	}

	public void visitString(JCodeTree p) {
		p.setType(String.class);
		this.mBuilder.push(p.toText());
	}

	public void visitCharacter(JCodeTree p) {
		p.setType(String.class);
		this.mBuilder.push(p.toText());
		// p.setType(char.class);
		// this.mBuilder.push(p.toText().charAt(0));
	}

	public void visitUndefined(JCodeTree p) {
		System.out.println("undefined: " + p.getTag().getSymbol());
	}

}