package krasa.grepconsole.plugin;

import static com.intellij.openapi.components.ServiceManager.getService;

import krasa.grepconsole.tail.TailPin;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.ExecutionAdapter;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;

public class GrepProjectComponent implements ProjectComponent {
	private static final Logger LOG = Logger.getInstance(GrepProjectComponent.class);

	private Project project;

	public GrepProjectComponent(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public void initComponent() {
		final MessageBusConnection conn = project.getMessageBus().connect();

		conn.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionAdapter() {
			@Override
			public void processStarting(String executorId, @NotNull ExecutionEnvironment env) {
				ServiceManager.getInstance().setLastExecutionId(env.getExecutionId());
			}
		});
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
			getService(project, TailPin.class).openOldPins();
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	@Override
	public void projectClosed() {
	}
}
