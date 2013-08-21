package krasa.grepconsole.filter;

import static junit.framework.Assert.*;

import java.awt.*;
import java.util.ArrayList;

import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Operation;
import krasa.grepconsole.model.Profile;

import org.junit.Test;

import com.intellij.execution.filters.Filter;

public class GrepHighlightFilterTest {

	@Test
	public void testWithoutFilters() throws Exception {
		GrepHighlightFilter grepConsoleService = new GrepHighlightFilter(new Profile(), new ArrayList<GrepProcessor>());
		Filter.Result result = grepConsoleService.applyFilter("input", 10);
		assertNull(result);
	}

	@Test
	public void testWithFilters() throws Exception {
		ArrayList<GrepProcessor> grepProcessors = new ArrayList<GrepProcessor>();
		grepProcessors.add(getFilterB(".*ERROR.*", Color.RED, Operation.EXIT));
		grepProcessors.add(getFilterB(".*INFO.*", Color.BLUE, Operation.EXIT));
		GrepHighlightFilter grepFilter = new GrepHighlightFilter(new Profile(), grepProcessors);

		assertNull(grepFilter.applyFilter("[WARN]", 10));

		Filter.Result result = grepFilter.applyFilter("[ERROR]", 10);
		assertNotNull(result);
		assertEquals(Color.RED, result.highlightAttributes.getBackgroundColor());
		assertNull(result.highlightAttributes.getEffectColor());
		assertNull(result.highlightAttributes.getErrorStripeColor());
		assertNull(result.highlightAttributes.getForegroundColor());

		result = grepFilter.applyFilter("[INFO]", 10);
		assertNotNull(result);
		assertEquals(Color.BLUE, result.highlightAttributes.getBackgroundColor());

		testVariousText(grepFilter);
	}

	@Test
	public void testCombinations() throws Exception {
		ArrayList<GrepProcessor> grepProcessors = new ArrayList<GrepProcessor>();
		grepProcessors.add(getFilterF(".*BLACK FOREGROUND.*", Color.BLACK, Operation.CONTINUE_MATCHING));
		grepProcessors.add(getFilterB(".*RED BACKGROUND.*", Color.RED, Operation.CONTINUE_MATCHING));
		GrepHighlightFilter grepFilter = new GrepHighlightFilter(new Profile(), grepProcessors);

		Filter.Result result = grepFilter.applyFilter("BLACK FOREGROUND RED BACKGROUND", 10);
		assertNotNull(result);
		assertEquals(Color.RED, result.highlightAttributes.getBackgroundColor());
		assertEquals(Color.BLACK, result.highlightAttributes.getForegroundColor());
		assertNull(result.highlightAttributes.getEffectColor());
		assertNull(result.highlightAttributes.getErrorStripeColor());

		result = grepFilter.applyFilter("BLACK FOREGROUND", 10);
		assertEquals(null, result.highlightAttributes.getBackgroundColor());
		assertEquals(Color.BLACK, result.highlightAttributes.getForegroundColor());

		result = grepFilter.applyFilter("RED BACKGROUND", 10);
		assertEquals(Color.RED, result.highlightAttributes.getBackgroundColor());
		assertEquals(null, result.highlightAttributes.getForegroundColor());
	}

	private void testVariousText(GrepHighlightFilter grepFilter) {
		grepFilter.applyFilter("[INFO]\n", 10);
		grepFilter.applyFilter("\n", 10);
		grepFilter.applyFilter("\n\n", 10);
		grepFilter.applyFilter("", 10);
		grepFilter.applyFilter(null, 10);
	}

	private GrepProcessor getFilterB(String grepExpression, Color red, Operation exit) {
		final GrepExpressionItem grepExpressionItem = getGrepExpressionItem(grepExpression, exit).style(
				new GrepStyle().backgroundColor(new GrepColor(red)));
		return new GrepProcessor(grepExpressionItem);
	}

	private GrepProcessor getFilterF(String grepExpression, Color red, Operation exit) {
		final GrepExpressionItem grepExpressionItem = getGrepExpressionItem(grepExpression, exit).style(
				new GrepStyle().foregroundColor(new GrepColor(red)));
		return new GrepProcessor(grepExpressionItem);
	}

	private GrepExpressionItem getGrepExpressionItem(String grepExpression, Operation exit) {
		return new GrepExpressionItem().grepExpression(grepExpression).operationOnMatch(exit);
	}

}
