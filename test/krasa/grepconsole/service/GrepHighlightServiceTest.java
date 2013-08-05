package krasa.grepconsole.service;

import com.intellij.execution.filters.Filter;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.filter.Operation;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;

import static junit.framework.Assert.*;

public class GrepHighlightServiceTest {

	@Test
	public void testWithoutFilters() throws Exception {
		GrepHighlightService grepConsoleService = new GrepHighlightService(new Profile(), new ArrayList<GrepFilter>());
		Filter.Result result = grepConsoleService.applyFilter("input", 10);
		assertNull(result);
	}

	@Test
	public void testWithFilters() throws Exception {
		ArrayList<GrepFilter> grepFilters = new ArrayList<GrepFilter>();
		grepFilters.add(getFilterB(".*ERROR.*", Color.RED, Operation.EXIT));
		grepFilters.add(getFilterB(".*INFO.*", Color.BLUE, Operation.EXIT));
		GrepHighlightService grepFilter = new GrepHighlightService(new Profile(), grepFilters);

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
		ArrayList<GrepFilter> grepFilters = new ArrayList<GrepFilter>();
		grepFilters.add(getFilterF(".*BLACK FOREGROUND.*", Color.BLACK, Operation.CONTINUE_MATCHING));
		grepFilters.add(getFilterB(".*RED BACKGROUND.*", Color.RED, Operation.CONTINUE_MATCHING));
		GrepHighlightService grepFilter = new GrepHighlightService(new Profile(), grepFilters);

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

	private void testVariousText(GrepHighlightService grepFilter) {
		grepFilter.applyFilter("[INFO]\n", 10);
		grepFilter.applyFilter("\n", 10);
		grepFilter.applyFilter("\n\n", 10);
		grepFilter.applyFilter("", 10);
		grepFilter.applyFilter(null, 10);
	}

	private GrepFilter getFilterB(String grepExpression, Color red, Operation exit) {
		final GrepExpressionItem grepExpressionItem = getGrepExpressionItem(grepExpression, exit).style(
				new GrepStyle().backgroundColor(new GrepColor(red)));
		return new GrepFilter(grepExpressionItem);
	}

	private GrepFilter getFilterF(String grepExpression, Color red, Operation exit) {
		final GrepExpressionItem grepExpressionItem = getGrepExpressionItem(grepExpression, exit).style(
				new GrepStyle().foregroundColor(new GrepColor(red)));
		return new GrepFilter(grepExpressionItem);
	}

	private GrepExpressionItem getGrepExpressionItem(String grepExpression, Operation exit) {
		return new GrepExpressionItem().grepExpression(grepExpression).operationOnMatch(exit);
	}

}
