package krasa.grepconsole.filter;

import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.awt.*;
import java.util.ArrayList;

import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.GrepProcessorImpl;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;

import org.junit.Before;
import org.junit.Test;

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
		String result = service2.applyFilter("input", NORMAL_OUTPUT);
		assertNotNull(result);
	}

	@Test
	public void testNotMatched() throws Exception {
		assertNotNull(service.applyFilter("[WARN]", NORMAL_OUTPUT));
	}

	@Test
	public void testMatched() throws Exception {
		String result = service.applyFilter("[ERROR]", NORMAL_OUTPUT);
		assertNull(result);
	}

	@Test
	public void testMatched2() throws Exception {
		String result = service.applyFilter("[INFO]", NORMAL_OUTPUT);
		assertNull(result);
	}

	private GrepProcessor getFilter(String grepExpression, Color red) {
		GrepStyle style1 = new GrepStyle().backgroundColor(new GrepColor(red));
		GrepExpressionItem item = new GrepExpressionItem().grepExpression(grepExpression).style(style1).inputFilter(
				true);
		return new GrepProcessorImpl(item);
	}
}
