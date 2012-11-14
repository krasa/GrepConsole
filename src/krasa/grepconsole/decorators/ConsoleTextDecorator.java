package krasa.grepconsole.decorators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.ModifiableConsoleViewContentType;

import org.apache.commons.lang.StringUtils;

public class ConsoleTextDecorator {

	private GrepExpressionItem grepExpressionItem;

	public ConsoleTextDecorator(GrepExpressionItem grepExpressionItem) {
		this.grepExpressionItem = grepExpressionItem;
	}

	public DecoratorState process(DecoratorState flow) {
		if (!StringUtils.isEmpty(grepExpressionItem.getGrepExpression())) {
			String matchedLine = flow.getLine();

			if (matches(matchedLine) && !matchesUnless(matchedLine)) {
				ModifiableConsoleViewContentType style = grepExpressionItem.getStyle(flow.getContentType());
				flow.setContentType(style);
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
