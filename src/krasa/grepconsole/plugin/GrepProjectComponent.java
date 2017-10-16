package krasa.grepconsole.plugin;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import krasa.grepconsole.grep.PinnedGrepConsolesState;
import krasa.grepconsole.tail.TailContentExecutor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@State(name = "GrepConsole", storages = { @Storage(value = "GrepConsole.xml") })
public class GrepProjectComponent implements ProjectComponent, PersistentStateComponent<GrepProjectState> {
	private static final Logger LOG = Logger.getInstance(GrepProjectComponent.class);

	private Project project;
	private GrepProjectState grepProjectState = new GrepProjectState();
	private List<WeakReference<TailContentExecutor.PinAction>> tailPinActions = new ArrayList<>();
	private List<WeakReference<CloseAction>> tailCloseActions = new ArrayList<>();

	public static GrepProjectComponent getInstance(Project project) {
		return project.getComponent(GrepProjectComponent.class);
	}

	public GrepProjectComponent(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public void initComponent() {
		final MessageBusConnection conn = project.getMessageBus().connect();
		ServiceManager instance = ServiceManager.getInstance();

		conn.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {

			@Override
			public void processStarting(String executorId, @NotNull ExecutionEnvironment env) {
				instance.lastRunConfiguration = getRunConfigurationBase(env);
			}

			@Override
			public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
				instance.lastRunConfiguration = null;
			}

			@Override
			public void processTerminating(@NotNull RunProfile runProfile, @NotNull ProcessHandler processHandler) {

			}

			@Override
			public void processTerminated(@NotNull RunProfile runProfile, @NotNull ProcessHandler processHandler) {

			}

			@Override
			public void processStartScheduled(String s, ExecutionEnvironment executionEnvironment) {

			}


			@Override
			public void processNotStarted(String s, @NotNull ExecutionEnvironment executionEnvironment) {
				instance.lastRunConfiguration = null;
			}
		});
	}

	private static RunConfigurationBase getRunConfigurationBase(ExecutionEnvironment executionEnvironment) {
		if (executionEnvironment != null && executionEnvironment.getRunProfile() instanceof RunConfigurationBase) {
			return (RunConfigurationBase) executionEnvironment.getRunProfile();
		}
		return null;
	}
	  
	@Override
	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@Override
	@NotNull
	public String getComponentName() {
		return "GrepProjectComponent";
	}

	@Override
	public void projectOpened() {
		// called when project is opened
		try {
			if (grepProjectState != null) {
				grepProjectState.openOldPins(project);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	@Override
	public void projectClosed() {
	}

	@NotNull
	@Override
	public GrepProjectState getState() {
		return grepProjectState;
	}

	@Override
	public void loadState(GrepProjectState grepProjectState) {
		this.grepProjectState = grepProjectState;
	}

	public void register(TailContentExecutor.PinAction action) {
		tailPinActions.add(new WeakReference<>(action));
	}

	public void pin(@NotNull File pinnedFile) {
		grepProjectState.addPinned(pinnedFile);
		refresh();
	}

	public void unpin(File file) {
		getState().removePinned(file);
		refresh();
	}

	private void refresh() {
		for (Iterator<WeakReference<TailContentExecutor.PinAction>> iterator = tailPinActions.iterator(); iterator.hasNext(); ) {
			WeakReference<TailContentExecutor.PinAction> listener = iterator.next();
			TailContentExecutor.PinAction pinAction = listener.get();
			if (pinAction == null) {
				iterator.remove();
			} else {
				pinAction.refreshPinStatus(this);
			}
		}
	}

	public boolean isPinned(File file) {
		return getState().isPinned(file);
	}

	public void register(CloseAction action) {
		tailCloseActions.add(new WeakReference<>(action));
	}

	public void closeAllTails(AnActionEvent e) {
		for (WeakReference<CloseAction> tailCloseAction : tailCloseActions) {
			CloseAction closeAction = tailCloseAction.get();
			if (closeAction != null) {
				closeAction.actionPerformed(e);
			}
		}
		tailCloseActions.clear();
	}

	public PinnedGrepConsolesState getPinnedGreps() {
		return grepProjectState.getPinnedGrepConsolesState();
	}

}
