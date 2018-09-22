package krasa.grepconsole.grep;

import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.*;
import com.intellij.execution.ui.layout.impl.RunnerLayoutUiImpl;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import krasa.grepconsole.filter.GrepCopyingFilter;
import krasa.grepconsole.grep.gui.GrepPanel;
import krasa.grepconsole.grep.listener.GrepCopyingFilterListener;
import krasa.grepconsole.grep.listener.GrepCopyingFilterSyncListener;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.PluginState;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.FocusUtils;
import krasa.grepconsole.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

//import krasa.grepconsole.grep.listener.GrepCopyingFilterAsyncListener;

public class OpenGrepConsoleAction extends DumbAwareAction {

	private static final Logger LOG = Logger.getInstance(OpenGrepConsoleAction.class);

	public OpenGrepConsoleAction() {
	}

	public OpenGrepConsoleAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		PluginState pluginState = GrepConsoleApplicationComponent.getInstance().getState();
		pluginState.getDonationNagger().actionExecuted();
		
		Project eventProject = getEventProject(e);
		ConsoleViewImpl parentConsoleView = (ConsoleViewImpl) getConsoleView(e);
		String expression = getExpression(e);
		try {
			PinnedGrepsReopener.enabled = false;
			createGrepConsole(eventProject, null, parentConsoleView, null, expression, UUID.randomUUID().toString());
		} finally {
			PinnedGrepsReopener.enabled = true;
		}

	}

	public ConsoleViewImpl createGrepConsole(Project project, PinnedGrepConsolesState.RunConfigurationRef key, ConsoleViewImpl parentConsoleView, @Nullable GrepModel grepModel, @Nullable String expression,
											 String consoleUUID) {
		if (grepModel != null) {
			expression = grepModel.getExpression();
		}

		final GrepCopyingFilter copyingFilter = ServiceManager.getInstance().getCopyingFilter(parentConsoleView);
		if (copyingFilter == null) {
			throw new IllegalStateException("Console not supported: " + parentConsoleView);
		}
		RunContentDescriptor runContentDescriptor = getRunContentDescriptor(project, parentConsoleView);
		RunnerLayoutUi runnerLayoutUi = getRunnerLayoutUi(project, runContentDescriptor, parentConsoleView);
		if (runnerLayoutUi == null) {  //should not happen
			throw new IllegalStateException("runnerLayoutUi == null");
		}
		if (key == null) {
			key = new PinnedGrepConsolesState.RunConfigurationRef(runContentDescriptor.getDisplayName(), runContentDescriptor.getIcon());
		}

		LightProcessHandler myProcessHandler = new LightProcessHandler();


		ConsoleViewImpl newConsole = (ConsoleViewImpl) createConsole(project, parentConsoleView, myProcessHandler);
		Profile profile = ServiceManager.getInstance().getProfile(parentConsoleView);
		ServiceManager.getInstance().profileChanged(newConsole, profile);

		final GrepCopyingFilterListener copyingListener = new GrepCopyingFilterSyncListener(myProcessHandler, project, profile);
		final GrepPanel quickFilterPanel = new GrepPanel(parentConsoleView, newConsole, copyingListener, grepModel, expression, runnerLayoutUi);


		DefaultActionGroup actions = new DefaultActionGroup();
		String parentConsoleUUID = getConsoleUUID(parentConsoleView);
		PinAction pinAction = new PinAction(project, quickFilterPanel, parentConsoleUUID, consoleUUID, profile, key);
		actions.add(pinAction);

		final MyJPanel consolePanel = createConsolePanel(runnerLayoutUi, newConsole, actions, quickFilterPanel, consoleUUID);
		for (AnAction action : newConsole.createConsoleActions()) {
			actions.add(action);
		}

		final Content tab = runnerLayoutUi.createContent(ExecutionConsole.CONSOLE_CONTENT_ID, consolePanel, title(expression),
				AllIcons.General.Filter, consolePanel);
		runnerLayoutUi.addContent(tab);
		try {
			runnerLayoutUi.selectAndFocus(tab, true, true);
		} catch (Exception e) {
			LOG.warn(e);
		}


		PinnedGrepConsolesState.RunConfigurationRef runConfigurationRef = pinAction.getRunConfigurationRef();
		quickFilterPanel.setApplyCallback(new Callback() {
			@Override
			public void apply(GrepModel grepModel) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("apply callback");
				}
				copyingListener.modelUpdated(grepModel);
				tab.setDisplayName(title(grepModel.getExpression()));
				PinnedGrepConsolesState.getInstance(project).update(runConfigurationRef, parentConsoleUUID, consoleUUID, grepModel, false);
			}
		});

		parentConsoleView.flushDeferredText();
		for (String s : parentConsoleView.getEditor().getDocument().getText().split("\n")) {
			copyingListener.process(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
		}
		copyingFilter.addListener(copyingListener);

		Disposer.register(runContentDescriptor, tab);
		Disposer.register(tab, consolePanel);
		AtomicBoolean exit = new AtomicBoolean();
		Disposer.register(runContentDescriptor, new Disposable() {
			@Override
			public void dispose() {
				exit.set(true);
			}
		});
		Disposer.register(tab, new Disposable() {
			@Override
			public void dispose() {
				if (!exit.get()) {
					pinAction.setSelected(false);
				}
			}
		});


		Disposer.register(consolePanel, newConsole);
		Disposer.register(consolePanel, copyingListener);
		Disposer.register(consolePanel, quickFilterPanel);
		Disposer.register(consolePanel, new Disposable() {
			@Override
			public void dispose() {
				copyingFilter.removeListener(copyingListener);
			}
		});


		Disposable inactiveTitleDisposer;
		Container parent = parentConsoleView.getParent();
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
	public String getConsoleUUID(ConsoleViewImpl parentConsoleView) {
		String parentConsoleUUID = null;
		Container parent = parentConsoleView.getParent();
		if (parent instanceof MyJPanel) {
			parentConsoleUUID = ((MyJPanel) parent).getConsoleUUID();
		}
		return parentConsoleUUID;
	}

	protected String title(String expression) {
		return StringUtils.substring(expression, 0, 20);
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

	@Deprecated
	@Nullable
	protected String getContentType(@NotNull RunnerLayoutUi runnerLayoutUi) {
		ContentManager contentManager = runnerLayoutUi.getContentManager();
		Content selectedContent = contentManager.getSelectedContent();  //not reliable, returns variables for debug sometimes or null
		return RunnerLayoutUiImpl.CONTENT_TYPE.get(selectedContent);
	}

	public interface Callback {

		void apply(GrepModel grepModel);
	}

	public static RunnerLayoutUi getRunnerLayoutUi(Project eventProject, RunContentDescriptor runContentDescriptor, ConsoleViewImpl parentConsoleView) {
		RunnerLayoutUi runnerLayoutUi = null;

		if (runContentDescriptor != null) {
			runnerLayoutUi = runContentDescriptor.getRunnerLayoutUi();
		}

		if (runnerLayoutUi == null) {
			XDebugSession debugSession = XDebuggerManager.getInstance(eventProject).getDebugSession(
					parentConsoleView);
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

		if (runnerLayoutUi == null) {
			Container parent = parentConsoleView.getParent();
			if (parent instanceof MyJPanel) {
				runnerLayoutUi = ((MyJPanel) parent).runnerLayoutUi;
			}
		}
		if (runnerLayoutUi == null) {    //probably useless
			RunContentManager contentManager = ExecutionManager.getInstance(eventProject).getContentManager();
			RunContentDescriptor selectedContent = contentManager.getSelectedContent();
			if (selectedContent != null) {
				runnerLayoutUi = selectedContent.getRunnerLayoutUi();
			}
		}
		return runnerLayoutUi;
	}

	public static RunContentDescriptor getRunContentDescriptor(Project project, ConsoleViewImpl consoleView) {
		Collection<RunContentDescriptor> descriptors = ExecutionHelper.findRunningConsole(project,
				dom -> {
					return FocusUtils.isSameConsole(dom, consoleView, true);
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
		RunContentManager contentManager = ExecutionManager.getInstance(project).getContentManager();
		RunContentDescriptor selectedContent = contentManager.getSelectedContent();
		if (selectedContent != null && LOG.isDebugEnabled()) {
			LOG.debug("#getRunContentDescriptor not found using ExecutionHelper.findRunningConsole, but found by getSelectedContent");

		}

		return selectedContent;
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

	private static MyJPanel createConsolePanel(RunnerLayoutUi runnerLayoutUi, ConsoleView view, ActionGroup actions,
											   GrepPanel comp, String consoleUUID) {
		MyJPanel panel = new MyJPanel(runnerLayoutUi, consoleUUID);
		panel.setLayout(new BorderLayout());
		panel.add(comp.getRootComponent(), BorderLayout.NORTH);
		panel.add(view.getComponent(), BorderLayout.CENTER);
		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions,
				false);
		panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
		return panel;
	}

	private ConsoleView createConsole(@NotNull Project project, ConsoleViewImpl parentConsoleView, @NotNull ProcessHandler processHandler) {
		ConsoleView console = ServiceManager.getInstance().createConsoleWithoutInputFilter(project, parentConsoleView);
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
		ConsoleViewImpl parentConsoleView = (ConsoleViewImpl) getConsoleView(e);
		if (parentConsoleView != null) {
			GrepCopyingFilter copyingFilter = ServiceManager.getInstance().getCopyingFilter(parentConsoleView);
			if (eventProject != null && copyingFilter != null) {
				RunContentDescriptor runContentDescriptor = OpenGrepConsoleAction.getRunContentDescriptor(eventProject, parentConsoleView);
				RunnerLayoutUi runnerLayoutUi = getRunnerLayoutUi(eventProject, runContentDescriptor, parentConsoleView);
				enabled = runnerLayoutUi != null;
			}
		}

		presentation.setEnabled(enabled);

	}

	static class MyJPanel extends JPanel implements Disposable {
		private RunnerLayoutUi runnerLayoutUi;
		private final String consoleUUID;

		public MyJPanel(RunnerLayoutUi runnerLayoutUi, String consoleUUID) {
			this.runnerLayoutUi = runnerLayoutUi;
			this.consoleUUID = consoleUUID;
		}

		public String getConsoleUUID() {
			return consoleUUID;
		}

		@Override
		public void dispose() {
			runnerLayoutUi = null;
			//TODO leak when closing tail by Close button
//			myPreferredFocusableComponent com.intellij.ui.tabs.TabInfo    

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

		public PinAction(Project myProject, GrepPanel quickFilterPanel, String parentConsoleUUID, String consoleUUID, Profile profile, @NotNull PinnedGrepConsolesState.RunConfigurationRef runConfigurationRef) {
			super("Pin", "Reopen on the next run (API allowed matching of the Run Configuration based only on the name&icon)", AllIcons.General.Pin_tab);
			this.quickFilterPanel = quickFilterPanel;
			this.parentConsoleUUID = parentConsoleUUID;
			this.consoleUUID = consoleUUID;
			this.myProject = myProject;
			this.runConfigurationRef = runConfigurationRef;
			PinnedGrepConsolesState projectComponent = PinnedGrepConsolesState.getInstance(this.myProject);
			projectComponent.register(this, profile);
			pinned = projectComponent.isPinned(this);
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

		public GrepModel getModel() {
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
			return "PinAction{" +
					"pinned=" + pinned +
					", parentConsoleUUID='" + parentConsoleUUID + '\'' +
					", consoleUUID='" + consoleUUID + '\'' +
					", runConfigurationRef=" + runConfigurationRef +
					'}';
		}
	}
}

