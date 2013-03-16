package krasa.grepconsole.ansi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.grepconsole.ansi.utils.AnsiConsoleAttributes;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.service.ConsoleListener;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Pair;

public class AnsiConsoleStyleFilter {

	private final static Pattern pattern = Pattern.compile("\u001b\\[[\\d;]*\\p{Alpha}");
	private final static Map<String, ConsoleViewContentType> cache = new HashMap<String, ConsoleViewContentType>();
	protected AnsiConsoleAttributes lastConsoleAttributes;
	private volatile Profile profile;
	private ConsoleListener ansiFilterService;

	public AnsiConsoleStyleFilter(Profile profile) {
		this.profile = profile;
	}

	private ConsoleViewContentType getContentType(AnsiConsoleAttributes consoleAttributes,
			ConsoleViewContentType inputType) {
		if (!profile.isEnableAnsiColoring()) {
			return inputType;
		}
		if (consoleAttributes == null) {
			return null;
		}
		String key = consoleAttributes.toString();
		ConsoleViewContentType contentType = cache.get(key);
		if (contentType == null) {
			TextAttributes textAttributes = new TextAttributes();
			consoleAttributes.updateRangeStyle(textAttributes);
			contentType = new ConsoleViewContentType(key, textAttributes);
			cache.put(key, contentType);
		}
		return contentType;
	}

	// it is executed with one thread per output stream
	@Nullable
	public List<Pair<String, ConsoleViewContentType>> process(String currentText,
			ConsoleViewContentType consoleViewContentType) {
		if (StringUtils.isEmpty(currentText)) {
			return null;
		}
		List<Pair<String, ConsoleViewContentType>> ranges = new ArrayList<Pair<String, ConsoleViewContentType>>();

		int previousRangeEnd = 0;
		Matcher matcher = getMatcher(currentText);

		while (matcher.find()) {
			AnsiConsoleAttributes consoleAttributes = null;
			// next line should continue to use previous style
			if (lastConsoleAttributes != null) {
				consoleAttributes = lastConsoleAttributes.clone();
			}

			// add previous text if there is any
			addTextOnFirstMatchCase(currentText, consoleViewContentType, ranges, matcher);

			consoleAttributes = interpretAsciCommand(currentText, ranges, matcher, consoleAttributes);

			printAsciCommandIfEnabled(currentText, ranges, matcher);

			// we do it in the second+ round, so that we know where the end of text to highlight is.
			addRangeIfNotFirstMatch(currentText, consoleViewContentType, ranges, previousRangeEnd, matcher);
			// set it after print!
			lastConsoleAttributes = consoleAttributes;
			previousRangeEnd = matcher.end();
		}

		return addRestOfTextOrReturnNothing(currentText, consoleViewContentType, ranges, previousRangeEnd);
	}

	private List<Pair<String, ConsoleViewContentType>> addRestOfTextOrReturnNothing(String currentText,
			ConsoleViewContentType consoleViewContentType, List<Pair<String, ConsoleViewContentType>> ranges,
			int previousRangeEnd) {
		// if there was matches previously, add the rest of text, if there is any
		if (lastConsoleAttributes != null && (previousRangeEnd != currentText.length() || previousRangeEnd == 0)) {
			if (isIntellijGeneratedLastLine(currentText, consoleViewContentType, ranges))
				return null;
			addRestOfText(currentText, consoleViewContentType, ranges, previousRangeEnd);
		}
		return ranges;
	}

	private void addRestOfText(String currentText, ConsoleViewContentType consoleViewContentType,
			List<Pair<String, ConsoleViewContentType>> ranges, int previousRangeEnd) {
		boolean endsWithLineEnd = currentText.endsWith("\n");

		String substring = getStringWithoutLineEnd(currentText, previousRangeEnd, endsWithLineEnd);
		if (substring.length() != 0) {
			addRange(ranges, substring, getContentType(lastConsoleAttributes, consoleViewContentType));
		}
		if (endsWithLineEnd) {
			addRange(ranges, "\n", consoleViewContentType);
		}
	}

