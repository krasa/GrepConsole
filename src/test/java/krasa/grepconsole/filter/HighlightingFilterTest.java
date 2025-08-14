package krasa.grepconsole.filter;

import com.intellij.execution.filters.Filter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.GrepProcessorImpl;
import krasa.grepconsole.model.*;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static junit.framework.Assert.*;

public class HighlightingFilterTest {

	@Test
	public void testWithoutFilters() throws Exception {
		HighlightingFilter grepConsoleService = new HighlightingFilter(new Profile(), new ArrayList<>());
		Filter.Result result = grepConsoleService.applyFilter("input", 10);
		assertNull(result);
	}

	@Test
	public void testWithFilters() throws Exception {
		ArrayList<GrepProcessor> grepProcessors = new ArrayList<>();
		grepProcessors.add(getFilterB(".*ERROR.*", Color.RED, Operation.EXIT));
		grepProcessors.add(getFilterB(".*INFO.*", Color.BLUE, Operation.EXIT));
		HighlightingFilter grepFilter = new HighlightingFilter(new Profile(), grepProcessors);

		assertNull(grepFilter.applyFilter("[WARN]", 10));

		Filter.Result result = grepFilter.applyFilter("[ERROR]", 10);
		assertNotNull(result);
		assertEquals(1, result.getResultItems().size());
		Filter.ResultItem resultItem = result.getResultItems().get(0);
		assertEquals(Color.RED, resultItem.getHighlightAttributes().getBackgroundColor());
		assertNull(resultItem.getHighlightAttributes().getEffectColor());
		assertNull(resultItem.getHighlightAttributes().getErrorStripeColor());
		assertNull(resultItem.getHighlightAttributes().getForegroundColor());

		result = grepFilter.applyFilter("[INFO]", 10);
		assertEquals(1, result.getResultItems().size());
		resultItem = result.getResultItems().get(0);
		assertNotNull(resultItem);
		assertEquals(Color.BLUE, resultItem.getHighlightAttributes().getBackgroundColor());

		testVariousText(grepFilter);
	}

	@Test
	public void testCombinations() throws Exception {
		ArrayList<GrepProcessor> grepProcessors = new ArrayList<>();
		grepProcessors.add(getFilterF(".*BLACK FOREGROUND.*", Color.BLACK, Operation.CONTINUE_MATCHING));
		grepProcessors.add(getFilterB(".*RED BACKGROUND.*", Color.RED, Operation.CONTINUE_MATCHING));
		HighlightingFilter grepFilter = new HighlightingFilter(new Profile(), grepProcessors);

		Filter.Result result = grepFilter.applyFilter("BLACK FOREGROUND RED BACKGROUND", 100);
		assertNotNull(result);
		assertEquals(1, result.getResultItems().size());
		Filter.ResultItem resultItem = result.getResultItems().get(0);
		assertEquals(Color.RED, resultItem.getHighlightAttributes().getBackgroundColor());
		assertEquals(Color.BLACK, resultItem.getHighlightAttributes().getForegroundColor());
		assertNull(resultItem.getHighlightAttributes().getEffectColor());
		assertNull(resultItem.getHighlightAttributes().getErrorStripeColor());

		result = grepFilter.applyFilter("BLACK FOREGROUND", 100);
		assertEquals(1, result.getResultItems().size());
		resultItem = result.getResultItems().get(0);
		assertEquals(null, resultItem.getHighlightAttributes().getBackgroundColor());
		assertEquals(Color.BLACK, resultItem.getHighlightAttributes().getForegroundColor());

		result = grepFilter.applyFilter("RED BACKGROUND", 100);
		assertEquals(1, result.getResultItems().size());
		resultItem = result.getResultItems().get(0);
		assertEquals(Color.RED, resultItem.getHighlightAttributes().getBackgroundColor());
		assertEquals(null, resultItem.getHighlightAttributes().getForegroundColor());
	}

	private void testVariousText(HighlightingFilter grepFilter) {
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
