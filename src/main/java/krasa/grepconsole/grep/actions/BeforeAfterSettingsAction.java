package krasa.grepconsole.grep.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.grep.GrepBeforeAfterModel;
import krasa.grepconsole.grep.gui.GrepBeforeAfterSettingsDialog;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BeforeAfterSettingsAction extends DumbAwareAction implements CustomComponentAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		GrepPanel grepPanel = getGrepPanel(e);
		if (grepPanel != null) {
			Project project = e.getProject();
			GrepBeforeAfterModel beforeAfterModel = grepPanel.getBeforeAfterModel();
			GrepBeforeAfterSettingsDialog grepBeforeAfterSettingsDialog = new GrepBeforeAfterSettingsDialog(project, beforeAfterModel);
			if (grepBeforeAfterSettingsDialog.showAndGet(e.getInputEvent().getComponent())) {
				grepPanel.setBeforeAfterModel(beforeAfterModel);
				e.getPresentation().setText(beforeAfterModel.toPresentationString());
			}
		}
	}

	public static GrepPanel getGrepPanel(AnActionEvent e) {
		GrepPanel grepPanel = GrepPanel.GREP_PANEL.getData(e.getDataContext());
		if (grepPanel != null) {
			return grepPanel;
		}

		ConsoleView data = e.getData(LangDataKeys.CONSOLE_VIEW);
		if (data instanceof MyConsoleViewImpl) {
			grepPanel = ((MyConsoleViewImpl) data).getGrepPanel();
		}
		return grepPanel;
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		GrepPanel grepPanel = GrepPanel.GREP_PANEL.getData(e.getDataContext());
		if (grepPanel != null) {
			GrepBeforeAfterModel beforeAfterModel = grepPanel.getBeforeAfterModel();
			String s = beforeAfterModel.toPresentationString();
			e.getPresentation().setText(s);
		}
	}


	@NotNull
	public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
		JLabel comp1 = new JLabel(presentation.getText());
		final JPanel comp = new JPanel();
		presentation.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName() == Presentation.PROP_TEXT) {
					comp1.setText((String) evt.getNewValue());
					comp1.repaint();
				}
			}
		});
		comp.add(comp1);
		comp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionPerformed(AnActionEvent.createFromInputEvent(e,
						"GrepConsole-BeforeAfterSettingsAction-" + place,
						presentation, ActionToolbar.getDataContextFor(e.getComponent())));
			}
		});
		comp.setToolTipText(presentation.getDescription());
		return comp;
	}

}
