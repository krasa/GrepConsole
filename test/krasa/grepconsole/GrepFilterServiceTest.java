package krasa.grepconsole;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.awt.*;
import java.util.ArrayList;

import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;

import org.junit.Test;

import com.intellij.execution.filters.Filter;

public class GrepFilterServiceTest {

	@Test
	public void testNoDecorators() throws Exception {
		GrepFilterService grepConsoleService = new GrepFilterService(new Profile(),
				new ArrayList<ConsoleTextDecorator>());
		Filter.Result result = grepConsoleService.applyFilter("input", 10);

		assertNull(result);
	}

	@Test
	public void testDecorators() throws Exception {
		ArrayList<ConsoleTextDecorator> consoleTextDecorators = new ArrayList<ConsoleTextDecorator>();

		consoleTextDecorators.add(new ConsoleTextDecorator(new GrepExpressionItem().grepExpression(".*ERROR.*").style(
				new GrepStyle().backgroundColor(new GrepColor(Color.RED)))));
		consoleTextDecorators.add(new ConsoleTextDecorator(new GrepExpressionItem().grepExpression(".*INFO.*").style(
				new GrepStyle().backgroundColor(new GrepColor(Color.BLUE)))));
		GrepFilterService grepFilter = new GrepFilterService(new Profile(), consoleTextDecorators);

		assertNull(grepFilter.applyFilter("[WARN]", 10));

		Filter.Result result = grepFilter.applyFilter("[ERROR]", 10);
		assertNotNull(result);
		assertEquals(Color.RED, result.highlightAttributes.getBackgroundColor());
		assertNull(result.highlightAttributes.getEffectColor());
		assertNull(result.highlightAttributes.getErrorStripeColor());
		assertNull(result.highlightAttributes.getForegroundColor());

		Filter.Result result1 = grepFilter.applyFilter("[INFO]", 10);
		assertNotNull(result1);
		assertEquals(Color.BLUE, result1.highlightAttributes.getBackgroundColor());

	}

}
