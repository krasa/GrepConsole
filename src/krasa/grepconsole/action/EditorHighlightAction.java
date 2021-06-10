package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.utils.Rehighlighter;

public class EditorHighlightAction extends HighlightManipulationAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = e.getProject();
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor != null && project != null) {
			MyConfigurable instance = new MyConfigurable(e.getProject());
			instance.setCurrentAction(this);
			boolean b = ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), instance);
			if (b) {
				Profile selectedProfile = instance.getSelectedProfile();

				Rehighlighter rehighlighter = new Rehighlighter();
				rehighlighter.removeAllHighlighters(editor);
				rehighlighter.highlight(editor, project, selectedProfile);
			}
			instance.setCurrentAction(null);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Project project = e.getProject();
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		e.getPresentation().setEnabled(project != null && editor != null);
	}

	@Override
	public void applySettings() {

	}
}
