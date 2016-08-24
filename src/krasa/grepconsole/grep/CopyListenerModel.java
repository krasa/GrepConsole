package krasa.grepconsole.grep;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class CopyListenerModel {
	private final boolean caseSensitive;
	private final boolean wholeLine;
	private final String expression;
	private final String unlessExpression;
	private final boolean regex;

	public CopyListenerModel(boolean caseSensitive, boolean wholeLine, boolean regex, String expression,
			String unlessExpression) {
		this.caseSensitive = caseSensitive;
		this.wholeLine = wholeLine;
		this.expression = expression;
		this.unlessExpression = unlessExpression;
		this.regex = regex;
	}

	public MyMatcher matcher() {
		Pattern unlessExpressionPattern = null;
		Pattern expressionPattern = null;
		if (!StringUtils.isBlank(expression)) {
			expressionPattern = Pattern.compile(expression, computeFlags());
		}
		if (!StringUtils.isBlank(unlessExpression)) {
			unlessExpressionPattern = Pattern.compile(unlessExpression, computeFlags());
		}
		return new MyMatcher(expressionPattern, unlessExpressionPattern, wholeLine);
	}

	public String getExpression() {
		return expression;
	}

	private int computeFlags() {
		int i = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		int j = regex ? 0 : Pattern.LITERAL;
		return i | j;
	}

}
