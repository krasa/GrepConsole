package krasa.grepconsole;

import krasa.grepconsole.model.GrepExpressionItem;

public class DummyConsoleTextDecorator extends ConsoleTextDecorator {
	public DummyConsoleTextDecorator(GrepExpressionItem grepExpressionItem) {
		super(grepExpressionItem);
	}

	@Override
	public DecoratorState process(DecoratorState flow) {
		return flow;
	}
}
