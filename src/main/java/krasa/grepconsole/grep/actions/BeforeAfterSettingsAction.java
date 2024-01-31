package krasa.grepconsole.grep.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.GrepBeforeAfterModel;
import krasa.grepconsole.grep.gui.GrepBeforeAfterSettingsDialog;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BeforeAfterSettingsAction extends MyDumbAwareAction implements CustomComponentAction {
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
		JButton jButton = new JButton(presentation.getText());
		jButton.setFont(JBUI.Fonts.toolbarFont());
		jButton.putClientProperty("ActionToolbar.smallVariant", true);
		jButton.setPreferredSize(new Dimension(50, jButton.getPreferredSize().height));
		final JPanel panel = new JPanel();
		presentation.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (Presentation.PROP_TEXT.equals(evt.getPropertyName())) {
					jButton.setText((String) evt.getNewValue());
					jButton.repaint();
				}
			}
		});
		panel.add(jButton);
		jButton.addActionListener((ActionEvent e) -> {
			KeyEvent event = new KeyEvent(jButton, KeyEvent.VK_ENTER, System.currentTimeMillis(), 0, 0, KeyEvent.CHAR_UNDEFINED);
			actionPerformed(AnActionEvent.createFromInputEvent(event,
					"GrepConsole-BeforeAfterSettingsAction-" + place,
					presentation, ActionToolbar.getDataContextFor(jButton)));
		});
//		comp.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent e) {
//				actionPerformed(AnActionEvent.createFromInputEvent(e,
//						"GrepConsole-BeforeAfterSettingsAction-" + place,
//						presentation, ActionToolbar.getDataContextFor(e.getComponent())));
//			}
//		});
		panel.setToolTipText(presentation.getDescription());
		jButton.setToolTipText(presentation.getDescription());
		return panel;
	}

}
