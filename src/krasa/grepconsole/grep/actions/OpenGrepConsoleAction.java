package krasa.grepconsole.grep.actions;

import com.intellij.build.BuildView;
import com.intellij.execution.console.ConsoleViewWrapperBase;
import com.intellij.execution.console.DuplexConsoleView;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.testframework.ui.TestResultsPanel;
import com.intellij.execution.ui.*;
import com.intellij.execution.ui.layout.impl.RunnerLayoutUiImpl;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.*;
import com.intellij.util.NotNullFunction;
import com.intellij.util.SmartList;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.grep.PinnedGrepConsolesState;
import krasa.grepconsole.grep.gui.GrepPanel;
import krasa.grepconsole.grep.gui.GrepUtils;
import krasa.grepconsole.grep.listener.GrepFilterListener;
import krasa.grepconsole.grep.listener.GrepFilterSyncListener;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepProjectComponent;
import krasa.grepconsole.plugin.ReflectionUtils;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

//import krasa.grepconsole.grep.listener.GrepFilterAsyncListener;

public class OpenGrepConsoleAction extends DumbAwareAction {

	private static final Logger LOG = Logger.getInstance(OpenGrepConsoleAction.class);
	public static final int MAX_TITLE_LENGTH = 40;

	public OpenGrepConsoleAction() {
	}

