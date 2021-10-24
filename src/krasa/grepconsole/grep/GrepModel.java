package krasa.grepconsole.grep;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

public class GrepModel {
	private boolean caseSensitive;
	private boolean wholeLine;
	private boolean wholeWords;
	private String expression;
	private boolean regex;
	private boolean exclude;

	private volatile GrepModel.Matcher matcher;

	public GrepModel() {
	}

	public GrepModel(String expression) {
		this.expression = expression;
	}

	public GrepModel(boolean caseSensitive, boolean wholeWords, boolean regex, String expression, boolean exclude) {
		this.caseSensitive = caseSensitive;
		this.wholeWords = wholeWords;
		this.expression = expression;
		this.regex = regex;
		this.exclude = exclude;
	}

	public boolean isWholeWords() {
		return wholeWords;
	}

	public void setWholeWords(boolean wholeWords) {
		this.wholeWords = wholeWords;
	}

	public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	public Matcher matcher() {
		Pattern expressionPattern = null;
		if (!StringUtils.isBlank(expression)) {
			String expression = this.expression;

			if (!regex) {
				expression = Pattern.quote(expression);
			}

			if (wholeWords) {
				expression = "\\b" + expression + "\\b";
			}

			expressionPattern = Pattern.compile(expression, computeFlags());
		}
		return new Matcher(expressionPattern, wholeLine);
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public void setWholeLine(boolean wholeLine) {
		this.wholeLine = wholeLine;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setRegex(boolean regex) {
		this.regex = regex;
	}

	public String getExpression() {
		return expression;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public boolean isWholeLine() {
		return wholeLine;
	}

	public boolean isRegex() {
		return regex;
	}

	private int computeFlags() {
		int i = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		return i;
	}

	boolean matches(CharSequence charSequence) {
		if (matcher == null) {
			matcher = matcher();
		}
		return matcher.matches(charSequence);
	}

	public static class Matcher {
		private final Pattern expressionPattern;
		private final boolean wholeLine;

		public Matcher(Pattern expressionPattern, boolean wholeLine) {
			this.expressionPattern = expressionPattern;
			this.wholeLine = wholeLine;
		}

		public boolean matches(CharSequence s) {
			if (matchesPattern(expressionPattern, s)) {
				return true;
			}
			return false;
		}

		private boolean matchesPattern(Pattern pattern, CharSequence matchedLine) {
			boolean matches = false;
			if (pattern != null) {
				if (wholeLine) {
					matches = pattern.matcher(matchedLine).matches();
				} else {
					matches = pattern.matcher(matchedLine).find();
				}
			}
			return matches;
		}

		@Override
		public String toString() {
			return "[" +
					"expressionPattern=" + expressionPattern +
					", wholeLine=" + wholeLine +
					']';
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrepModel grepModel = (GrepModel) o;
		return caseSensitive == grepModel.caseSensitive && wholeLine == grepModel.wholeLine && wholeWords == grepModel.wholeWords && regex == grepModel.regex && exclude == grepModel.exclude && Objects.equals(expression, grepModel.expression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(caseSensitive, wholeLine, wholeWords, expression, regex, exclude);
	}

	@Override
	public String toString() {
		return "GrepModel{" +
				"caseSensitive=" + caseSensitive +
				", wholeLine=" + wholeLine +
				", expression='" + expression + '\'' +
				", regex=" + regex +
				'}';
	}
}
