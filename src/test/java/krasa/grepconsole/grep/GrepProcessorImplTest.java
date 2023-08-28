package krasa.grepconsole.grep;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessorImpl;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Operation;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static com.intellij.util.ObjectUtils.assertNotNull;
import static junit.framework.Assert.assertEquals;

public class GrepProcessorImplTest {

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

		process = grepProcessor.process(new FilterState(-1, "foo", null, StringUtil.newBombedCharSequence(LINE, 1000), null));
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
		assertEquals(LINE, process.getCharSequence().toString());

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
		FilterState process = grepProcessor.process(new FilterState(-1, "foo", null, StringUtil.newBombedCharSequence(LINE_FOO, 1000), null));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertEquals(LINE_FOO, process.getCharSequence().toString());

	}

	@Test
	public void testNoGrepExpression() throws Exception {

		GrepProcessorImpl grepProcessor = new GrepProcessorImpl(new GrepExpressionItem());
		FilterState process = grepProcessor.process(new FilterState(-1, "foo", null, StringUtil.newBombedCharSequence(LINE_FOO, 1000), null));
		// unless matched = no match
		assertEquals(Operation.CONTINUE_MATCHING, process.getNextOperation());
		assertEquals(null, process.getConsoleViewContentType());
		assertEquals(LINE_FOO, process.getCharSequence().toString());
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
		return new FilterState(-1, "foo", null, StringUtil.newBombedCharSequence(line, 1000), null);
	}

	private void checkCache(GrepExpressionItem grepExpressionItem, FilterState process) {
		assertEquals(1, Cache.getInstance().getMap().size());
		assertEquals(process.getConsoleViewContentType(), getTextAttributesFromCache(grepExpressionItem));
	}
}
