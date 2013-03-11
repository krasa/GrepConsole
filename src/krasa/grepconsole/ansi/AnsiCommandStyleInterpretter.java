package krasa.grepconsole.ansi;

import static krasa.grepconsole.ansi.utils.AnsiCommands.*;
import krasa.grepconsole.ansi.utils.AnsiConsoleAttributes;

public class AnsiCommandStyleInterpretter {
	public static void interpretStyle(AnsiConsoleAttributes currentAttributes, int nCmd) {
		switch (nCmd) {
		case COMMAND_ATTR_RESET:
			currentAttributes.reset();
			break;

		case COMMAND_ATTR_INTENSITY_BRIGHT:
			currentAttributes.bold = true;
			break;
		case COMMAND_ATTR_INTENSITY_FAINT:
			currentAttributes.bold = false;
			break;
		case COMMAND_ATTR_INTENSITY_NORMAL:
			currentAttributes.bold = false;
			break;

		case COMMAND_ATTR_ITALIC:
			currentAttributes.italic = true;
			break;
		case COMMAND_ATTR_ITALIC_OFF:
			currentAttributes.italic = false;
			break;

		case COMMAND_ATTR_UNDERLINE:
			currentAttributes.underline = true;
			break;
		case COMMAND_ATTR_UNDERLINE_OFF:
			currentAttributes.underline = false;
			break;

		case COMMAND_ATTR_NEGATIVE_ON:
			currentAttributes.invert = true;
			break;
		case COMMAND_ATTR_NEGATIVE_Off:
			currentAttributes.invert = false;
			break;

		case COMMAND_COLOR_FOREGROUND_RESET:
			currentAttributes.currentFgColor = null;
			break;
		case COMMAND_COLOR_BACKGROUND_RESET:
			currentAttributes.currentBgColor = null;
			break;

		default:
			if (nCmd >= COMMAND_COLOR_FOREGROUND_FIRST && nCmd <= COMMAND_COLOR_FOREGROUND_LAST) // text color
				currentAttributes.currentFgColor = nCmd - COMMAND_COLOR_FOREGROUND_FIRST;
			else if (nCmd >= COMMAND_COLOR_BACKGROUND_FIRST && nCmd <= COMMAND_COLOR_BACKGROUND_LAST) // background
																										// color
				currentAttributes.currentBgColor = nCmd - COMMAND_COLOR_BACKGROUND_FIRST;
		}
	}
}
