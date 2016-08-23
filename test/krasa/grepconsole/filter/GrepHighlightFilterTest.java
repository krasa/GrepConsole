package krasa.grepconsole.filter;

import static junit.framework.Assert.*;

import java.awt.*;
import java.util.ArrayList;

import org.junit.Test;

import com.intellij.execution.filters.Filter;

import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.GrepProcessorImpl;
import krasa.grepconsole.model.*;

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
		assertEquals(1, result.getResultItems().size());
		Filter.ResultItem resultItem = result.getResultItems().get(0);
		assertEquals(Color.RED, resultItem.highlightAttributes.getBackgroundColor());
		assertNull(resultItem.highlightAttributes.getEffectColor());
		assertNull(resultItem.highlightAttributes.getErrorStripeColor());
		assertNull(resultItem.highlightAttributes.getForegroundColor());

		result = grepFilter.applyFilter("[INFO]", 10);
		assertEquals(1, result.getResultItems().size());
		resultItem = result.getResultItems().get(0);
		assertNotNull(resultItem);
		assertEquals(Color.BLUE, resultItem.highlightAttributes.getBackgroundColor());

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
		assertEquals(1, result.getResultItems().size());
		Filter.ResultItem resultItem = result.getResultItems().get(0);
		assertEquals(Color.RED, resultItem.highlightAttributes.getBackgroundColor());
		assertEquals(Color.BLACK, resultItem.highlightAttributes.getForegroundColor());
		assertNull(resultItem.highlightAttributes.getEffectColor());
		assertNull(resultItem.highlightAttributes.getErrorStripeColor());

		result = grepFilter.applyFilter("BLACK FOREGROUND", 10);
		assertEquals(1, result.getResultItems().size());
		resultItem = result.getResultItems().get(0);
		assertEquals(null, resultItem.highlightAttributes.getBackgroundColor());
		assertEquals(Color.BLACK, resultItem.highlightAttributes.getForegroundColor());

		result = grepFilter.applyFilter("RED BACKGROUND", 10);
		assertEquals(1, result.getResultItems().size());
		resultItem = result.getResultItems().get(0);
		assertEquals(Color.RED, resultItem.highlightAttributes.getBackgroundColor());
		assertEquals(null, resultItem.highlightAttributes.getForegroundColor());
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
		return new GrepProcessorImpl(grepExpressionItem);
	}

	private GrepProcessor getFilterF(String grepExpression, Color red, Operation exit) {
		final GrepExpressionItem grepExpressionItem = getGrepExpressionItem(grepExpression, exit).style(
				new GrepStyle().foregroundColor(new GrepColor(red)));
		return new GrepProcessorImpl(grepExpressionItem);
	}

	private GrepExpressionItem getGrepExpressionItem(String grepExpression, Operation exit) {
		return new GrepExpressionItem().grepExpression(grepExpression).operationOnMatch(exit);
	}

}
