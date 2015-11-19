package nez.ext;

import java.io.IOException;

import nez.Parser;
import nez.ast.CommonTree;
import nez.ast.Tree;
import nez.io.SourceContext;
import nez.io.StringContext;
import nez.main.Command;
import nez.main.CommandContext;
import nez.parser.ParsingMachine;
import nez.util.ConsoleUtils;

public class Cbench extends Command {

	@Override
	public void exec(CommandContext config) throws IOException {
		int fileIndex = 0;

		while (config.hasInput()) {
			Parser g = config.newParser();
			String file = config.getInputFileList().ArrayValues[fileIndex++];
			Tree<?> prototype = config.getStrategy().isDisabled("ast", true) ? null : new CommonTree();
			SourceContext input = config.nextInput();
			for (int i = 0; i < 10; i++) {
				g.perform(new ParsingMachine(), input, prototype);
				// we assume there is no syntax error
				if (input.hasUnconsumed()) {
					ConsoleUtils.println(input.getUnconsumedMessage());
					break;
				}
				input = StringContext.newFileContext(file);
			}
			g.logProfiler();
		}
	}

}
