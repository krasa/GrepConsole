package krasa.grepconsole.utils;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.plugin.ReflectionUtils;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsManager;
import org.jetbrains.annotations.Nullable;

import static krasa.grepconsole.action.HighlightManipulationAction.FILTER;

public class Rehighlighter {

	public void resetHighlights(ConsoleView console) {
		if (console instanceof ConsoleViewImpl) {
			reset((ConsoleViewImpl) console);
		} else if (console instanceof BaseTestsOutputConsoleView) {
			BaseTestsOutputConsoleView view = (BaseTestsOutputConsoleView) console;
			resetHighlights(view.getConsole());
		}
	}


	public void removeAllHighlighters(Editor editor) {
		editor.getMarkupModel().removeAllHighlighters();
	}

	private void reset(ConsoleViewImpl consoleViewImpl) {
		Editor editor = consoleViewImpl.getEditor();
		removeAllHighlighters(editor);
		highlightAll(consoleViewImpl, editor);
		StatisticsManager.resetStatisticsPanels(consoleViewImpl);
	}

	private void highlightAll(ConsoleViewImpl consoleViewImpl, Editor editor) {
		try {
			Filter myCustomFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl, "myFilters");

			int lineCount = editor.getDocument().getLineCount();
			if (lineCount > 0) {
				consoleViewImpl.getHyperlinks().highlightHyperlinks(myCustomFilter, FILTER, 0, lineCount - 1);
			}
		} catch (NoSuchFieldException e1) {
			throw new RuntimeException("IJ API was probably changed, update the plugin or report it", e1);
		}
	}


	public void highlight(Editor editor, Project project) {
		EditorHyperlinkSupport myHyperlinks = new EditorHyperlinkSupport(editor, project);
		int lineCount = editor.getDocument().getLineCount();
		if (lineCount > 0) {
			myHyperlinks.highlightHyperlinks(getGrepFilter(project), getPredefinedMessageFilter(), 0, lineCount - 1);
		}
	}

	private GrepHighlightFilter getGrepFilter(Project project) {
		return ServiceManager.getInstance().createHighlightFilter(project, null);
	}

	private Filter getPredefinedMessageFilter() {
		return new Filter() {
			@Nullable
			@Override
			public Result applyFilter(String line, int entireLength) {
				return null;
			}
		};
	}

}
