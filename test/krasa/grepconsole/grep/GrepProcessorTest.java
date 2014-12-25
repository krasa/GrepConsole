package krasa.grepconsole.grep;

import com.intellij.execution.ui.ConsoleViewContentType;
import krasa.grepconsole.model.*;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static com.intellij.util.ObjectUtils.assertNotNull;
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

		GrepProcessorImpl grepProcessor = new GrepProcessorImpl(grepExpressionItem);
		FilterState process = grepProcessor.process(getInput(LINE));
		checkCache(grepExpressionItem, process);

		process = grepProcessor.process(new FilterState(LINE, -1));
		checkCache(grepExpressionItem, process);
	}

	@Test
	public void testMatches() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		GrepProcessorImpl grepProcessor = new GrepProcessorImpl(grepExpressionItem);
		// matched
		FilterState process = grepProcessor.process(getInput(LINE));
		assertEquals(Operation.EXIT, process.getNextOperation());
		assertEquals(getTextAttributesFromCache(grepExpressionItem), process.getConsoleViewContentType());
		assertEquals(LINE, process.getText());

	}

	@Test
	public void testHighlightOnlyMatchingText() throws Exception {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem().style(new GrepStyle().backgroundColor(new GrepColor(true, Color.RED))).grepExpression(
				"ERROR").highlightOnlyMatchingText(true);

		GrepProcessorImpl grepProcessor = new GrepProcessorImpl(grepExpressionItem);
		// matched
		FilterState process = grepProcessor.process(getInput("foo [ERROR] [WARN] [ERROR]"));
		assertEquals(Operation.EXIT, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertNotNull(process.getResultItemList());
		assertEquals(2, process.getResultItemList().size());
	}

	@Test
	public void testMatchesUnless() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		GrepProcessorImpl grepProcessor = new GrepProcessorImpl(grepExpressionItem);
		FilterState process = grepProcessor.process(new FilterState(LINE_FOO, -1));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertEquals(LINE_FOO, process.getText());

	}

	@Test
	public void testNoGrepExpression() throws Exception {

		GrepProcessorImpl grepProcessor = new GrepProcessorImpl(new GrepExpressionItem());
		FilterState process = grepProcessor.process(new FilterState(LINE_FOO, -1));
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
		return new FilterState(line, -1);
	}

	private void checkCache(GrepExpressionItem grepExpressionItem, FilterState process) {
		assertEquals(1, Cache.getInstance().getMap().size());
		assertEquals(process.getConsoleViewContentType(), getTextAttributesFromCache(grepExpressionItem));
	}
}
