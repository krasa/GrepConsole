package krasa.grepconsole.service;

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

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;

public class GrepInputFilterServiceTest {

	public static final String WARN = "[WARN]";
	public static final String INFO = "[INFO]";
	public static final String ERROR = "[ERROR]";

	@Test
	public void testWithoutFilters() throws Exception {
		GrepInputFilterService grepConsoleService = new GrepInputFilterService(new Profile(),
				new ArrayList<GrepFilter>());
		InputFilter.Result result = grepConsoleService.applyFilter("input", ConsoleViewContentType.NORMAL_OUTPUT);
		assertNull(result);
	}

	@Test
	public void testWithFilters() throws Exception {
		ArrayList<GrepFilter> grepFilters = new ArrayList<GrepFilter>();
		grepFilters.add(getFilter(".*ERROR.*", Color.RED));
		grepFilters.add(getFilter(".*INFO.*", Color.BLUE));
		GrepInputFilterService grepFilter = new GrepInputFilterService(new Profile(), grepFilters);

		assertNull(grepFilter.applyFilter(WARN, ConsoleViewContentType.NORMAL_OUTPUT));

		InputFilter.Result result = grepFilter.applyFilter(ERROR, ConsoleViewContentType.NORMAL_OUTPUT);
		assertNotNull(result);
		assertNull(result.getLine());
		assertNull(result.getConsoleViewContentType());

		result = grepFilter.applyFilter(INFO, ConsoleViewContentType.NORMAL_OUTPUT);
		assertNotNull(result);
		assertNull(result.getLine());
		assertNull(result.getConsoleViewContentType());

	}

	private GrepFilter getFilter(String grepExpression, Color red) {
		return new GrepFilter(new GrepExpressionItem().grepExpression(grepExpression).style(
				new GrepStyle().backgroundColor(new GrepColor(red))));
	}
}
