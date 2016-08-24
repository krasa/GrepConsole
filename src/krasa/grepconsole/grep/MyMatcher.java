package krasa.grepconsole.grep;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class MyMatcher {
	private final Pattern expressionPattern;
	private final Pattern unlessExpressionPattern;
	private final boolean wholeLine;

	public MyMatcher(Pattern expressionPattern, Pattern unlessExpressionPattern, boolean wholeLine) {
		this.expressionPattern = expressionPattern;
		this.unlessExpressionPattern = unlessExpressionPattern;
		this.wholeLine = wholeLine;
	}

	public boolean matches(String s) {
		if (!StringUtils.isEmpty(s)) {
			if (matchesPattern(expressionPattern, s) && !matchesPattern(unlessExpressionPattern, s)) {
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
			if (wholeLine) {
				matches = pattern.matcher(matchedLine).matches();
			} else {
				matches = pattern.matcher(matchedLine).find();
			}
		}
		return matches;
	}

}
