package krasa.grepconsole.grep;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.TextFieldWithStoredHistory;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBDimension;

public class GrepPanel extends JPanel implements Disposable {

	public static final NotificationGroup GROUP_DISPLAY_ID_ERROR = new NotificationGroup("Grep Console error",
			NotificationDisplayType.BALLOON, true);

	@Nullable
	private ConsoleViewImpl originalConsole;
	private final ConsoleViewImpl newConsole;
	private final GrepCopyingListener copyingListener;
	private final RunnerLayoutUi runnerLayoutUi;
	private TextFieldWithStoredHistory expression;
	private TextFieldWithStoredHistory unlessExpression;
	private JCheckBox caseSensitiveCheckBox;
	private JButton applyButton;
	private JButton reloadButton;
	private JButton sourceButton;
	private JPanel rootComponent;
	private OpenGrepConsoleAction.ApplyCallback applyCallback;

	public JPanel getRootComponent() {
		return rootComponent;
	}

	private void createUIComponents() {
		this.expression = new TextFieldWithStoredHistory("ConsoleQuickFilterPanel-expression");
		// this.expression.setBorder(JBUI.Borders.empty());
		this.expression.setMinimumAndPreferredWidth(300);
		unlessExpression = new TextFieldWithStoredHistory("ConsoleQuickFilterPanel-unlessExpression");
		unlessExpression.setMinimumAndPreferredWidth(150);
		// this.unlessExpression.setBorder(JBUI.Borders.empty());
	}

	public GrepPanel(final ConsoleViewImpl originalConsole, final ConsoleViewImpl newConsole,
			final GrepCopyingListener copyingListener, final String expression, final RunnerLayoutUi runnerLayoutUi) {
		this.originalConsole = originalConsole;
		this.newConsole = newConsole;
		this.copyingListener = copyingListener;
		this.runnerLayoutUi = runnerLayoutUi;
		this.expression.setText(expression);
		buttons(copyingListener);
	}

	protected void buttons(final GrepCopyingListener copyingListener) {
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply(expression.getText(), unlessExpression.getText());
			}
		});
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply(expression.getText(), unlessExpression.getText());
				newConsole.clear();
				if (originalConsole != null) {
					Editor editor = originalConsole.getEditor();
					Document document = editor.getDocument();
					String text = document.getText();
					for (String s : text.split("\n")) {
						copyingListener.process(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
					}
				}

			}
		});
		sourceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Content[] contents = runnerLayoutUi.getContents();
				for (Content content : contents) {
					JComponent component = content.getComponent();
					if (component == originalConsole) {
						runnerLayoutUi.selectAndFocus(content, true, true);
					} else if (isChild(component, originalConsole)) {
						runnerLayoutUi.selectAndFocus(content, true, true);
					}
				}

			}

			private boolean isChild(JComponent component, ConsoleViewImpl originalConsole) {
				return component.getComponentZOrder(originalConsole) != -1;
			}
		});
		buttonSize(sourceButton);
		buttonSize(reloadButton);
		buttonSize(applyButton);
	}

	private void buttonSize(JButton sourceButton) {
		Dimension oldSize = sourceButton.getPreferredSize();
		JBDimension newSize = new JBDimension(oldSize.width, oldSize.height - 5);
		sourceButton.setPreferredSize(newSize);
	}

	public void reset() {
		this.expression.setText(".*");
	}

	public void apply(String text, String unlessExpressionText) {
		if (applyCallback != null) {
			if (applyCallback.apply(caseSensitiveCheckBox.isSelected(), text, unlessExpressionText)) {
				expression.addCurrentTextToHistory();
				unlessExpression.addCurrentTextToHistory();
			} else {
				final Notification notification = GROUP_DISPLAY_ID_ERROR.createNotification(
						"Grep: Failed to apply RegExp", NotificationType.ERROR);
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					@Override
					public void run() {
						Notifications.Bus.notify(notification, newConsole.getProject());
					}
				});
			}
		}

	}

	@Override
	public void dispose() {
		originalConsole = null;
		applyButton.setEnabled(false);
		reloadButton.setEnabled(false);
		sourceButton.setEnabled(false);
	}

	public void setApplyCallback(OpenGrepConsoleAction.ApplyCallback applyCallback) {
		this.applyCallback = applyCallback;
	}
}
