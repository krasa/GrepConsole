package krasa.grepconsole.grep.gui;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.TextFieldWithStoredHistory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBDimension;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.grep.GrepModel;
import krasa.grepconsole.grep.OpenGrepConsoleAction;
import krasa.grepconsole.grep.listener.GrepFilterListener;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.FocusUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.PatternSyntaxException;

public class GrepPanel extends JPanel implements Disposable {
	private static final Logger LOG = Logger.getInstance(GrepPanel.class);

	@Nullable
	private ConsoleView originalConsole;
	private final MyConsoleViewImpl newConsole;
	private final GrepFilter grepFilter;
	private final GrepFilterListener grepListener;
	private MyTextFieldWithStoredHistory expressionTextField;
	private TextFieldWithStoredHistory unlessExpressionTextField;
	private JBCheckBox matchCase;
	private JButton applyButton;
	private JButton reloadButton;
	private JButton sourceButton;
	private JPanel rootComponent;
	private JBCheckBox wholeLine;
	private JBCheckBox regex;
	private JLabel expLabel;
	private JLabel unlessLabel;
	private JButton clearHistory;
	private JPanel expressions;
	private OpenGrepConsoleAction.Callback applyCallback;
	private GrepModel grepModel;
	private boolean initialized;

	public JPanel getRootComponent() {
		return rootComponent;
	}

	private void createUIComponents() {
		expressionTextField = new MyTextFieldWithStoredHistory("ConsoleQuickFilterPanel-expression");
		expressionTextField.setHistorySize(50);
		expressionTextField.setMinimumAndPreferredWidth(100);
		unlessExpressionTextField = new TextFieldWithStoredHistory("ConsoleQuickFilterPanel-unlessExpression") {
//			@Override
//			public Dimension getPreferredSize() {
//				Dimension dim = super.getPreferredSize();
//				Insets m = getInsets();
//				int i = getFontMetrics(getFont()).stringWidth(getText());
//				dim.width = i + 40 + m.left + m.right;
//				return dim;
//			}
		};
		unlessExpressionTextField.setMinimumAndPreferredWidth(100);
		unlessExpressionTextField.setHistorySize(50);
		// this.unlessExpression.setBorder(JBUI.Borders.empty());
	}

	public GrepPanel(final ConsoleView originalConsole, final MyConsoleViewImpl newConsole,
					 GrepFilter grepFilter, GrepFilterListener grepListener, GrepModel grepModel, final String pattern, SelectSourceActionListener selectSourceActionListener) {
		this.originalConsole = originalConsole;
		this.newConsole = newConsole;
		this.grepFilter = grepFilter;
		this.grepListener = grepListener;
		initModel(pattern, grepModel);
		actions();
		buttons(selectSourceActionListener);
		expressionTextField.addItemListener(new ItemChangeListener());

	}

	@Nullable
	public ConsoleView getOriginalConsole() {
		return originalConsole;
	}

	public MyConsoleViewImpl getConsole() {
		return newConsole;
	}

	public void initModel(String pattern, GrepModel grepModel) {
		//reset initializes model
		java.util.List<GrepOptionsItem> history = expressionTextField.reset();
		if (grepModel != null) {
			this.expressionTextField.addCurrentTextToHistory(grepModel);
		}

		if (!StringUtils.isEmpty(pattern)) {
			GrepOptionsItem selectedItem = null;
			for (GrepOptionsItem grepOptionsItem : history) {
				if (grepOptionsItem.getExpression().equals(pattern)) {
					selectedItem = grepOptionsItem;
					break;
				}
			}
			if (selectedItem == null) {
				selectedItem = new GrepOptionsItem().setExpression(pattern);
			}
			this.expressionTextField.setSelectedItem(selectedItem);
		}
		updateGrepOptions((GrepOptionsItem) expressionTextField.getSelectedItem());

		expLabel.setLabelFor(expressionTextField);
		unlessLabel.setLabelFor(unlessExpressionTextField);
		grepModel = new GrepModel(matchCase.isSelected(), wholeLine.isSelected(),
				regex.isSelected(), expressionTextField.getText(), unlessExpressionTextField.getText());
		this.expressionTextField.addCurrentTextToHistory(grepModel);
		this.grepModel = new GrepModel(matchCase.isSelected(),
				wholeLine.isSelected(), regex.isSelected(), expressionTextField.getText(),
				unlessExpressionTextField.getText());
		initialized = true;
	}

