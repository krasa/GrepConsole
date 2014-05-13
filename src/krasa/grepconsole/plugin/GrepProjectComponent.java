package krasa.grepconsole.plugin;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.ExecutionAdapter;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;

public class GrepProjectComponent implements ProjectComponent {
	private Project project;

	public GrepProjectComponent(Project project) {
		this.project = project;
	}

	public void initComponent() {
		final MessageBusConnection conn = project.getMessageBus().connect();

		conn.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionAdapter() {
			@Override
			public void processStarting(String executorId, @NotNull ExecutionEnvironment env) {
				ServiceManager.getInstance().setLastExecutionId(env.getExecutionId());
			}
		});
	}

	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@NotNull
	public String getComponentName() {
		return "GrepProjectComponent";
	}

	public void projectOpened() {
		// called when project is opened
	}

	public void projectClosed() {
	}
}
