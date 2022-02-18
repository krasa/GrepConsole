package krasa.grepconsole.grep.gui;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.WrapLayout;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.grep.GrepBeforeAfterModel;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.grep.GrepModel;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;
import krasa.grepconsole.grep.listener.GrepFilterListener;
import krasa.grepconsole.plugin.PluginState;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.FocusUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class GrepPanel extends JPanel implements Disposable, DataProvider {

	public static final DataKey<GrepPanel> GREP_PANEL = DataKey.create("GrepPanel");

	private static final Logger LOG = Logger.getInstance(GrepPanel.class);
	private final SelectSourceActionListener selectSourceActionListener;

	@Nullable
	private ConsoleView originalConsole;
	private final MyConsoleViewImpl newConsole;
	private final GrepFilter grepFilter;
	private final GrepFilterListener grepListener;
	private JPanel rootComponent;
	private JPanel expressions;
	private JPanel buttons;
	private JPanel beforeExpressions;
	private OpenGrepConsoleAction.Callback applyCallback;
	private String customTitle;
	private String cachedFullTitle;
	private GrepBeforeAfterModel beforeAfterModel = new GrepBeforeAfterModel();
	public JPanel getRootComponent() {
		return rootComponent;
	}

	private void createUIComponents() {
		WrapLayout layout = new WrapLayout(FlowLayout.LEFT, 3, 3);
		layout.setFillWidth(false);
		expressions = new JPanel(layout);
	}

	public GrepPanel(final ConsoleView originalConsole, final MyConsoleViewImpl newConsole,
					 GrepFilter grepFilter, GrepFilterListener grepListener, GrepCompositeModel grepModel, final String pattern, SelectSourceActionListener selectSourceActionListener) {
		this.originalConsole = originalConsole;
		this.newConsole = newConsole;
		this.grepFilter = grepFilter;
		this.grepListener = grepListener;
		initModel(pattern, grepModel);
		this.selectSourceActionListener = selectSourceActionListener;
		buttons.add(createToolbar("krasa.grepconsole.grep.panel").getComponent(), BorderLayout.CENTER);
		beforeExpressions.add(createToolbar("krasa.grepconsole.grep.panel.before").getComponent(), BorderLayout.CENTER);
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				final int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER && e.isAltDown()) {
					reload();
				} else if (keyCode == KeyEvent.VK_ENTER && e.isControlDown()) {
					reload();
				} else if (keyCode == KeyEvent.VK_ENTER) {
					apply();
				}
			}
		});
	}


	@NotNull
	private ActionToolbar createToolbar(String s) {
		final ActionManager actionManager = ActionManager.getInstance();

		DefaultActionGroup newGroup = new DefaultActionGroup();
		newGroup.add(actionManager.getAction(s));

		ActionToolbar actionToolbar = actionManager.createActionToolbar("GrepConsole-GrepPanel", newGroup, true);
		final ActionToolbarImpl editorToolbar = ((ActionToolbarImpl) actionToolbar);
		editorToolbar.setReservePlaceAutoPopupIcon(false);
		editorToolbar.setOpaque(false);
		editorToolbar.setBorder(new JBEmptyBorder(0, 0, 0, 0));
		editorToolbar.setTargetComponent(newConsole);
		actionToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
		return editorToolbar;
	}

	@Nullable
	public ConsoleView getOriginalConsole() {
		return originalConsole;
	}

	public MyConsoleViewImpl getConsole() {
		return newConsole;
	}

	public void initModel(String pattern, GrepCompositeModel compositeModel) {
		PluginState state = PluginState.getInstance();
		List grepHistory = state.getGrepHistory();
		expressions.removeAll();

		if (compositeModel != null) {
			customTitle = compositeModel.getCustomTitle();
			beforeAfterModel = compositeModel.getBeforeAfterModel();
		} else {
			if (!StringUtils.isEmpty(pattern)) {
				GrepModel selectedItem = null;
				for (Object grepOptionsItem : grepHistory) {
					if (grepOptionsItem instanceof GrepModel) {
						GrepModel g = (GrepModel) grepOptionsItem;
						if (g.getExpression().equals(pattern)) {
							selectedItem = g;
							break;
						}
					}
				}
				if (selectedItem == null) {
					selectedItem = new GrepModel(pattern);
				}
				compositeModel = new GrepCompositeModel(selectedItem);
			}
		}
		if (compositeModel != null) {
			state.addToHistory(compositeModel);
		} else {
			compositeModel = new GrepCompositeModel(new GrepModel(""));
		}
		if (compositeModel.getModels().isEmpty()) {
			LOG.error("grepModel.getModels() is empty: " + compositeModel);
			return;
		}
		for (GrepModel model : compositeModel.getModels()) {
			expressions.add(new MyGrepSearchTextArea(this, model));
		}
	}


	public void addExpression(String expression) {
		expressions.add(new MyGrepSearchTextArea(this, new GrepModel(expression)));
		reload();
	}


	public void reload() {
		apply();
		expressions.revalidate();
		expressions.repaint();
		newConsole.clear();
		grepListener.clear();
		beforeAfterModel.clear();
		GrepUtils.grepThroughExistingText(originalConsole, grepFilter, grepListener);
	}

	public GrepCompositeModel createModel() {
		GrepCompositeModel grepCompositeModel = new GrepCompositeModel();
		Component[] components = expressions.getComponents();
		for (Component component : components) {
			if (component instanceof MyGrepSearchTextArea) {
				GrepModel grepModel = ((MyGrepSearchTextArea) component).grepModel();
				grepCompositeModel.add(grepModel);
			}
		}
		grepCompositeModel.setCustomTitle(customTitle);
		grepCompositeModel.setBeforeAfterModel(beforeAfterModel);
		cachedFullTitle = grepCompositeModel.getFullTitle();
		return grepCompositeModel;
	}

	public GrepBeforeAfterModel getBeforeAfterModel() {
		return beforeAfterModel;
	}

	public void updateTabDescription() {
		if (applyCallback != null) {
			applyCallback.updateTabDescription(newConsole, createModel());
		}
	}

	public void apply() {
		if (applyCallback != null) {
			try {
				GrepCompositeModel model = createModel();
				applyCallback.apply(newConsole, model);
				PluginState.getInstance().addToHistory(model);
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


	@Override
	public void dispose() {
		ServiceManager.getInstance().unregisterGrepPanel(this);
		originalConsole = null;
//		applyButton.setEnabled(false);
//		reloadButton.setEnabled(false);
//		sourceButton.setEnabled(false);
	}

	public void setApplyCallback(OpenGrepConsoleAction.Callback applyCallback) {
		this.applyCallback = applyCallback;
	}

	public void selectSource() {
		selectSourceActionListener.actionPerformed(null);
	}

	@Override
	public @Nullable
	Object getData(@NotNull @NonNls String s) {
		if (GREP_PANEL.is(s)) {
			return this;
		}
		return null;
	}

	public void setCustomTitle(String customTitle) {
		this.customTitle = customTitle;
	}

	public String getCustomTitle() {
		return customTitle;
	}

	public ConsoleView getSourceConsole() {
		return originalConsole;
	}

	public String getCachedFullTitle() {
		return cachedFullTitle;
	}

	public void tabSelected() {
		//layout fix
		expressions.revalidate();
	}

	public void setBeforeAfterModel(@NotNull GrepBeforeAfterModel beforeAfterModel) {
		this.beforeAfterModel = beforeAfterModel;
		reload();
	}

	public void textExpressionChanged() {
		PluginState instance = PluginState.getInstance();
		if (instance.isAutoReloadGrepModel()) {
			reload();
		} else if (instance.isAutoApplyGrepModel()) {
			apply();
		}
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
