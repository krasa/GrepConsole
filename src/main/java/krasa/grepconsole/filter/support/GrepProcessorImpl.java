package krasa.grepconsole.filter.support;

import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrepProcessorImpl extends GrepProcessor {
	private static final Logger log = Logger.getInstance(GrepProcessorImpl.class);

	private GrepExpressionItem grepExpressionItem;
	private int matches;

	public GrepProcessorImpl(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
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
			if (grepExpressionItem.isHighlightOnlyMatchingText()) { //not whole line
				Pattern pattern = grepExpressionItem.getPattern();
				if (pattern != null) {
					final Matcher matcher = pattern.matcher(input);
					while (matcher.find()) {
						matches++;
						final int start = matcher.start();
						final int end = matcher.end();
						state.executeAction(grepExpressionItem, matcher);
						MyResultItem resultItem = new MyResultItem(state.getOffset() + start, state.getOffset() + end,
								null, grepExpressionItem.getConsoleViewContentType(null));

						state.add(resultItem);
					}
				}
			} else {//whole line
				Pattern pattern = grepExpressionItem.getPattern();
				boolean isMatching = false;
				Matcher matcher = null;
				if (pattern != null) {
					matcher = pattern.matcher(input);
					isMatching = matcher.matches();
				}
				if (isMatching && !matchesUnless(input)) {
					matches++;
					state.setConsoleViewContentType(
							grepExpressionItem.getConsoleViewContentType(state.getConsoleViewContentType()));
					state.executeAction(grepExpressionItem, matcher);
				}
			}
		}
		return state;
	}

	private boolean matches(CharSequence input) {
		Pattern pattern = grepExpressionItem.getPattern();
		boolean matches = false;
		if (pattern != null) {
			matches = pattern.matcher(input).matches();
		}
		return matches;
	}

	private boolean matchesUnless(CharSequence input) {
		boolean matchUnless = false;
		Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
		if (unlessPattern != null) {
			Matcher unlessMatcher = unlessPattern.matcher(input);
			if (unlessMatcher.matches()) {
				matchUnless = true;
			}
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
