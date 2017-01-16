package krasa.grepconsole.action;

import javax.swing.*;

import krasa.grepconsole.plugin.ReflectionUtils;
import krasa.grepconsole.stats.StatisticsManager;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class HighlightManipulationAction extends DumbAwareAction {

	public static final Filter FILTER = new Filter() {
		@Nullable
		@Override
		public Result applyFilter(String s, int i) {
			return null;
		}
	};

	public HighlightManipulationAction() {
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

	private void reset(ConsoleViewImpl consoleViewImpl) {
		Editor editor = consoleViewImpl.getEditor();
		removeAllHighlighters(consoleViewImpl, editor);
		highlight(consoleViewImpl, editor);
		StatisticsManager.resetStatisticsPanels(consoleViewImpl);
	}

	private void highlight(ConsoleViewImpl consoleViewImpl, Editor editor) {
		try {
			Filter myCustomFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl, "myCustomFilter");
			Filter myPredefinedMessageFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl,
					"myPredefinedMessageFilter");

			int lineCount = editor.getDocument().getLineCount();
			if (lineCount > 0) {
				consoleViewImpl.getHyperlinks().highlightHyperlinks(myCustomFilter, myPredefinedMessageFilter, 0,
						lineCount - 1);
			}
		} catch (NoSuchFieldException e) {
			highlightWithNewAPI(consoleViewImpl, editor);
		}
	}

	private void highlightWithNewAPI(ConsoleViewImpl consoleViewImpl, Editor editor) {
		try {
			Filter myCustomFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl, "myFilters");

			int lineCount = editor.getDocument().getLineCount();
			if (lineCount > 0) {
				consoleViewImpl.getHyperlinks().highlightHyperlinks(myCustomFilter, FILTER, 0, lineCount - 1);
			}
		} catch (NoSuchFieldException e1) {
			throw new RuntimeException("IJ API was probably changed, update the plugin", e1);
		}
	}

	protected void removeAllHighlighters(ConsoleViewImpl consoleViewImpl, Editor editor) {
		editor.getMarkupModel().removeAllHighlighters();
	}

	public abstract void applySettings();
}
