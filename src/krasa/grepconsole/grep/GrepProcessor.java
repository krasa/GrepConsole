package krasa.grepconsole.grep;

import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrepProcessor {
	private static final Logger log = Logger.getInstance(GrepProcessor.class.getName());

	private GrepExpressionItem grepExpressionItem;

	public GrepProcessor(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
	}

	public FilterState process(FilterState state) {
		if (grepExpressionItem.isEnabled() && !StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			String matchedLine = state.getText();

			if (matches(matchedLine) && !matchesUnless(matchedLine)) {
				state.setNextOperation(grepExpressionItem.getOperationOnMatch());
				state.setConsoleViewContentType(grepExpressionItem.getConsoleViewContentType(state.getConsoleViewContentType()));
				state.setExclude(grepExpressionItem.isInputFilter());
				state.setMatchesSomething(true);
				grepExpressionItem.getSound().play();
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
