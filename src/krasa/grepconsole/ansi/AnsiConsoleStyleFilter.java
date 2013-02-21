package krasa.grepconsole.ansi;

import static krasa.grepconsole.ansi.utils.AnsiCommands.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.grepconsole.ansi.utils.AnsiConsoleAttributes;
import krasa.grepconsole.model.Profile;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Pair;

public class AnsiConsoleStyleFilter {
	private final static Pattern pattern = Pattern.compile("\u001b\\[[\\d;]*m");
	private final static Map<String, ConsoleViewContentType> cache = new HashMap<String, ConsoleViewContentType>();
	protected AnsiConsoleAttributes lastConsoleAttributes;
	private Profile profile;

	public AnsiConsoleStyleFilter(Profile profile) {
		this.profile = profile;
	}

	private static int tryParseInteger(String text) {
		if ("".equals(text))
			return 0;

		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private AnsiConsoleAttributes interpretCommand(String cmd, AnsiConsoleAttributes currentAttributes) {
		if (currentAttributes == null) {
			currentAttributes = new AnsiConsoleAttributes();
		}

		int nCmd = tryParseInteger(cmd);

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
				currentAttributes.currentFgColor = Integer.valueOf(nCmd - COMMAND_COLOR_FOREGROUND_FIRST);
			else if (nCmd >= COMMAND_COLOR_BACKGROUND_FIRST && nCmd <= COMMAND_COLOR_BACKGROUND_LAST) // background
																										// color
				currentAttributes.currentBgColor = Integer.valueOf(nCmd - COMMAND_COLOR_BACKGROUND_FIRST);
		}

		return currentAttributes;
	}

	private void addRange(ArrayList<Pair<String, ConsoleViewContentType>> ranges, final String substring,
			final ConsoleViewContentType contentType) {
		ranges.add(new Pair<String, ConsoleViewContentType>(substring, contentType));
	}

	private ConsoleViewContentType getContentType(AnsiConsoleAttributes consoleAttributes) {
		if (consoleAttributes == null) {
			return null;
		}
		String key = consoleAttributes.toString();
		ConsoleViewContentType contentType = null;
		// contentType = cache.get(key);
		if (contentType == null) {
			TextAttributes textAttributes = new TextAttributes();
			consoleAttributes.updateRangeStyle(textAttributes);
			contentType = new ConsoleViewContentType(key, textAttributes);
			cache.put(key, contentType);
		}
		return contentType;
	}

	// it is executed with one thread per output stream
	public List<Pair<String, ConsoleViewContentType>> process(String currentText,
			ConsoleViewContentType consoleViewContentType) {
		if ((currentText == null) || (currentText.length() == 0))
			return null;
		ArrayList<Pair<String, ConsoleViewContentType>> ranges = new ArrayList<Pair<String, ConsoleViewContentType>>();

		int lastRangeEnd = 0;
		Matcher matcher = pattern.matcher(currentText);

		while (matcher.find()) {
			AnsiConsoleAttributes consoleAttributes = null;
			// next line should continue to use previous style
			if (lastConsoleAttributes != null) {
				consoleAttributes = lastConsoleAttributes.clone();
			}
			int start = matcher.start();
			int end = matcher.end();

			// first match - add previous text if there is any
			if (isFirstMatch(lastRangeEnd, start)) {
				ConsoleViewContentType lastType = getContentType(lastConsoleAttributes);
				if (lastType == null) {
					lastType = consoleViewContentType;
				}
				String substring = currentText.substring(0, start);
				ranges.add(new Pair<String, ConsoleViewContentType>(substring, lastType));
			}

			String theEscape = currentText.substring(matcher.start() + 2, matcher.end() - 1);
			for (String cmd : theEscape.split(";")) {
				consoleAttributes = interpretCommand(cmd, consoleAttributes);
			}
			if (profile.isPrintAnsi()) {
				String substring = currentText.substring(start, end);
				addRange(ranges, substring, ConsoleViewContentType.SYSTEM_OUTPUT);
			}

			// we do it in the second+ round, so that we know where the end of text to highlight is.
			if (!isFirstMatch(lastRangeEnd, start) && start != 0) {
				String substring = currentText.substring(lastRangeEnd, start);
				addRange(ranges, substring, getContentType(lastConsoleAttributes));
			}

			lastConsoleAttributes = consoleAttributes;
			lastRangeEnd = end;
		}

		// if there was matches previously, add the rest of text, if there is any
		if (lastConsoleAttributes != null && (lastRangeEnd != currentText.length() || lastRangeEnd == 0)) {
			// 'Process finished with exit code', or just one '\n'
			if (currentText.startsWith("\n")) {
				addRange(ranges, currentText, consoleViewContentType);
				return null;
			}

			boolean endsWithLineEnd = currentText.endsWith("\n");

			int endIndex = endsWithLineEnd ? currentText.length() - 1 : currentText.length();
			String substring = currentText.substring(lastRangeEnd, endIndex);
			if (substring.length() != 0) {
				addRange(ranges, substring, getContentType(lastConsoleAttributes));
			}
			if (endsWithLineEnd) {
				addRange(ranges, "\n", consoleViewContentType);
			}
		}

		return ranges;
	}

	private boolean isFirstMatch(int lastRangeEnd, int start) {
		return start != 0 && lastRangeEnd == 0;
	}

}
