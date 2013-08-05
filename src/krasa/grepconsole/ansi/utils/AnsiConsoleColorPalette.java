package krasa.grepconsole.ansi.utils;

import java.awt.*;

public class AnsiConsoleColorPalette {
	public static final String PALETTE_VGA = "paletteVGA";
	public static final String PALETTE_WINXP = "paletteXP";
	public static final String PALETTE_MAC = "paletteMac";
	public static final String PALETTE_PUTTY = "palettePuTTY";
	public static final String PALETTE_XTERM = "paletteXTerm";

	// From Wikipedia, http://en.wikipedia.org/wiki/ANSI_escape_code
	private final static Color[] paletteVGA = {new Color(0, 0, 0), // black
			new Color(170, 0, 0), // red
			new Color(0, 170, 0), // green
			new Color(170, 85, 0), // brown/yellow
			new Color(0, 0, 170), // blue
			new Color(170, 0, 170), // magenta
			new Color(0, 170, 170), // cyan
			new Color(170, 170, 170), // gray
			new Color(85, 85, 85), // dark gray
			new Color(255, 85, 85), // bright red
			new Color(85, 255, 85), // bright green
			new Color(255, 255, 85), // yellow
			new Color(85, 85, 255), // bright blue
			new Color(255, 85, 255), // bright magenta
			new Color(85, 255, 255), // bright cyan
			new Color(255, 255, 255) // white
	};
	private final static Color[] paletteXP = {new Color(0, 0, 0), // black
			new Color(128, 0, 0), // red
			new Color(0, 128, 0), // green
			new Color(128, 128, 0), // brown/yellow
			new Color(0, 0, 128), // blue
			new Color(128, 0, 128), // magenta
			new Color(0, 128, 128), // cyan
			new Color(192, 192, 192), // gray
			new Color(128, 128, 128), // dark gray
			new Color(255, 0, 0), // bright red
			new Color(0, 255, 0), // bright green
			new Color(255, 255, 0), // yellow
			new Color(0, 0, 255), // bright blue
			new Color(255, 0, 255), // bright magenta
			new Color(0, 255, 255), // bright cyan
			new Color(255, 255, 255) // white
	};
	private final static Color[] paletteMac = {new Color(0, 0, 0), // black
			new Color(194, 54, 33), // red
			new Color(37, 188, 36), // green
			new Color(173, 173, 39), // brown/yellow
			new Color(73, 46, 225), // blue
			new Color(211, 56, 211), // magenta
			new Color(51, 187, 200), // cyan
			new Color(203, 204, 205), // gray
			new Color(129, 131, 131), // dark gray
			new Color(252, 57, 31), // bright red
			new Color(49, 231, 34), // bright green
			new Color(234, 236, 35), // yellow
			new Color(88, 51, 255), // bright blue
			new Color(249, 53, 248), // bright magenta
			new Color(20, 240, 240), // bright cyan
			new Color(233, 235, 235) // white
	};
	private final static Color[] palettePuTTY = {new Color(0, 0, 0), // black
			new Color(187, 0, 0), // red
			new Color(0, 187, 0), // green
			new Color(187, 187, 0), // brown/yellow
			new Color(0, 0, 187), // blue
			new Color(187, 0, 187), // magenta
			new Color(0, 187, 187), // cyan
			new Color(187, 187, 187), // gray
			new Color(85, 85, 85), // dark gray
			new Color(255, 85, 85), // bright red
			new Color(85, 255, 85), // bright green
			new Color(255, 255, 85), // yellow
			new Color(85, 85, 255), // bright blue
			new Color(255, 85, 255), // bright magenta
			new Color(85, 255, 255), // bright cyan
			new Color(255, 255, 255) // white
	};
	private final static Color[] paletteXTerm = {new Color(0, 0, 0), // black
			new Color(205, 0, 0), // red
			new Color(0, 205, 0), // green
			new Color(205, 205, 0), // brown/yellow
			new Color(0, 0, 238), // blue
			new Color(205, 0, 205), // magenta
			new Color(0, 205, 205), // cyan
			new Color(229, 229, 229), // gray
			new Color(127, 127, 127), // dark gray
			new Color(255, 0, 0), // bright red
			new Color(0, 255, 0), // bright green
			new Color(255, 255, 0), // yellow
			new Color(92, 92, 255), // bright blue
			new Color(255, 0, 255), // bright magenta
			new Color(0, 255, 255), // bright cyan
			new Color(255, 255, 255) // white
	};
	private static Color[] palette = paletteXP;
	private static String currentPaletteName = PALETTE_WINXP;

	public static Color getColor(int index) {
		if (index < 0 || index > palette.length)
			return new Color(0x00, 0x00, 0x00);

		return palette[index];
	}

	public static String getPalette() {
		return currentPaletteName;
	}

	public static void setPalette(String paletteName) {
		currentPaletteName = paletteName;
		if ("paletteVGA".equalsIgnoreCase(paletteName))
			palette = paletteVGA;
		else if ("paletteXP".equalsIgnoreCase(paletteName))
			palette = paletteXP;
		else if ("paletteIOS".equalsIgnoreCase(paletteName))
			palette = paletteMac;
		else if ("palettePuTTY".equalsIgnoreCase(paletteName))
			palette = palettePuTTY;
		else if ("paletteXTerm".equalsIgnoreCase(paletteName))
			palette = paletteXTerm;
		else {
			String os = System.getProperty("os.name");
			if (os == null || os.startsWith("Windows"))
				setPalette(PALETTE_WINXP);
			else if (os.startsWith("Mac"))
				setPalette(PALETTE_MAC);
			else
				setPalette(PALETTE_XTERM);
		}
	}

}
