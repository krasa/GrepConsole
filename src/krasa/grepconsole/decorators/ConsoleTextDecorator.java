package krasa.grepconsole.decorators;

import java.util.regex.Matcher;

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
			Matcher matcher = grepExpressionItem.getPattern().matcher(flow.getLine());
			if (matcher.matches()) {
				ModifiableConsoleViewContentType style = grepExpressionItem.getStyle(flow.getContentType());
				flow.setContentType(style);
				flow.setOperation(grepExpressionItem.getOperationOnMatch());
			}
		}
		return flow;
	}
}
