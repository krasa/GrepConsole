package krasa.grepconsole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.grepconsole.model.GrepExpressionItem;

import org.apache.commons.lang.StringUtils;

import com.intellij.openapi.diagnostic.Logger;

public class GrepFilter {
	private static final Logger log = Logger.getInstance(GrepFilter.class.getName());

	private GrepExpressionItem grepExpressionItem;

	public GrepFilter(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
	}

	public FilterState process(FilterState flow) {
		if (grepExpressionItem.isEnabled() && !StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			String matchedLine = flow.getText();

			if (matches(matchedLine) && !matchesUnless(matchedLine)) {
				flow.setNextOperation(grepExpressionItem.getOperationOnMatch());
				flow.setConsoleViewContentType(grepExpressionItem.getTextAttributes());
				flow.setExclude(grepExpressionItem.isInputFilter());

				playSound(flow);
			}
		}
		return flow;
	}

	private void playSound(FilterState flow) {
		if (flow.getMode() == Mode.DEFAULT) {
			grepExpressionItem.getSound().play();
		}
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
