package krasa.grepconsole.grep;

import com.intellij.execution.ui.ConsoleViewContentType;
import krasa.grepconsole.model.*;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static junit.framework.Assert.assertEquals;

public class GrepProcessorTest {

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

		GrepProcessor grepProcessor = new GrepProcessor(grepExpressionItem);
		FilterState process = grepProcessor.process(getInput(LINE));
		checkCache(grepExpressionItem, process);

		process = grepProcessor.process(new FilterState(LINE));
		checkCache(grepExpressionItem, process);
	}

	@Test
	public void testMatches() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		GrepProcessor grepProcessor = new GrepProcessor(grepExpressionItem);
		// matched
		FilterState process = grepProcessor.process(getInput(LINE));
		assertEquals(Operation.EXIT, process.getNextOperation());
		assertEquals(getTextAttributesFromCache(grepExpressionItem), process.getConsoleViewContentType());
		assertEquals(LINE, process.getText());

	}

	@Test
	public void testMatchesUnless() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		GrepProcessor grepProcessor = new GrepProcessor(grepExpressionItem);
		FilterState process = grepProcessor.process(new FilterState(LINE_FOO));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertEquals(LINE_FOO, process.getText());

	}

	@Test
	public void testNoGrepExpression() throws Exception {

		GrepProcessor grepProcessor = new GrepProcessor(new GrepExpressionItem());
		FilterState process = grepProcessor.process(new FilterState(LINE_FOO));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertEquals(LINE_FOO, process.getText());
	}

	private GrepExpressionItem getGrepExpressionItem() {
		return new GrepExpressionItem().style(new GrepStyle().backgroundColor(new GrepColor(true, Color.RED))).grepExpression(
				".*ERROR.*").unlessGrepExpression(".*foo.*");
	}

	private ConsoleViewContentType getTextAttributesFromCache(GrepExpressionItem grepExpressionItem) {
		return Cache.getInstance().getMap().get(getCacheId(grepExpressionItem));
	}

	private String getCacheId(GrepExpressionItem grepExpressionItem) {
		return grepExpressionItem.getId();
	}

	private FilterState getInput(String line) {
		return new FilterState(line);
	}

	private void checkCache(GrepExpressionItem grepExpressionItem, FilterState process) {
		assertEquals(1, Cache.getInstance().getMap().size());
		assertEquals(process.getConsoleViewContentType(), getTextAttributesFromCache(grepExpressionItem));
	}
}
