package krasa.grepconsole.action;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColorPicker;
import krasa.grepconsole.model.*;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.Rehighlighter;
import krasa.grepconsole.utils.Utils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

public class AddHighlightAction extends HighlightManipulationAction {
	public AddHighlightAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	private static JComponent rootComponent(Project project) {
		if (project != null) {
			IdeFrame frame = WindowManager.getInstance().getIdeFrame(project);
			if (frame != null)
				return frame.getComponent();
		}

		JFrame frame = WindowManager.getInstance().findVisibleFrame();
		return frame != null ? frame.getRootPane() : null;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final ConsoleView consoleView = getConsoleView(e);
		if (consoleView != null) {
			try {
				String string = Utils.getString(e);
				if (string == null)
					return;

				Method[] methods = ColorPicker.class.getMethods();
				Color color = null;
				boolean found = false;
				// Color color = ColorPicker.showDialog(rootComponent(getEventProject(e)), "Background color",
				// Color.CYAN,
				// true, null, true);
				for (Method method : methods) {
					if (method.getName().equals("showDialog")) {
						color = (Color) method.invoke(null, rootComponent(getEventProject(e)), "Background color",
								Color.CYAN, true, null, true);
						found = true;
						break;
					}
				}
				if (!found) {
					throw new IllegalStateException("plugin is broken, please report this");
				}

				if (color == null) {
					return;
				}

				add(consoleView, string, color);

			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		}
	}

	protected void add(ConsoleView consoleView, String string, Color color) {
		addExpressionItem(string, color, ServiceManager.getInstance().getProfile(consoleView));
		ServiceManager.getInstance().resetSettings();
		new Rehighlighter().resetHighlights(consoleView);
	}

	private void addExpressionItem(String string, Color color, final Profile profile) {
		GrepStyle style = new GrepStyle();
		style.setForegroundColor(new GrepColor(Color.BLACK));
		style.setBackgroundColor(new GrepColor(color));
		java.util.List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		GrepExpressionGroup group = grepExpressionGroups.get(0);
		group.getGrepExpressionItems().add(0,
				new GrepExpressionItem().grepExpression(string).style(style).highlightOnlyMatchingText(
						true).operationOnMatch(Operation.CONTINUE_MATCHING));
	}

	protected ConsoleView getConsoleView(AnActionEvent e) {
		return e.getData(LangDataKeys.CONSOLE_VIEW);
	}

	@Override
	public void update(AnActionEvent e) {
		Presentation presentation = e.getPresentation();
		final boolean enabled = getConsoleView(e) != null;
		boolean selectedText = Utils.isSelectedText(e);
		presentation.setEnabled(selectedText && enabled);
		presentation.setVisible(selectedText && enabled);
	}

	@Override
	public void applySettings() {
	}
}
