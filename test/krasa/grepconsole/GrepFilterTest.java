package krasa.grepconsole;

import static junit.framework.Assert.assertEquals;

import java.awt.*;

import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;

import org.junit.Before;
import org.junit.Test;

import com.intellij.execution.ui.ConsoleViewContentType;

public class GrepFilterTest {

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

		GrepFilter grepFilter = new GrepFilter(grepExpressionItem);
		FilterState process = grepFilter.process(getInput(LINE));
		checkCache(grepExpressionItem, process);

		process = grepFilter.process(new FilterState(LINE));
		checkCache(grepExpressionItem, process);
	}

	@Test
	public void testMatches() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		GrepFilter grepFilter = new GrepFilter(grepExpressionItem);
		// matched
		FilterState process = grepFilter.process(getInput(LINE));
		assertEquals(Operation.PRINT_IMMEDIATELY, process.getNextOperation());
		assertEquals(getTextAttributesFromCache(grepExpressionItem), process.getConsoleViewContentType());
		assertEquals(LINE, process.getText());

	}

	@Test
	public void testMatchesUnless() throws Exception {
		GrepExpressionItem grepExpressionItem = getGrepExpressionItem();

		GrepFilter grepFilter = new GrepFilter(grepExpressionItem);
		FilterState process = grepFilter.process(new FilterState(LINE_FOO));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertEquals(LINE_FOO, process.getText());

	}

	@Test
	public void testNoGrepExpression() throws Exception {

		GrepFilter grepFilter = new GrepFilter(new GrepExpressionItem());
		FilterState process = grepFilter.process(new FilterState(LINE_FOO));
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
