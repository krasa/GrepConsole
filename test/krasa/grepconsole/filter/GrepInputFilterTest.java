package krasa.grepconsole.filter;

import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.awt.*;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Pair;

import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.GrepProcessorImpl;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;

public class GrepInputFilterTest {

	protected GrepInputFilter service;
	protected GrepInputFilter service2;

	@Before
	public void setUp() throws Exception {
		ArrayList<GrepProcessor> grepProcessors = new ArrayList<GrepProcessor>();
		grepProcessors.add(getFilter(".*ERROR.*", Color.RED));
		grepProcessors.add(getFilter(".*INFO.*", Color.BLUE));
		service = new GrepInputFilter(new Profile(), grepProcessors);
		service2 = new GrepInputFilter(new Profile(), new ArrayList<GrepProcessor>());
	}

	@Test
	public void testWithoutFilters() throws Exception {
		java.util.List<Pair<String, ConsoleViewContentType>> result = service2.applyFilter("input", NORMAL_OUTPUT);
		assertNull(result);
	}

	@Test
	public void testNotMatched() throws Exception {
		assertNull(service.applyFilter("[WARN]", NORMAL_OUTPUT));
	}

	@Test
	public void testMatched() throws Exception {
		java.util.List<Pair<String, ConsoleViewContentType>> result = service.applyFilter("[ERROR]", NORMAL_OUTPUT);
		assertNotNull(result);
		Assert.assertEquals(1, result.size());
		final Pair<String, ConsoleViewContentType> pai = result.get(0);
		assertExcludedItem(pai);
	}

	@Test
	public void testMatched2() throws Exception {
		java.util.List<Pair<String, ConsoleViewContentType>> result = service.applyFilter("[INFO]", NORMAL_OUTPUT);
		assertNotNull(result);
		Assert.assertEquals(1, result.size());
		final Pair<String, ConsoleViewContentType> pai = result.get(0);
		assertExcludedItem(pai);
	}

	private void assertExcludedItem(Pair<String, ConsoleViewContentType> pai) {
		assertNull(pai.first);
		assertNull(pai.second);
	}

	private GrepProcessor getFilter(String grepExpression, Color red) {
		GrepStyle style1 = new GrepStyle().backgroundColor(new GrepColor(red));
		GrepExpressionItem item = new GrepExpressionItem().grepExpression(grepExpression).style(style1).inputFilter(
				true);
		return new GrepProcessorImpl(item);
	}
}
