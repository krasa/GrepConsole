package krasa.grepconsole.integration;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LookAndFeelListener implements LafManagerListener {

	public static final Key GREP_BEFORE_AFTER = new Key("grepBeforeAfter");
	public static TextAttributesKey TEXT_ATTRIBUTES_KEY;
	public static ConsoleViewContentType CONTENT_TYPE;

	public LookAndFeelListener() {
		lookAndFeelChanged();
	}

	public static void lookAndFeelChanged() {
		TextAttributes defaultAttributes = ConsoleViewContentType.getConsoleViewType(ProcessOutputTypes.STDOUT).getAttributes().clone();
		defaultAttributes.setFontType(Font.ITALIC);
		TEXT_ATTRIBUTES_KEY = TextAttributesKey.createTextAttributesKey("Grep Console - Before/After", defaultAttributes);
		CONTENT_TYPE = new ConsoleViewContentType(GREP_BEFORE_AFTER.toString(), TEXT_ATTRIBUTES_KEY);
		ConsoleViewContentType.registerNewConsoleViewType(GREP_BEFORE_AFTER, CONTENT_TYPE);
	}

	@Override
	public void lookAndFeelChanged(@NotNull LafManager lafManager) {
		lookAndFeelChanged();

		Cache.reset();
		ServiceManager.getInstance().rehighlight();
	}

}
