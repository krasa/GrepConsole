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

public class GrepRunConfigurationExtensionOld extends RunConfigurationExtension {

	@Override
	public <T extends RunConfigurationBase> void updateJavaParameters(T runConfiguration, JavaParameters javaParameters, RunnerSettings runnerSettings) throws ExecutionException {
	}

	@Override
	protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws InvalidDataException {
		element.removeAttribute(GrepConsoleData.SELECTED_PROFILE_ID);
	}

	@Override
	public void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws WriteExternalException {
		if (!isApplicableFor(runConfiguration)) {
			return;
		}
		element.removeAttribute(GrepConsoleData.SELECTED_PROFILE_ID);
	}

	@Nullable
	@Override
	protected String getEditorTitle() {
		return "Grep Console";
	}

	@Override
	protected boolean isApplicableFor(@NotNull RunConfigurationBase runConfiguration) {
		return true;
	}

	@Nullable
	public SettingsEditor createEditor(@NotNull RunConfigurationBase configuration) {
		return null;
	}

	@NotNull
	@Override
	public String getSerializationId() {
		return "krasa.grepconsole.plugin.runConfiguration.GrepRunConfigurationExtension";
	}
}