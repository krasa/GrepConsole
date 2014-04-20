package krasa.grepconsole.ansi;

public class ColorTest {
	final static String CSI = "\u001b[";
	final static String RESET = CSI + "0m";

	final static Prefix[] prefixes = {
			new Prefix(0, " 0 normal"),
			new Prefix(1, " 1 bold/increased intensity"),
			new Prefix(3, " 3 italic on"),
			new Prefix(7, " 7 image negative"),
			new Prefix(8, " 8 conceal"),
			new Prefix(51, "51 framed"),
			new Prefix(9, " 9 crossed out (strike)"),
			new Prefix(4, " 4 underlined, single"),
			new Prefix(21, "21 undeline double")
	};
	final static Integer[] colors = {
			30, // normal
			40, // background
			90, // high intensity normal
			100 // high intensity background
	};

	static void test16() {
		int color;
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("==== ATTRIBUTES ====\n"));
		sb.append(String.format("     %-28s", "Changing fg, default bg"));
		sb.append(String.format("%-28s", "Default fg, changing bg"));
		sb.append(String.format("%-28s", "High fg, default bg"));
		sb.append(String.format("%s%n", "Default fg, high bg"));
		for (Prefix prefix : prefixes) {
			sb.append(String.format("%03d: ", prefix.val));
			for (Integer command : colors) {
				for (color = 0; color <= 7; color++)
					sb.append(String.format(CSI + "%d;%dm{x}", prefix.val, command + color));
				sb.append(RESET + "    ");
			}
			sb.append(String.format(RESET + " // %s%n", prefix.desc));
			print(sb);
		}
		System.out.println(RESET);
	}

	private static void print(StringBuilder sb) {
		System.out.print(sb.toString());
		sb.setLength(0);
	}

	static void test16matrix() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("==== 16 COLOR MATRIX ====\n"));
		sb.append(String.format("     %-28s", "Normal fg, Normal bg"));
		sb.append(String.format("%-28s", "Normal fg, high bg"));
		sb.append(String.format("%-28s", "High fg, default bg"));
		sb.append(String.format("%s%n", "High fg, high bg"));
		for (int fg = 0; fg < 8; fg++) {
			sb.append(String.format("%03d: ", fg));
			for (int bg = 0; bg < 8; bg++)
				sb.append(String.format(CSI + "%d;%dm{x}", 30 + fg, 40 + bg));
			sb.append(RESET + "    ");
			for (int bg = 0; bg < 8; bg++)
				sb.append(String.format(CSI + "%d;%dm{x}", 30 + fg, 100 + bg));
			sb.append(RESET + "    ");
			for (int bg = 0; bg < 8; bg++)
				sb.append(String.format(CSI + "%d;%dm{x}", 90 + fg, 40 + bg));
			sb.append(RESET + "    ");
			for (int bg = 0; bg < 8; bg++)
				sb.append(String.format(CSI + "%d;%dm{x}", 90 + fg, 100 + bg));
			print(sb);
			System.out.println(RESET);
		}
		System.out.println(RESET);
	}

	/* Java port of 256colors2.pl, http://www.frexx.de/xterm-256-notes */
	static void test256() {
		final StringBuilder sb = new StringBuilder();
		int color;
		// first the system ones:
		sb.append("===== 256 COLORS =====\n");
		sb.append("System colors:\n");
		for (color = 0; color < 16; color++)
			sb.append(String.format(CSI + "38;5;%dm{x}", color));
		print(sb);
		System.out.println(RESET);
		for (color = 0; color < 16; color++)
			sb.append(String.format(CSI + "48;5;%dm{x}", color));
		print(sb);
		System.out.println(RESET);

		// now the color cube
		sb.append("Color cube, 6x6x6:\n");
		for (int green = 0; green < 6; green++) {
			for (int red = 0; red < 6; red++) {
				for (int blue = 0; blue < 6; blue++) {
					color = 16 + (red * 36) + (green * 6) + blue;
					sb.append(String.format(CSI + "38;5;%dm{x}", color));
				}
				sb.append(RESET + " ");
			}
			sb.append("\n");
			print(sb);
		}
		for (int green = 0; green < 6; green++) {
			for (int red = 0; red < 6; red++) {
				for (int blue = 0; blue < 6; blue++) {
					color = 16 + (red * 36) + (green * 6) + blue;
					sb.append(String.format(CSI + "48;5;%dm   ", color));
				}
				sb.append(RESET + " ");
			}
			sb.append("\n");
			print(sb);
		}

		// now the grayscale ramp
		sb.append("Grayscale ramp:\n");
		for (color = 232; color < 256; color++)
			sb.append(String.format(CSI + "38;5;%dm{x}", color));
		print(sb);
		System.out.println(RESET);
		for (color = 232; color < 256; color++)
			sb.append(String.format(CSI + "48;5;%dm   ", color));

		print(sb);
		System.out.println(RESET + "\n");
	}

	public static void main(String[] args) {
		test16();
		test16matrix();
		test256();
	}
}
