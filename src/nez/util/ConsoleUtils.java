package nez.util;

public class ConsoleUtils {
	static boolean isColored = false;
	static {
		if (System.getenv("CLICOLOR") != null) {
			isColored = true;
		}
	}

	public final static void exit(int status, String message) {
		ConsoleUtils.println("EXIT " + message);
		System.exit(status);
	}

	// 31 :red 　　　32 green, 34 blue, 37 gray
	public final static void begin(int c) {
		if (isColored) {
			System.out.print("\u001b[00;" + c + "m");
		}
	}

	public final static void bold() {
		if (isColored) {
			System.out.print("\u001b[1m");
		}
	}

	public final static void end() {
		if (isColored) {
			System.out.print("\u001b[00m");
		}
	}

	public final static String bold(String text) {
		if (isColored) {
			return "\u001b[1m" + text + "\u001b[00m";
		}
		return text;
	}

	public final static void println(Object s) {
		System.out.println(s);
	}

	public final static void print(Object s) {
		System.out.print(s);
	}

	public final static void println(String format, Object... args) {
		System.out.println(String.format(format, args));
	}

	public final static void print(String format, Object... args) {
		System.out.print(String.format(format, args));
	}

	public final static void printIndent(String tab, Object o) {
		System.out.print(tab);
		String s = o.toString();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\n') {
				System.out.println();
				System.out.print(tab);
			} else {
				System.out.print(c);
			}
		}
	}

	public final static void printlnIndent(String tab, Object o) {
		printIndent(tab, o);
		System.out.println();
	}

	public final static boolean isDebug() {
		return System.getenv("DEBUG") != null;
	}

	public final static void debug(String s) {
		System.out.println(s);
	}

	public static void notice(String message) {
		System.out.println("NOTICE: " + message);
	}

	// console

}
