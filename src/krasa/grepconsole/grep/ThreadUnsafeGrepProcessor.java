package krasa.grepconsole.grep;

import java.util.regex.*;

import krasa.grepconsole.model.GrepExpressionItem;

import org.apache.commons.lang.StringUtils;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.diagnostic.Logger;

public class ThreadUnsafeGrepProcessor implements GrepProcessor {
	private static final Logger log = Logger.getInstance(ThreadUnsafeGrepProcessor.class.getName());
	protected Matcher patternMatcher;
	protected Matcher unlessMatcher;
	private GrepExpressionItem grepExpressionItem;
	private int matches;

	public ThreadUnsafeGrepProcessor(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
		Pattern pattern = grepExpressionItem.getPattern();
		if (pattern != null) {
			patternMatcher = pattern.matcher("");
		}
		Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
		if (unlessPattern != null) {
			unlessMatcher = unlessPattern.matcher("");
		}
	}

	@Override
	public GrepExpressionItem getGrepExpressionItem() {
		return grepExpressionItem;
	}

	@Override
	public int getMatches() {
		return matches;
	}

	@Override
	public void resetMatches() {
		this.matches = 0;
	}

	@Override
	public FilterState process(FilterState state) {
		if (grepExpressionItem.isEnabled() && !StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			String matchedLine = state.getText();
			if (grepExpressionItem.isHighlightOnlyMatchingText()) {
				if (patternMatcher != null) {
					patternMatcher.reset(matchedLine);
					while (patternMatcher.find()) {
						matches++;
						final int start = patternMatcher.start();
						final int end = patternMatcher.end();
						state.setNextOperation(grepExpressionItem.getOperationOnMatch());
						state.setExclude(grepExpressionItem.isInputFilter());
						state.setMatchesSomething(true);
						state.add(new Filter.ResultItem(state.getOffset() + start, state.getOffset() + end, null,
								grepExpressionItem.getConsoleViewContentType(null).getAttributes()));
					}
				}
			} else if (matches(matchedLine) && !matchesUnless(matchedLine)) {
				matches++;
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
		boolean matches = false;
		if (patternMatcher != null) {
			matches = patternMatcher.reset(matchedLine).matches();
		}
		return matches;
	}

	private boolean matchesUnless(String matchedLine) {
		boolean matchUnless = false;
		if (unlessMatcher != null) {
			matchUnless = unlessMatcher.reset(matchedLine).matches();
		}
		return matchUnless;
	}
}
