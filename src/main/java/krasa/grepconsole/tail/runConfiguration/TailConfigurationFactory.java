package krasa.grepconsole.tail.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TailConfigurationFactory extends ConfigurationFactory {

	protected TailConfigurationFactory(ConfigurationType type) {
		super(type);
	}

	@Override
	public RunConfiguration createTemplateConfiguration(Project project) {
		return new TailRunConfiguration(project, this, "Tail");
	}

	@Override
	public @NotNull
	String getId() {
		return TailRunConfigurationType.ID;
	}
}