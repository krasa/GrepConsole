package krasa.grepconsole.plugin;

import krasa.grepconsole.GrepFilterService;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

public class EditorHighlightAction extends HighlightManipulationAction {
	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		Project project = e.getProject();
		if (editor != null && project != null) {
			boolean b = ShowSettingsUtil.getInstance().editConfigurable(e.getProject(),
					GrepConsoleApplicationComponent.getInstance());
			if (b) {
                removeAllHighlighters(editor);
                highlight(editor, project);
			}

		}
	}

    private void highlight(Editor editor, Project project) {
		EditorHyperlinkSupport myHyperlinks = new EditorHyperlinkSupport(editor, project);
		int lineCount = editor.getDocument().getLineCount();
		if (lineCount > 0) {
            myHyperlinks.highlightHyperlinks(getGrepFilter(project), getPredefinedMessageFilter(), 0, lineCount - 1);
		}
	}

    private GrepFilterService getGrepFilter(Project project) {
        return GrepConsoleApplicationComponent.getInstance().getGrepFilter(project);
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
