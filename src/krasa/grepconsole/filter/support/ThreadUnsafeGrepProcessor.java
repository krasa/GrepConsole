package krasa.grepconsole.filter.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;

import krasa.grepconsole.model.GrepExpressionItem;

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
			CharSequence input = StringUtil.newBombedCharSequence(matchedLine, 10000);
			if (grepExpressionItem.isHighlightOnlyMatchingText()) {
				if (patternMatcher != null) {
					patternMatcher.reset(input);
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
			} else if (matches(input) && !matchesUnless(input)) {
				matches++;
				state.setNextOperation(grepExpressionItem.getOperationOnMatch());
				state.setConsoleViewContentType(
						grepExpressionItem.getConsoleViewContentType(state.getConsoleViewContentType()));
				state.setExclude(grepExpressionItem.isInputFilter());
				state.setMatchesSomething(true);
				if (grepExpressionItem.getSound().isEnabled()) {
					grepExpressionItem.getSound().play();
				}
			}
		}
		return state;
	}

	private boolean matches(CharSequence matchedLine) {
		boolean matches = false;
		if (patternMatcher != null) {
			matches = patternMatcher.reset(matchedLine).matches();
		}
		return matches;
	}

	private boolean matchesUnless(CharSequence matchedLine) {
		boolean matchUnless = false;
		if (unlessMatcher != null) {
			matchUnless = unlessMatcher.reset(matchedLine).matches();
		}
		return matchUnless;
	}
}
