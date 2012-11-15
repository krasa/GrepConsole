package krasa.grepconsole;

import krasa.grepconsole.model.GrepExpressionItem;

public class DummyConsoleTextDecorator extends GrepFilter {
	public DummyConsoleTextDecorator(GrepExpressionItem grepExpressionItem) {
		super(grepExpressionItem);
	}

	@Override
	public FilterState process(FilterState flow) {
		return flow;
	}
}
