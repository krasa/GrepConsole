package krasa.grepconsole.plugin.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrepRunConfigurationExtensionNew extends RunConfigurationExtension {

	@Override
	public <T extends RunConfigurationBase<?>> void updateJavaParameters(@NotNull T runConfiguration, @NotNull JavaParameters javaParameters, RunnerSettings runnerSettings) throws ExecutionException {
		GrepConsoleData userData = GrepConsoleData.getGrepConsoleData(runConfiguration);
	}

	@Override
	protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws InvalidDataException {
		GrepConsoleData.getGrepConsoleData(runConfiguration).readExternal(element);
	}

	@Override
	public void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws WriteExternalException {
		if (!isApplicableFor(runConfiguration)) {
			return;
		}
		GrepConsoleData.getGrepConsoleData(runConfiguration).writeExternal(element);
	}

	@Nullable
	@Override
	protected String getEditorTitle() {
		return "Grep Console";
	}

	@Override
	public boolean isApplicableFor(@NotNull RunConfigurationBase runConfiguration) {
		return true;
	}

	@Nullable
	public SettingsEditor createEditor(@NotNull RunConfigurationBase configuration) {
		return new GrepSettingsEditor(configuration);
	}

	@NotNull
	@Override
	public String getSerializationId() {
		return "GrepConsole";
	}

}