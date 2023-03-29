package krasa.grepconsole.filter.support;

import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * should perform faster than GrepProcessorImpl
 */
public class ThreadUnsafeGrepProcessor extends GrepProcessor {
	private static final Logger log = Logger.getInstance(ThreadUnsafeGrepProcessor.class);
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

	@SuppressWarnings("Duplicates")
	@Override
	public FilterState process(FilterState state) {
		if (grepExpressionItem.isEnabled() && !StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			CharSequence input = state.getCharSequence();
			if (grepExpressionItem.isHighlightOnlyMatchingText()) {
				if (patternMatcher != null) {
					patternMatcher.reset(input);
					while (patternMatcher.find()) {
						if (patternMatcher.groupCount() > 0){
							matches += patternMatcher.groupCount();
							for (int i = 1; i <= patternMatcher.groupCount(); i++) {
								final int start = patternMatcher.start(i);
								final int end = patternMatcher.end(i);
								MyResultItem resultItem = new MyResultItem(state.getOffset() + start, state.getOffset() + end,
										null, grepExpressionItem.getConsoleViewContentType(null));
								state.add(resultItem);
							}
							state.executeAction(grepExpressionItem);
						}else {
							matches++;
							final int start = patternMatcher.start();
							final int end = patternMatcher.end();
							MyResultItem resultItem = new MyResultItem(state.getOffset() + start, state.getOffset() + end,
																	   null, grepExpressionItem.getConsoleViewContentType(null));
							state.add(resultItem);
							state.executeAction(grepExpressionItem);
						}
					}
				}
			} else if (matches(input) && !matchesUnless(input)) {
				matches++;
				state.setConsoleViewContentType(grepExpressionItem.getConsoleViewContentType(state.getConsoleViewContentType()));
				state.executeAction(grepExpressionItem);
			}
		}
		return state;
	}

	private boolean matches(CharSequence input) {
		boolean matches = false;
		if (patternMatcher != null) {
			matches = patternMatcher.reset(input).matches();
		}
		return matches;
	}

	private boolean matchesUnless(CharSequence input) {
		boolean matchUnless = false;
		if (unlessMatcher != null) {
			matchUnless = unlessMatcher.reset(input).matches();
		}
		return matchUnless;
	}

	@Override
	public String toString() {
		String grepExpression = grepExpressionItem.getGrepExpression();
		String unless = grepExpressionItem.getUnlessGrepExpression();
		return "pattern='" + grepExpression + "', unlessPattern='" + unless + "'";
	}

}
