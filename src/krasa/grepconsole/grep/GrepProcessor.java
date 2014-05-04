package krasa.grepconsole.grep;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.grepconsole.model.GrepExpressionItem;

import org.apache.commons.lang.StringUtils;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.diagnostic.Logger;

public class GrepProcessor {
	private static final Logger log = Logger.getInstance(GrepProcessor.class.getName());

	private GrepExpressionItem grepExpressionItem;

	public GrepProcessor(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
	}

	public FilterState process(FilterState state) {
		if (grepExpressionItem.isEnabled() && !StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			String matchedLine = state.getText();
			if (grepExpressionItem.isHighlightOnlyMatchingText()) {
				Pattern pattern = grepExpressionItem.getPattern();
				if (pattern != null) {
					final Matcher matcher = pattern.matcher(matchedLine);
					while (matcher.find()) {
						final int start = matcher.start();
						final int end = matcher.end();
						state.setNextOperation(grepExpressionItem.getOperationOnMatch());
						state.setExclude(grepExpressionItem.isInputFilter());
						state.setMatchesSomething(true);
						state.add(new Filter.ResultItem(state.getOffset() + start, state.getOffset() + end, null, grepExpressionItem.getConsoleViewContentType(null).getAttributes()));
					}
				}
			} else if (matches(matchedLine) && !matchesUnless(matchedLine)) {
				state.setNextOperation(grepExpressionItem.getOperationOnMatch());
				state.setConsoleViewContentType(grepExpressionItem.getConsoleViewContentType(state.getConsoleViewContentType()));
				state.setExclude(grepExpressionItem.isInputFilter());
				state.setMatchesSomething(true);
				if (grepExpressionItem.getSound().isEnabled()) {
					grepExpressionItem.getSound().play();
				}
			}
		}
		return state;
	}

	private boolean matches(String matchedLine) {
		Pattern pattern = grepExpressionItem.getPattern();
		boolean matches = false;
		if (pattern != null) {
			matches = pattern.matcher(matchedLine).matches();
		}
		return matches;
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
