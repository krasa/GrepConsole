package krasa.grepconsole.service;

import krasa.grepconsole.decorators.ConsoleTextDecorator;
import krasa.grepconsole.decorators.DecoratorState;
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