	public void addExpression(String expression) {
		String exp = StringUtil.escapeToRegexp(expression);
		if (wholeLine.isSelected()) {
			exp = ".*" + exp + ".*";
		}
		if (regex.isSelected()) {
			String current = expressionTextField.getText();
			String ex;
			if (!current.startsWith("(") && !current.endsWith(")")) {
				ex = "(" + current + "|" + exp + ")";
			} else {
				ex = current.substring(0, current.length() - 1) + "|" + exp + ")";
			}
			expressionTextField.setText(ex);
		} else {
			regex.setSelected(true);
			String current = expressionTextField.getText();
			expressionTextField.setText("(" + current + "|" + exp + ")");
		}

		reload();
	}

//	public void addExpression(String expression) {
//		if (regex.isSelected()) {
//			expression = StringUtil.escapeToRegexp(expression);
//		}
//		MyTextFieldWithStoredHistory field = new MyTextFieldWithStoredHistory("ConsoleQuickFilterPanel-expression");
//		field.setHistorySize(50);
//		field.setMinimumAndPreferredWidth(100);
//		field.setText(expression);
//		expressions.add(field);
//		reload();
//	}

	class ItemChangeListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent event) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				GrepOptionsItem item = (GrepOptionsItem) event.getItem();
				updateGrepOptions(item);
			}
		}
	}

	protected void updateGrepOptions(GrepOptionsItem selectedItem) {
		if (selectedItem != null) {
			wholeLine.setSelected(selectedItem.isWholeLine());
			regex.setSelected(selectedItem.isRegex());
			matchCase.setSelected(selectedItem.isCaseSensitive());
		}
	}

	protected void actions() {
		KeyAdapter reload = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				final int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER && e.isAltDown()) {
					reloadButton.doClick();
				} else if (keyCode == KeyEvent.VK_ENTER && e.isControlDown()) {
					reloadButton.doClick();
				} else if (keyCode == KeyEvent.VK_ENTER) {
					applyButton.doClick();
				}
			}
		};
		expressionTextField.addKeyboardListener(reload);
		unlessExpressionTextField.addKeyboardListener(reload);
	}

	protected void buttons(SelectSourceActionListener selectSourceActionListener) {
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		clearHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				expressionTextField.clearHistory();

				PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
				propertiesComponent.unsetValue("ConsoleQuickFilterPanel-unlessExpression");
				unlessExpressionTextField.reset();
			}
		});
		if (selectSourceActionListener != null) {
			sourceButton.addActionListener(selectSourceActionListener);
			buttonSize(sourceButton);
		}
		buttonSize(reloadButton);
		buttonSize(applyButton);
		buttonSize(clearHistory);
	}

	protected void reload() {
		apply();
		newConsole.clear();
		GrepUtils.grepThroughExistingText(originalConsole, grepFilter, grepListener);
	}

	public GrepModel getModel() {
		return grepModel;
	}

	public void apply() {
		if (applyCallback != null) {
			GrepModel grepModel = new GrepModel(matchCase.isSelected(),
					wholeLine.isSelected(), regex.isSelected(), expressionTextField.getText(),
					unlessExpressionTextField.getText());

			try {
				applyCallback.apply(grepModel);
				expressionTextField.addCurrentTextToHistory(grepModel);
				unlessExpressionTextField.addCurrentTextToHistory();
				this.grepModel = grepModel;
			} catch (PatternSyntaxException e) {
				Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Grep Console error")
						.createNotification("Grep: invalid regexp", NotificationType.WARNING);
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					@Override
					public void run() {
						Notifications.Bus.notify(notification, newConsole.getProject());
					}
				});
			}
		}

	}

	private void buttonSize(JButton sourceButton) {
		Dimension oldSize = sourceButton.getPreferredSize();
		JBDimension newSize = new JBDimension(oldSize.width, oldSize.height - 5);
		sourceButton.setPreferredSize(newSize);
	}

	@Override
	public void dispose() {
		ServiceManager.getInstance().unregisterGrepPanel(this);
		originalConsole = null;
		applyButton.setEnabled(false);
		reloadButton.setEnabled(false);
		sourceButton.setEnabled(false);
	}

	public void setApplyCallback(OpenGrepConsoleAction.Callback applyCallback) {
		this.applyCallback = applyCallback;
		apply();
	}

	public static class SelectSourceActionListener implements ActionListener {
		private RunnerLayoutUi runnerLayoutUi;
		private ConsoleView originalConsole;
		private ToolWindow toolWindow;

		public SelectSourceActionListener(ConsoleView originalConsole, RunnerLayoutUi runnerLayoutUi, ToolWindow toolWindow) {
			this.runnerLayoutUi = runnerLayoutUi;
			this.originalConsole = originalConsole;
			this.toolWindow = toolWindow;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (runnerLayoutUi != null) {
				FocusUtils.selectAndFocusSubTab(runnerLayoutUi, originalConsole);
			} else {
				Content[] contents = toolWindow.getContentManager().getContents();
				for (Content content : contents) {
					if (OpenGrepConsoleAction.isSameConsole(content, originalConsole)) {
						toolWindow.getContentManager().setSelectedContent(content);
					}
				}
			}
		}

	}
}