	private String getStringWithoutLineEnd(String currentText, int previousRangeEnd, boolean endsWithLineEnd) {
		int endIndex = endsWithLineEnd ? currentText.length() - 1 : currentText.length();
		return currentText.substring(previousRangeEnd, endIndex);
	}

	private boolean isIntellijGeneratedLastLine(String currentText, ConsoleViewContentType consoleViewContentType,
			List<Pair<String, ConsoleViewContentType>> ranges) {
		// 'Process finished with exit code', or just one '\n'
		if (currentText.startsWith("\n")) {
			addRange(ranges, currentText, consoleViewContentType);
			return true;
		}
		return false;
	}

	private void addRangeIfNotFirstMatch(String currentText, ConsoleViewContentType consoleViewContentType,
			List<Pair<String, ConsoleViewContentType>> ranges, int previousRangeEnd, Matcher matcher) {
		if (!isFirstMatch(previousRangeEnd, matcher.start()) && matcher.start() != 0) {
			String substring = currentText.substring(previousRangeEnd, matcher.start());
			addRange(ranges, substring, getContentType(lastConsoleAttributes, consoleViewContentType));
		}
	}

	private void printAsciCommandIfEnabled(String currentText, List<Pair<String, ConsoleViewContentType>> ranges,
			Matcher matcher) {
		if (!profile.isHideAnsiCommands()) {
			String substring = currentText.substring(matcher.start(), matcher.end());
			addRange(ranges, substring, ConsoleViewContentType.SYSTEM_OUTPUT);
		}
	}

	private void addRange(List<Pair<String, ConsoleViewContentType>> ranges, final String substring,
			final ConsoleViewContentType contentType) {
		ranges.add(new Pair<String, ConsoleViewContentType>(substring, contentType));
	}

	private void addTextOnFirstMatchCase(String currentText, ConsoleViewContentType consoleViewContentType,
			List<Pair<String, ConsoleViewContentType>> ranges, Matcher matcher) {
		int start = matcher.start();
		if (isFirstMatch(matcher.end(), start)) {
			ConsoleViewContentType lastType = getContentType(lastConsoleAttributes, consoleViewContentType);
			if (lastType == null) {
				lastType = consoleViewContentType;
			}
			String substring = currentText.substring(0, start);
			ranges.add(new Pair<String, ConsoleViewContentType>(substring, lastType));
		}
	}

	private AnsiConsoleAttributes interpretAsciCommand(String currentText,
			List<Pair<String, ConsoleViewContentType>> ranges, Matcher matcher, AnsiConsoleAttributes consoleAttributes) {
		if (consoleAttributes == null) {
			consoleAttributes = new AnsiConsoleAttributes();
		}

		String theEscape = currentText.substring(matcher.start() + 2, matcher.end() - 1);
		String kind = currentText.substring(matcher.end() - 1, matcher.end());

		for (String cmd : theEscape.split(";")) {
			int nCmd = Utils.tryParseInteger(cmd);
			if ("J".equals(kind) && profile.isHideAnsiCommands()) {
				switch (nCmd) {
				case 2:
					ansiFilterService.clearConsole();
					ranges.clear();
					break;
				}
				return consoleAttributes;
			} else if ("m".equals(kind)) {
				AnsiCommandStyleInterpretter.interpretStyle(consoleAttributes, nCmd);
			}
		}
		return consoleAttributes;
	}

	protected Matcher getMatcher(String currentText) {
		return pattern.matcher(currentText);
	}

	private boolean isFirstMatch(int lastRangeEnd, int start) {
		return start != 0 && lastRangeEnd == 0;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public void addListener(ConsoleListener consoleListener) {
		this.ansiFilterService = consoleListener;
	}
}
