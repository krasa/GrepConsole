package krasa.grepconsole.plugin;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;

public abstract class HighlightManipulationAction extends AnAction {
	public HighlightManipulationAction() {
	}

	public HighlightManipulationAction(Icon icon) {
		super(icon);
	}

	public HighlightManipulationAction(@Nullable String text) {
		super(text);
	}

	public HighlightManipulationAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	protected void resetHighlights(ConsoleView console) {
		if (console instanceof ConsoleViewImpl) {
			reset((ConsoleViewImpl) console);
		} else if (console instanceof BaseTestsOutputConsoleView) {
			BaseTestsOutputConsoleView view = (BaseTestsOutputConsoleView) console;
			resetHighlights(view.getConsole());
		}
	}

	protected void reset(ConsoleViewImpl consoleViewImpl) {
		try {
			Editor editor = consoleViewImpl.getEditor();
			removeAllHighlighters(editor);
			Filter myCustomFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl, "myCustomFilter");
			Filter myPredefinedMessageFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl,
					"myPredefinedMessageFilter");

			int lineCount = editor.getDocument().getLineCount();
			if (lineCount > 0) {
				consoleViewImpl.getHyperlinks().highlightHyperlinks(myCustomFilter, myPredefinedMessageFilter, 0,
						lineCount - 1);
			}
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	protected void removeAllHighlighters(Editor editor) {
		editor.getMarkupModel().removeAllHighlighters();
	}

}
