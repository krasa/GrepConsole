package krasa.grepconsole;

import static junit.framework.Assert.assertEquals;

import java.awt.*;

import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;

import org.junit.Before;
import org.junit.Test;

import com.intellij.openapi.editor.markup.TextAttributes;

public class ConsoleTextDecoratorTest {

	public static final String BAR = "bar";
	public static final String LINE = "[ERROR]";
	public static final String LINE_FOO = "[ERROR] foo";

	@Before
	public void setUp() throws Exception {
		Cache.reset();
	}

	@Test
	public void testCache() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		ConsoleTextDecorator consoleTextDecorator = new ConsoleTextDecorator(grepExpressionItem);
		DecoratorState process = consoleTextDecorator.process(getInput(LINE));
		checkCache(grepExpressionItem, process);

		process = consoleTextDecorator.process(new DecoratorState(LINE));
		checkCache(grepExpressionItem, process);
	}

	@Test
	public void testMatches() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		ConsoleTextDecorator consoleTextDecorator = new ConsoleTextDecorator(grepExpressionItem);
		// matched
		DecoratorState process = consoleTextDecorator.process(getInput(LINE));
		assertEquals(Operation.PRINT_IMMEDIATELY, process.getNextOperation());
		assertEquals(getTextAttributesFromCache(grepExpressionItem), process.getTextAttributes());
		assertEquals(LINE, process.getLine());

	}

	@Test
	public void testMatchesUnless() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		ConsoleTextDecorator consoleTextDecorator = new ConsoleTextDecorator(grepExpressionItem);
		DecoratorState process = consoleTextDecorator.process(new DecoratorState(LINE_FOO));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getTextAttributes());
		assertEquals(LINE_FOO, process.getLine());

	}

	private GrepExpressionItem getGrepExpressionItem() {
		return new GrepExpressionItem().style(new GrepStyle().backgroundColor(new GrepColor(true, Color.RED))).grepExpression(
				".*ERROR.*").unlessGrepExpression(".*foo.*");
	}

	private TextAttributes getTextAttributesFromCache(GrepExpressionItem grepExpressionItem) {
		return Cache.getInstance().getMap().get(getCacheId(grepExpressionItem));
	}

	private String getCacheId(GrepExpressionItem grepExpressionItem) {
		return grepExpressionItem.getId();
	}

	private DecoratorState getInput(String line) {
		return new DecoratorState(line);
	}

	private void checkCache(GrepExpressionItem grepExpressionItem, DecoratorState process) {
		assertEquals(1, Cache.getInstance().getMap().size());
		assertEquals(process.getTextAttributes(), getTextAttributesFromCache(grepExpressionItem));
	}
}
