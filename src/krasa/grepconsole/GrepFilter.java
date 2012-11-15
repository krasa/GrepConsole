package krasa.grepconsole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.grepconsole.model.GrepExpressionItem;

import org.apache.commons.lang.StringUtils;

public class GrepFilter {

	private GrepExpressionItem grepExpressionItem;

	public GrepFilter(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
	}

	public FilterState process(FilterState flow) {
		if (!StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			String matchedLine = flow.getLine();

			if (matches(matchedLine) && !matchesUnless(matchedLine)) {
				flow.setTextAttributes(grepExpressionItem.getTextAttributes());
				flow.setNextOperation(grepExpressionItem.getOperationOnMatch());
			}
		}
		return flow;
	}

	private boolean matches(String matchedLine) {
		return grepExpressionItem.getPattern().matcher(matchedLine).matches();
	}

	private boolean matchesUnless(String matchedLine) {
		boolean matchUnless = false;
		Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
		if (unlessPattern != null) {
			Matcher unlessMatcher = unlessPattern.matcher(matchedLine);
			if (unlessMatcher.matches()) {
				matchUnless = true;
			}
		}
		return matchUnless;
	}
}
