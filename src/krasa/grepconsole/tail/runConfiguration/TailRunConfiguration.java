package krasa.grepconsole.tail.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class TailRunConfiguration extends RunConfigurationBase<TailRunConfigurationSettings> {
	public TailRunConfigurationSettings mySettings = new TailRunConfigurationSettings();

	protected TailRunConfiguration(Project project, ConfigurationFactory factory, String name) {
		super(project, factory, name);
	}

	@Override
	public void readExternal(@NotNull Element element) throws InvalidDataException {
		super.readExternal(element);
		XmlSerializer.deserializeInto(mySettings, element);
	}

	@Override
	public void writeExternal(@NotNull Element element) {
		super.writeExternal(element);
		XmlSerializer.serializeInto(mySettings, element);
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
		return new TailSettingsEditor(getProject());
	}

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException {
		for (String path : mySettings.getPaths()) {
			if (!new File(path).exists()) {
				throw new RuntimeConfigurationException("File does not exist: " + path);
			}
			if (new File(path).isDirectory()) {
				throw new RuntimeConfigurationException("Directories not supported: " + path);
			}
		}
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
		return new TailRunProfileState(executionEnvironment);
	}


	@Override
	public RunConfiguration clone() {
		TailRunConfiguration clone = (TailRunConfiguration) super.clone();
		clone.mySettings = XmlSerializerUtil.createCopy(mySettings);
		return clone;
	}
}