package krasa.grepconsole.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.awt.*;
import java.util.ArrayList;

import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;

import org.junit.Test;

import com.intellij.execution.filters.Filter;

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
		grepFilters.add(getFilter(".*ERROR.*", Color.RED));
		grepFilters.add(getFilter(".*INFO.*", Color.BLUE));
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

	private void testVariousText(GrepHighlightService grepFilter) {
		grepFilter.applyFilter("[INFO]\n", 10);
		grepFilter.applyFilter("\n", 10);
		grepFilter.applyFilter("\n\n", 10);
		grepFilter.applyFilter("", 10);
		grepFilter.applyFilter(null, 10);
	}

	private GrepFilter getFilter(String grepExpression, Color red) {
		return new GrepFilter(new GrepExpressionItem().grepExpression(grepExpression).style(
				new GrepStyle().backgroundColor(new GrepColor(red))));
	}

}