	public OpenGrepConsoleAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project eventProject = getEventProject(e);
		ConsoleView parentConsoleView = (ConsoleView) getConsoleView(e);
		if (parentConsoleView == null) {
			return;
		}
		String expression = getExpression(e);
		GrepProjectComponent grepProjectComponent = GrepProjectComponent.getInstance(eventProject);
		try {
			grepProjectComponent.pinReopenerEnabled = false;
			createGrepConsole(e, eventProject, null, parentConsoleView, null, expression, UUID.randomUUID().toString(), null);
		} finally {
			grepProjectComponent.pinReopenerEnabled = true;
		}

	}

	public ConsoleViewImpl createGrepConsole(AnActionEvent e, Project project, PinnedGrepConsolesState.RunConfigurationRef key, ConsoleView parentConsoleView,
											 @Nullable GrepCompositeModel grepModel, @Nullable String expression, String consoleUUID, String contentType) {
		String title = grepModel != null ? grepModel.getTitle() : title(expression);
		boolean focusTab = e != null;
		if (!(parentConsoleView instanceof JComponent)) {
			throw new RuntimeException("console not supported, must be instance of JComponent: " + parentConsoleView);
		}
		JComponent parentConsoleView_JComponent = (JComponent) parentConsoleView;

		final GrepFilter grepFilter = ServiceManager.getInstance().getGrepFilter(parentConsoleView);
		if (grepFilter == null) {
			throw new IllegalStateException("Console not supported: " + parentConsoleView);
		}
		ConsoleView topParentConsoleView = getTopParentConsoleView(parentConsoleView);

		RunContentDescriptor runContentDescriptor = getRunContentDescriptor(project, topParentConsoleView);
		ToolWindow toolWindow = null;
		if (e != null) {
			toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
		} else {
			toolWindow = findToolWindow(parentConsoleView, project);
		}
		if (runContentDescriptor != null) {
			if (key == null) {
				key = new PinnedGrepConsolesState.RunConfigurationRef(runContentDescriptor.getDisplayName(), runContentDescriptor.getIcon());
			}
		} else if (key == null && toolWindow != null) {
			key = PinnedGrepConsolesState.RunConfigurationRef.toKey(toolWindow);
		}

		RunnerLayoutUi runnerLayoutUi = getRunnerLayoutUi(project, runContentDescriptor, topParentConsoleView);
		if (runnerLayoutUi == null && toolWindow == null) { // should not happen
			throw new IllegalStateException("runnerLayoutUi, toolWindow == null");
		}


		if (contentType == null && runnerLayoutUi != null) {
			contentType = getContentType(topParentConsoleView, runnerLayoutUi.getContents());
		}
		/** TODO a little different than java.lang.Runnable#getContentType*/
		if (contentType == null && runnerLayoutUi != null) {
			contentType = ExecutionConsole.CONSOLE_CONTENT_ID;
		}
		if (contentType == null) {
			contentType = toolWindow.getId();
		}

		LightProcessHandler myProcessHandler = new LightProcessHandler();

		MyConsoleViewImpl newConsole = createConsoleWithoutInputFilter(project, parentConsoleView, myProcessHandler);
		Profile profile = ServiceManager.getInstance().getProfile(parentConsoleView);
		ServiceManager.getInstance().profileChanged(newConsole, profile);

		final GrepFilterListener grepListener = new GrepFilterSyncListener(myProcessHandler, project, profile);

		GrepPanel.SelectSourceActionListener selectSourceActionListener = new GrepPanel.SelectSourceActionListener(parentConsoleView, runnerLayoutUi,
				toolWindow);
		final GrepPanel quickFilterPanel = new GrepPanel(parentConsoleView, newConsole, grepFilter, grepListener, grepModel, expression,
				selectSourceActionListener);
		newConsole.setGrepPanel(quickFilterPanel);
		ServiceManager.getInstance().registerChildGrepConsole(parentConsoleView, newConsole);

		DefaultActionGroup actions = new DefaultActionGroup();
		String parentConsoleUUID = getConsoleUUID(parentConsoleView_JComponent);

		PinnedGrepConsolesState.RunConfigurationRef runConfigurationRef = null;
		PinAction pinAction = null;
		if (key != null) {
			pinAction = new PinAction(project, quickFilterPanel, parentConsoleUUID, consoleUUID, profile, key, contentType);
			runConfigurationRef = pinAction.getRunConfigurationRef();
			actions.add(pinAction);
		}

		final MyJPanel consolePanel = createConsolePanel(runnerLayoutUi, newConsole, actions, quickFilterPanel, consoleUUID);
		for (AnAction action : newConsole.createConsoleActions()) {
			actions.add(action);
		}

		final Content tab;
		if (runnerLayoutUi != null) {
			tab = runnerLayoutUi.createContent(contentType, consolePanel, title, AllIcons.General.Filter, consolePanel);
			runnerLayoutUi.addContent(tab);
			runnerLayoutUi.addListener(new MyContentManagerListener(tab, quickFilterPanel), tab);
			try {
				if (focusTab) {
					runnerLayoutUi.selectAndFocus(tab, true, true);
				}
			} catch (Exception ex) {
				LOG.warn(ex);
			}
			actions.add(new MyCloseAction(tab, runnerLayoutUi));
		} else {
			tab = ContentFactory.SERVICE.getInstance().createContent(consolePanel, title, true);
			RunContentDescriptor contentDescriptor = new RunContentDescriptor(newConsole, myProcessHandler, consolePanel, title);
			tab.setDisposer(contentDescriptor);
			ContentManager contentManager = toolWindow.getContentManager();
			Content selectedContent = contentManager.getSelectedContent();
			if (selectedContent != null && StringUtils.isBlank(selectedContent.getDisplayName())) {
				selectedContent.setDisplayName("Main");
			}
			MyContentManagerListener contentManagerListener = new MyContentManagerListener(tab, quickFilterPanel);
			contentManager.addContentManagerListener(contentManagerListener);
			Disposer.register(tab, () -> contentManager.removeContentManagerListener(contentManagerListener));
			contentManager.addContent(tab);
			if (focusTab) {
				contentManager.setSelectedContent(tab);
			}
			actions.add(new MyCloseAction(tab, contentManager));
		}

		String finalContentType = contentType;
		PinnedGrepConsolesState.RunConfigurationRef finalRunConfigurationRef = runConfigurationRef;
		quickFilterPanel.setApplyCallback(new Callback() {
			@Override
			public void apply(MyConsoleViewImpl current, GrepCompositeModel grepModel) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("apply callback");
				}
				grepListener.modelUpdated(grepModel);
				tab.setDisplayName(title(grepModel.getTitle()));

				updateTabDescription(current, grepModel);

				if (finalRunConfigurationRef != null) {
					PinnedGrepConsolesState.getInstance(project).update(finalRunConfigurationRef, parentConsoleUUID, consoleUUID, grepModel, finalContentType,
							false);
				}
			}

			@Override
			public void updateTabDescription(MyConsoleViewImpl current, GrepCompositeModel model) {
				String description;
				if (parentConsoleView instanceof MyConsoleViewImpl) {
					GrepPanel panel = ((MyConsoleViewImpl) parentConsoleView).getGrepPanel();
					description = "Source: '" + panel.getCachedFullTitle() + "'";
				} else {
					description = "Source: Main Console";
				}
				tab.setDescription(description);

				updateChildTabDescription(current);
			}

			private void updateChildTabDescription(MyConsoleViewImpl current) {
				List<MyConsoleViewImpl> childGreps = ServiceManager.getInstance().findChildGreps(current);
				for (MyConsoleViewImpl childGrep : childGreps) {
					childGrep.getGrepPanel().updateTabDescription();
				}
			}

		});

		GrepUtils.grepThroughExistingText(parentConsoleView, grepFilter, grepListener);

		grepFilter.addListener(grepListener);

		// Disposer.register(parentDisposable, tab);
		Disposer.register(tab, consolePanel);

		Disposable parentDisposable;
		if (runContentDescriptor != null) {
			parentDisposable = runContentDescriptor;
		} else {
			parentDisposable = parentConsoleView;
		}
		AtomicBoolean parentDisposed = new AtomicBoolean();
		Disposer.register(parentDisposable, new Disposable() {
			@Override
			public void dispose() {
				parentDisposed.set(true);
			}
		});
		PinAction finalPinAction = pinAction;
		Disposer.register(tab, new Disposable() {
			@Override
			public void dispose() {
				if (!parentDisposed.get()) {
					if (finalPinAction != null) {
						finalPinAction.setSelected(false);
					}
				}
			}
		});

		Disposer.register(consolePanel, newConsole);
		Disposer.register(consolePanel, grepListener);
		Disposer.register(consolePanel, quickFilterPanel);
		Disposer.register(consolePanel, new Disposable() {
			@Override
			public void dispose() {
				grepFilter.removeListener(grepListener);
			}
		});

		Disposable inactiveTitleDisposer;
		Container parent = parentConsoleView_JComponent.getParent();
		if (parent instanceof MyJPanel && !Disposer.isDisposed((MyJPanel) parent)) {
			inactiveTitleDisposer = (MyJPanel) parent;
		} else {
			inactiveTitleDisposer = parentConsoleView;
		}

		Disposer.register(inactiveTitleDisposer, new Disposable() {
			@Override
			public void dispose() {
				// dispose chained grep consoles
				Disposer.dispose(quickFilterPanel);
				tab.setDisplayName(title(tab.getDisplayName()) + " (Inactive)");
			}
		});
		return newConsole;
	}


	@Nullable
	public String getConsoleUUID(JComponent parentConsoleView) {
		String parentConsoleUUID = null;
		Container parent = parentConsoleView.getParent();
		if (parent instanceof MyJPanel) {
			parentConsoleUUID = ((MyJPanel) parent).getConsoleUUID();
		}
		return parentConsoleUUID;
	}

	public static String title(String expression) {
		if (StringUtils.isEmpty(expression)) {
			expression = "---";
		}
		if (expression.length() > MAX_TITLE_LENGTH + 10) {
			return StringUtils.substring(expression, 0, MAX_TITLE_LENGTH) + "...";
		}
		return expression;
	}

	@NotNull
	protected String getExpression(AnActionEvent e) {
		String s = Utils.getSelectedString(e);
		if (s == null)
			s = "";
		if (s.endsWith("\n")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	@Nullable
	protected String getContentType(ConsoleView consoleView, Content[] contents) {
		for (Content content : contents) {
			if (isSameConsole(content, consoleView)) {
				return RunnerLayoutUiImpl.CONTENT_TYPE.get(content);
			}
		}
		return null;
	}

	public interface Callback {

		void apply(MyConsoleViewImpl current, GrepCompositeModel grepModel);

		void updateTabDescription(MyConsoleViewImpl current, GrepCompositeModel model);
	}

	public static RunnerLayoutUi getRunnerLayoutUi(Project eventProject, @Nullable RunContentDescriptor runContentDescriptor, ConsoleView parentConsoleView) {
		RunnerLayoutUi runnerLayoutUi = null;

		if (runContentDescriptor != null) {
			runnerLayoutUi = runContentDescriptor.getRunnerLayoutUi();
		}

		if (runnerLayoutUi == null) {
			XDebugSession debugSession = XDebuggerManager.getInstance(eventProject).getDebugSession(parentConsoleView);
			if (debugSession != null) {
				runnerLayoutUi = debugSession.getUI();
			}
			if (debugSession == null) {
				XDebugSession currentSession = XDebuggerManager.getInstance(eventProject).getCurrentSession();
				if (currentSession != null) {
					runnerLayoutUi = currentSession.getUI();
				}
			}
		}

		if (runnerLayoutUi == null && parentConsoleView instanceof JComponent) {
			Container parent = ((JComponent) parentConsoleView).getParent();
			if (parent instanceof MyJPanel) {
				runnerLayoutUi = ((MyJPanel) parent).runnerLayoutUi;
			}
		}
		return runnerLayoutUi;
	}

	public static RunContentDescriptor getRunContentDescriptor(Project project, ConsoleView consoleView) {
		Collection<RunContentDescriptor> descriptors = findRunningConsole(project, dom -> {
			if (isSameConsole(dom, consoleView, true)) {
				return true;
			}
			RunnerLayoutUi runnerLayoutUi = dom.getRunnerLayoutUi();
			if (runnerLayoutUi != null) {
				Content[] contents = runnerLayoutUi.getContents();
				for (Content content : contents) {
					if (isSameConsole(content, consoleView)) {
						return true;
					}
				}
			}
			return false;
		});
		if (!descriptors.isEmpty()) {
			if (descriptors.size() == 1) {
				RunContentDescriptor runContentDescriptor = (RunContentDescriptor) descriptors.toArray()[0];
				if (runContentDescriptor != null) {
					return runContentDescriptor;
				}
			} else {
				LOG.warn("more than 1 RunContentDescriptor " + descriptors);
			}
		}

		return null;
	}

	/**
	 * com.intellij.execution.ExecutionHelper#findRunningConsole
	 */
	public static Collection<RunContentDescriptor> findRunningConsole(@NotNull Project project,
																	  @NotNull NotNullFunction<? super RunContentDescriptor, Boolean> descriptorMatcher) {
		final Ref<Collection<RunContentDescriptor>> ref = new Ref<>();

		final Runnable computeDescriptors = () -> {
			RunContentManager contentManager = RunContentManager.getInstanceIfCreated(project);
			if (contentManager == null) {
				ref.set(Collections.emptyList());
				return;
			}
			final RunContentDescriptor selectedContent = contentManager.getSelectedContent();
			if (selectedContent != null) {
				final ToolWindow toolWindow = contentManager.getToolWindowByDescriptor(selectedContent);
				if (toolWindow != null && toolWindow.isVisible()) {
					if (descriptorMatcher.fun(selectedContent)) {
						ref.set(Collections.singletonList(selectedContent));
						return;
					}
				}
			}

			final List<RunContentDescriptor> result = new SmartList<>();
			for (RunContentDescriptor runContentDescriptor : contentManager.getAllDescriptors()) {
				if (descriptorMatcher.fun(runContentDescriptor)) {
					result.add(runContentDescriptor);
				}
			}
			ref.set(result);
		};

		if (ApplicationManager.getApplication().isDispatchThread()) {
			computeDescriptors.run();
		} else {
			LOG.assertTrue(!ApplicationManager.getApplication().isReadAccessAllowed());
			ApplicationManager.getApplication().invokeAndWait(computeDescriptors);
		}

		return ref.get();
	}

	static boolean broken = false;

	public static boolean isSameConsole(RunContentDescriptor dom, ExecutionConsole consoleView, boolean orChild) {
		ExecutionConsole executionConsole = dom.getExecutionConsole();
		if (executionConsole instanceof BaseTestsOutputConsoleView) {
			executionConsole = ((BaseTestsOutputConsoleView) executionConsole).getConsole();
		} else if (executionConsole instanceof DuplexConsoleView) {
			DuplexConsoleView duplexConsoleView = (DuplexConsoleView) executionConsole;
			if (duplexConsoleView.isPrimaryConsoleEnabled()) {
				executionConsole = duplexConsoleView.getPrimaryConsoleView();
			} else {
				executionConsole = duplexConsoleView.getSecondaryConsoleView(); // no idea what that is
			}
		} else if (executionConsole instanceof ConsoleViewWrapperBase) { // javaee like google app
			executionConsole = ((ConsoleViewWrapperBase) executionConsole).getDelegate();
		} else if (executionConsole instanceof BuildView) {
			try {
				executionConsole = (ExecutionConsole) ReflectionUtils.getPropertyValue(executionConsole, "myExecutionConsole");
				if (executionConsole instanceof BaseTestsOutputConsoleView) {
					executionConsole = ((BaseTestsOutputConsoleView) executionConsole).getConsole();
				}
			} catch (Exception e) {
				if (!broken) {
					LOG.error(e);
					broken = true;
				}
			}
		}
		if (consoleView instanceof MyConsoleViewImpl && orChild) {
			ConsoleView parentConsoleView = ((MyConsoleViewImpl) consoleView).getParentConsoleView();
			return isSameConsole(dom, parentConsoleView, orChild);
		}
		return executionConsole == consoleView;
	}

	public static boolean isSameConsole(Content dom, ExecutionConsole consoleView) {
		JComponent actionsContextComponent = dom.getActionsContextComponent();

		Disposable disposer = dom.getDisposer();
		if (disposer instanceof RunContentDescriptor) {
			if (isSameConsole((RunContentDescriptor) disposer, consoleView, false)) {
				return true;
			}
		}
		if (actionsContextComponent == null) {
			return false;
		}
		if (actionsContextComponent == consoleView) {
			return true;
		} else if (actionsContextComponent instanceof SMTestRunnerResultsForm) {
			SMTestRunnerResultsForm resultsForm = (SMTestRunnerResultsForm) actionsContextComponent;
			try {
				Field myConsole = TestResultsPanel.class.getDeclaredField("myConsole");
				myConsole.setAccessible(true);
				ConsoleView data = DataManager.getInstance().getDataContext((Component) myConsole.get(resultsForm)).getData(LangDataKeys.CONSOLE_VIEW);
				return data == consoleView;
			} catch (Throwable e) {
				LOG.error(e);
			}
		} else if (actionsContextComponent instanceof DuplexConsoleView) {
			DuplexConsoleView duplexConsoleView = (DuplexConsoleView) actionsContextComponent;
			ExecutionConsole executionConsole;
			if (duplexConsoleView.isPrimaryConsoleEnabled()) {
				executionConsole = duplexConsoleView.getPrimaryConsoleView();
			} else {
				executionConsole = duplexConsoleView.getSecondaryConsoleView(); // no idea what that is
			}
			return executionConsole == consoleView;
		} else if (actionsContextComponent instanceof BuildView) {
			try {
				Object myExecutionConsole = ReflectionUtils.getPropertyValue(actionsContextComponent, "myExecutionConsole");
				if (myExecutionConsole instanceof BaseTestsOutputConsoleView) {
					myExecutionConsole = ((BaseTestsOutputConsoleView) myExecutionConsole).getConsole();
				}
				if (myExecutionConsole == consoleView) {
					return true;
				}
			} catch (Exception e) {
				if (!broken) {
					LOG.error(e);
					broken = true;
				}
			}
		}
		return false;
	}

	public static class LightProcessHandler extends ProcessHandler {
		@Override
		protected void destroyProcessImpl() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void detachProcessImpl() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean detachIsDefault() {
			return false;
		}

		@Override
		@Nullable
		public OutputStream getProcessInput() {
			return null;
		}
	}

	private static MyJPanel createConsolePanel(RunnerLayoutUi runnerLayoutUi, ConsoleView view, ActionGroup actions, GrepPanel comp, String consoleUUID) {
		MyJPanel panel = new MyJPanel(runnerLayoutUi, consoleUUID);
		panel.setLayout(new BorderLayout());
		panel.add(comp.getRootComponent(), BorderLayout.NORTH);
		panel.add(view.getComponent(), BorderLayout.CENTER);
		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("GrepConsole", actions, false);
		actionToolbar.setTargetComponent(view.getComponent());
		panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
		return panel;
	}

	private MyConsoleViewImpl createConsoleWithoutInputFilter(@NotNull Project project, ConsoleView parentConsoleView, @NotNull ProcessHandler processHandler) {
		MyConsoleViewImpl console = ServiceManager.getInstance().createConsoleWithoutInputFilter(project, parentConsoleView);
		console.attachToProcess(processHandler);
		return console;
	}

	private ConsoleView getConsoleView(AnActionEvent e) {
		return e.getData(LangDataKeys.CONSOLE_VIEW);
	}

	@Override
	public void update(AnActionEvent e) {
		Presentation presentation = e.getPresentation();
		boolean enabled = false;

		Project eventProject = getEventProject(e);
		ConsoleView parentConsoleView = getTopParentConsoleView(e.getData(LangDataKeys.CONSOLE_VIEW));
		if (parentConsoleView != null) {
			GrepFilter grepFilter = ServiceManager.getInstance().getGrepFilter(parentConsoleView);
			if (eventProject != null && grepFilter != null) {
				RunContentDescriptor runContentDescriptor = OpenGrepConsoleAction.getRunContentDescriptor(eventProject, parentConsoleView);
				if (runContentDescriptor != null) {
					RunnerLayoutUi runnerLayoutUi = getRunnerLayoutUi(eventProject, runContentDescriptor, parentConsoleView);
					enabled = runnerLayoutUi != null;
				}
			}
		}

		presentation.setEnabled(enabled || e.getData(PlatformDataKeys.TOOL_WINDOW) != null);
	}

	public static ConsoleView getTopParentConsoleView(ConsoleView data) {
		if (data instanceof MyConsoleViewImpl) {
			data = getTopParentConsoleView(((MyConsoleViewImpl) data).getParentConsoleView());
		}
		return data;
	}

	static class MyJPanel extends JPanel implements Disposable {
		private RunnerLayoutUi runnerLayoutUi;
		private final String consoleUUID;

		public MyJPanel(@Nullable RunnerLayoutUi runnerLayoutUi, String consoleUUID) {
			this.runnerLayoutUi = runnerLayoutUi;
			this.consoleUUID = consoleUUID;
		}

		public String getConsoleUUID() {
			return consoleUUID;
		}

		@Override
		public void dispose() {
			runnerLayoutUi = null;
			// TODO leak when closing tail by Close button
			// myPreferredFocusableComponent com.intellij.ui.tabs.TabInfo

			removeAll();
		}
	}

	public static class PinAction extends ToggleAction implements DumbAware {
		private boolean pinned;
		private final GrepPanel quickFilterPanel;
		private final String parentConsoleUUID;
		private String consoleUUID;
		private Project myProject;
		private PinnedGrepConsolesState.RunConfigurationRef runConfigurationRef;
		private final String contentType;

		public PinAction(Project myProject, GrepPanel quickFilterPanel, String parentConsoleUUID, String consoleUUID, Profile profile,
						 @NotNull PinnedGrepConsolesState.RunConfigurationRef runConfigurationRef, String contentType) {
			super("Pin", "Reopen on the next run (API allowed matching of the Run Configuration based only on the name&icon)", AllIcons.General.Pin_tab);
			this.quickFilterPanel = quickFilterPanel;
			this.parentConsoleUUID = parentConsoleUUID;
			this.consoleUUID = consoleUUID;
			this.myProject = myProject;
			this.runConfigurationRef = runConfigurationRef;
			this.contentType = contentType;
			PinnedGrepConsolesState projectComponent = PinnedGrepConsolesState.getInstance(this.myProject);
			projectComponent.register(this, profile);
			pinned = projectComponent.isPinned(this);
		}

		public String getContentType() {
			return contentType;
		}

		@Override
		public boolean isSelected(AnActionEvent anActionEvent) {
			return pinned;
		}

		public void refreshPinStatus(PinnedGrepConsolesState projectComponent) {
			pinned = projectComponent.isPinned(this);
		}

		@Override
		public void setSelected(@Nullable AnActionEvent anActionEvent, boolean b) {
			setSelected(b);
		}

		public void setSelected(boolean b) {
			pinned = b;
			PinnedGrepConsolesState projectComponent = PinnedGrepConsolesState.getInstance(myProject);
			if (pinned) {
				projectComponent.pin(this);
			} else {
				projectComponent.unpin(this);
			}
		}

		public GrepCompositeModel getModel() {
			return quickFilterPanel.getModel();
		}

		public boolean isPinned() {
			return pinned;
		}

		public String getParentConsoleUUID() {
			return parentConsoleUUID;
		}

		public String getConsoleUUID() {
			return consoleUUID;
		}

		public Project getMyProject() {
			return myProject;
		}

		@NotNull
		public PinnedGrepConsolesState.RunConfigurationRef getRunConfigurationRef() {
			return runConfigurationRef;
		}

		@Override
		public String toString() {
			return "PinAction{" + "pinned=" + pinned + ", parentConsoleUUID='" + parentConsoleUUID + '\'' + ", consoleUUID='" + consoleUUID + '\''
					+ ", runConfigurationRef=" + runConfigurationRef + '}';
		}
	}

	public static ToolWindow findToolWindow(ConsoleView consoleView, Project project) {
		ToolWindowManager instance = ToolWindowManager.getInstance(project);
		String activeToolWindowId = instance.getActiveToolWindowId();
		ToolWindow toolWindow = instance.getToolWindow(activeToolWindowId);
		if (toolWindow != null) {
			String[] toolWindowIds = instance.getToolWindowIds();
			for (String toolWindowId : toolWindowIds) {
				ToolWindow t = instance.getToolWindow(toolWindowId);
				if (t != null) {
					ContentManager contentManager = t.getContentManagerIfCreated();
					if (contentManager != null) {
						Content[] contents = contentManager.getContents();
						for (Content content : contents) {
							if (isSameConsole(content, consoleView)) {
								return t;
							}
						}
					}
				}
			}
			return null;
		} else {
			return toolWindow;
		}
	}

	private static class MyContentManagerListener implements ContentManagerListener {
		private final Content tab;
		private final GrepPanel quickFilterPanel;

		public MyContentManagerListener(Content tab, GrepPanel quickFilterPanel) {
			this.tab = tab;
			this.quickFilterPanel = quickFilterPanel;
		}

		@Override
		public void selectionChanged(@NotNull ContentManagerEvent event) {
			if (event.getContent() == tab && event.getOperation() == ContentManagerEvent.ContentOperation.add) {
				quickFilterPanel.tabSelected();
			}
		}
	}
}
