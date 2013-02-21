package krasa.grepconsole.service.ansi.utils;


import static krasa.grepconsole.service.ansi.utils.AnsiCommands.COMMAND_COLOR_INTENSITY_DELTA;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class AnsiConsoleAttributes implements Cloneable {
	public Integer currentBgColor;
	public Integer currentFgColor;
	public boolean underline;
	public boolean bold;
	public boolean italic;
	public boolean invert;

	public AnsiConsoleAttributes() {
		reset();
	}

	public void reset() {
		currentBgColor = null;
		currentFgColor = null;
		underline = false;
		bold = false;
		italic = false;
		invert = false;
	}

	@Override
	public AnsiConsoleAttributes clone() {
		AnsiConsoleAttributes result = new AnsiConsoleAttributes();
		result.currentBgColor = currentBgColor;
		result.currentFgColor = currentFgColor;
		result.underline = underline;
		result.bold = bold;
		result.italic = italic;
		result.invert = invert;
		return result;
	}

	public static Color hiliteColor(Color c) {
		if (c == null)
			return new Color(0xff, 0xff, 0xff);
		int red = c.getRed() * 2;
		int green = c.getGreen() * 2;
		int blue = c.getBlue() * 2;

		if (red > 0xff) red = 0xff;
		if (green > 0xff) green = 0xff;
		if (blue > 0xff) blue = 0xff;

		return new Color(red, green, blue); // here
	}

	public Color getBgColor() {
		if (currentBgColor == null)
			return null;
		boolean useWindowsMapping = true;
		if (useWindowsMapping && underline)
			return AnsiConsoleColorPalette.getColor(currentBgColor.intValue() + COMMAND_COLOR_INTENSITY_DELTA);
		else
			return AnsiConsoleColorPalette.getColor(currentBgColor.intValue());
	}

	public Color getFgColor() {
		if (currentFgColor == null)
			return null;

		boolean useWindowsMapping = true;
		if (useWindowsMapping && bold)
			return AnsiConsoleColorPalette.getColor(currentFgColor.intValue() + COMMAND_COLOR_INTENSITY_DELTA);
		else
			return AnsiConsoleColorPalette.getColor(currentFgColor.intValue());
	}

	public void updateRangeStyle(final TextAttributes textAttributes, final boolean useWindowsMapping) {


		Color color = getFgColor();
		if (color != null)
			textAttributes.setForegroundColor(color);
		else if (useWindowsMapping && bold) {
			textAttributes.setForegroundColor(hiliteColor(textAttributes.getForegroundColor()));
		}

		color = getBgColor();
		if (color != null)
			textAttributes.setBackgroundColor(color);
		else if (useWindowsMapping && underline) {
			textAttributes.setBackgroundColor(hiliteColor(color));
		}

		if (!useWindowsMapping) {
			textAttributes.setEffectType(EffectType.LINE_UNDERSCORE);
		}
		if (bold && !useWindowsMapping)
			textAttributes.setFontType(Font.BOLD);
		if (italic)
			textAttributes.setFontType(Font.ITALIC);


		if (invert) {
			Color tmp = textAttributes.getBackgroundColor();
			textAttributes.setBackgroundColor(textAttributes.getForegroundColor());
			textAttributes.setForegroundColor(tmp);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AnsiConsoleAttributes that = (AnsiConsoleAttributes) o;

		if (bold != that.bold) return false;
		if (invert != that.invert) return false;
		if (italic != that.italic) return false;
		if (underline != that.underline) return false;
		if (!currentBgColor.equals(that.currentBgColor)) return false;
		if (!currentFgColor.equals(that.currentFgColor)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = currentBgColor.hashCode();
		result = 31 * result + currentFgColor.hashCode();
		result = 31 * result + (underline ? 1 : 0);
		result = 31 * result + (bold ? 1 : 0);
		result = 31 * result + (italic ? 1 : 0);
		result = 31 * result + (invert ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AnsiConsoleAttributes{");
		sb.append("currentBgColor=").append(currentBgColor);
		sb.append(", currentFgColor=").append(currentFgColor);
		sb.append(", underline=").append(underline);
		sb.append(", bold=").append(bold);
		sb.append(", italic=").append(italic);
		sb.append(", invert=").append(invert);
		sb.append('}');
		return sb.toString();
	}
}
