package nez.tool.ast;

import nez.ast.Tree;

public class SimpleTreeJSONWriter extends TreeWriter {
	@Override
	public String getFileExtension() {
		return "json";
	}

	@Override
	public void writeTree(Tree<?> node) {
		writeJSON(node);
		file.writeNewLine();
	}

	private void writeJSON(Tree<?> node) {
		file.writeIndent("{");
		file.writeIndent("\"tag\":\"%s\",", node.getTag().toString());
		file.writeIndent("\"pos\":%s,", node.getSourcePosition());
		file.writeIndent("\"line\":%s,", node.getLineNum());
		if (node.size() == 0) {
			file.writeIndent("\"text\":\"%s\"", node.toText().replace('\n', ' '));
			file.writeIndent("}");
			return;
		} else {
			file.writeIndent("\"children\":[");
			file.incIndent();
			for (int i = 0; i < node.size() - 1; i++) {
				writeJSON(node.get(i));
				file.write(",");
			}
			writeJSON(node.get(node.size() - 1));
			file.decIndent();
			file.writeIndent("]");
			file.writeIndent("}");
		}
	}

}
