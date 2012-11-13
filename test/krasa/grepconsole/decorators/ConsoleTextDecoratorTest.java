package krasa.grepconsole.decorators;

import java.awt.*;

import junit.framework.Assert;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.service.Cache;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;

import org.junit.Test;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;

public class ConsoleTextDecoratorTest {

	public static final String FOO = "foo";
	public static final ConsoleViewContentType ORIGINAL_CONTENT_TYPE = new ConsoleViewContentType(FOO,
			new TextAttributes());

	@Test
	public void testProcess() throws Exception {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem().style(
				new GrepStyle().backgroundColor(new GrepColor(true, Color.RED))).grepExpression(".*ERROR");

		ConsoleTextDecorator consoleTextDecorator = new ConsoleTextDecorator(grepExpressionItem);

		DecoratorState process = consoleTextDecorator.process(new DecoratorState("[ERROR]", ORIGINAL_CONTENT_TYPE));
		checkCache(grepExpressionItem, process);

		process = consoleTextDecorator.process(new DecoratorState("[ERROR] foo", ORIGINAL_CONTENT_TYPE));
		checkCache(grepExpressionItem, process);
	}

	private void checkCache(GrepExpressionItem grepExpressionItem, DecoratorState process) {
		Assert.assertEquals(1, Cache.getInstance().getMap().size());
		Assert.assertEquals(process.getContentType(),  Cache.getInstance().getMap().get(grepExpressionItem.getId()+FOO));
	}
}
