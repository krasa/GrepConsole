package krasa.grepconsole.utils;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.stats.StatisticsManager;

public class Rehighlighter {

	public void resetHighlights(ConsoleView console) {
		if (console instanceof ConsoleViewImpl) {
			reset((ConsoleViewImpl) console);
		} else if (console instanceof BaseTestsOutputConsoleView) {
			BaseTestsOutputConsoleView view = (BaseTestsOutputConsoleView) console;
			resetHighlights(view.getConsole());
		}
	}


	private void reset(ConsoleViewImpl consoleViewImpl) {
		Editor editor = consoleViewImpl.getEditor();
		if (editor != null) {//disposed are null - may be bug
			consoleViewImpl.rehighlightHyperlinksAndFoldings();
			StatisticsManager.resetStatisticsPanels(consoleViewImpl);
		}
	}

	public void removeAllHighlighters(Editor editor) {
		if (editor != null) {
			editor.getMarkupModel().removeAllHighlighters();
		}
	}

	public void highlight(Editor editor, Project project, Profile selectedProfile) {
		EditorHyperlinkSupport myHyperlinks = new EditorHyperlinkSupport(editor, project);
		int lineCount = editor.getDocument().getLineCount();
		if (lineCount > 0) {
			if (selectedProfile == null) {
				selectedProfile = GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
			}
			myHyperlinks.highlightHyperlinks(new HighlightingFilter(project, selectedProfile), 0, lineCount - 1);
		}
	}


}
