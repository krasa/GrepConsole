package krasa.grepconsole;

import static java.lang.Thread.sleep;

public class AnsiTest {

	static final String ANSI_RESET = "\u001B[0m";
	static final String ANSI_RED = "\u001B[31m";
	static final String ANSI_GREEN = "\u001B[32m";
	static final String ANSI_YELLOW = "\u001B[33m";
	static final String ANSI_BLUE = "\u001B[34m";
	;

	public static void main(String[] args) throws InterruptedException {
		int n = 0;
		while (true) {
			System.out.println(ANSI_RED + "This " + ANSI_RESET + "is an example: " + ANSI_BLUE + n + ANSI_RESET);
			System.out.println(ANSI_GREEN + "That " + ANSI_RESET + ANSI_YELLOW + "is not the same..." + ANSI_RESET);
			n++;
			sleep(500);
		}
	}
}