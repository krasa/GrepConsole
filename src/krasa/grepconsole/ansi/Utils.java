package krasa.grepconsole.ansi;

public class Utils {
	public static int tryParseInteger(String text) {
		if ("".equals(text))
			return 0;

		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
