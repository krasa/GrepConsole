package krasa.grepconsole.grep;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

import com.intellij.execution.ui.ConsoleViewContentType;

public abstract class GrepCopyingListener {

	private Pattern expression;
	private Pattern unlessExpression;

	public GrepCopyingListener(String expression) {
		try {
			this.expression = Pattern.compile(expression, computeFlags(false));
		} catch (PatternSyntaxException ex) {
		}
	}

	abstract public void process(String s, ConsoleViewContentType entireLength);

	public boolean set(boolean caseSensitive, String expression, String unlessExpression) {
		try {
			this.expression = Pattern.compile(expression, computeFlags(caseSensitive));
			this.unlessExpression = Pattern.compile(unlessExpression, computeFlags(caseSensitive));
		} catch (PatternSyntaxException ex) {
			return false;
		}
		return true;
	}

	public boolean matches(String s) {
		if (!StringUtils.isEmpty(s)) {
			if (matchesPattern(expression, s) && !matchesPattern(unlessExpression, s)) {
				return true;
			}
		}
		return false;
	}

	private boolean matchesPattern(Pattern pattern, String matchedLine) {
		boolean matches = false;
		if (pattern != null) {
			if (matchedLine.endsWith("\n")) {
				matchedLine = matchedLine.substring(0, matchedLine.length() - 1);
			}
			matches = pattern.matcher(matchedLine).matches();
		}
		return matches;
	}

	private int computeFlags(boolean caseInsensitive) {
		return caseInsensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	}
}
