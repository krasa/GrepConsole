package krasa.grepconsole.plugin;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.gui.CompositeSettingsDialog;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.plugin.runConfiguration.GrepConsoleData;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyConfigurable implements Configurable {

	private RunConfigurationBase runConfigurationBase;
	private long originalSelectedProfileId;
	private ConsoleView console;
	private CompositeSettingsDialog form;
	private ServiceManager serviceManager = ServiceManager.getInstance();
	public GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
	HighlightManipulationAction currentAction;

	public MyConfigurable() {
	}

	public MyConfigurable(ConsoleView console) {
		this.console = console;
		GrepConsoleData consoleSettings = ServiceManager.getInstance().getConsoleSettings(console);
		if (consoleSettings != null) {
			originalSelectedProfileId = consoleSettings.getSelectedProfileId();
		}
	}

	public MyConfigurable(RunConfigurationBase runConfigurationBase) {
		this.runConfigurationBase = runConfigurationBase;
		originalSelectedProfileId = GrepConsoleData.getGrepConsoleData(runConfigurationBase).getSelectedProfileId();
	}

	@Nls
	@Override
	public String getDisplayName() {
		return "Grep Console";
	}

	@Nullable
	public Icon getIcon() {
		return null;
	}

	@Override
	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	@Override
	public JComponent createComponent() {
		if (form == null) {
			form = new CompositeSettingsDialog(this, applicationComponent.getState(), originalSelectedProfileId);
		}
		return form.getRootComponent();
	}

	@Override
	public boolean isModified() {
		return form != null && form.isSettingsModified(applicationComponent.getState());
	}

	@Override
	public void apply() throws ConfigurationException {
		long selectedProfileId = form.getSelectedProfile().getId();
		RunConfigurationBase runConfigurationBase = this.runConfigurationBase;
		if (runConfigurationBase == null) {
			runConfigurationBase = ServiceManager.getInstance().getRunConfigurationBase(console);
		}
		if (runConfigurationBase != null) {
			GrepConsoleData.getGrepConsoleData(runConfigurationBase).setSelectedProfileId(selectedProfileId);
		}
		form.setSelectedProfileId(selectedProfileId);
		
		
		apply(currentAction);
	}

	public void apply(@Nullable HighlightManipulationAction currentAction) {
		PluginState formSettings = form.getSettings();
		applicationComponent.loadState(formSettings.clone());
		serviceManager.resetSettings();
		applicationComponent.initFoldingCache();
		Sound.soundMode = SoundMode.DISABLED;
		if (currentAction != null) {
			currentAction.applySettings();
		}
		Sound.soundMode = SoundMode.ENABLED;
	}

	@Override
	public void reset() {
		if (form != null) {
			form.importFrom(applicationComponent.getState());
		}
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}

	public void prepareForm(SettingsContext settingsContext) {
		form = new CompositeSettingsDialog(this, applicationComponent.getState(), settingsContext, originalSelectedProfileId);
	}

	public void setCurrentAction(HighlightManipulationAction currentEditor) {
		this.currentAction = currentEditor;
	}
}
