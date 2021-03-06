package nez.tool.ast;

import nez.debugger.DebugManager;
import nez.main.Command;
import nez.parser.Parser;

public class Cnezdb extends Command {
	@Override
	public void exec(CommandContext config) {
		Command.displayVersion();
		config.getStrategy().Optimization = false;
		Parser parser = config.newParser();
		DebugManager manager = new DebugManager(config.inputFileLists);
		manager.exec(parser, config.getStrategy());
	}
}
