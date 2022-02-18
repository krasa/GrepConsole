package krasa.grepconsole.integration;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.grep.GrepBeforeAfterModel;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LookAndFeelListener implements LafManagerListener {

	public static final TextAttributesKey TEXT_ATTRIBUTES_KEY;

	static {
		TextAttributes defaultAttributes = ConsoleViewContentType.getConsoleViewType(ProcessOutputTypes.STDOUT).getAttributes().clone();
		defaultAttributes.setFontType(Font.ITALIC);
		TEXT_ATTRIBUTES_KEY = TextAttributesKey.createTextAttributesKey("Grep Console - Before/After", defaultAttributes);
		ConsoleViewContentType.registerNewConsoleViewType(GrepBeforeAfterModel.GREP_BEFORE_AFTER, new ConsoleViewContentType(GrepBeforeAfterModel.GREP_BEFORE_AFTER.toString(), TEXT_ATTRIBUTES_KEY));
	}

	@Override
	public void lookAndFeelChanged(@NotNull LafManager lafManager) {
		TextAttributes defaultAttributes = TEXT_ATTRIBUTES_KEY.getDefaultAttributes();
		defaultAttributes.copyFrom(ConsoleViewContentType.getConsoleViewType(ProcessOutputTypes.STDOUT).getAttributes());
		defaultAttributes.setFontType(Font.ITALIC);

		Cache.reset();
		ServiceManager.getInstance().rehighlight();
	}
}
