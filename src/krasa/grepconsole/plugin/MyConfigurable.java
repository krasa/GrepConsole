package krasa.grepconsole.plugin;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.gui.CompositeSettingsDialog;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.plugin.runConfiguration.GrepConsoleData;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyConfigurable implements Configurable {

	private RunConfigurationBase runConfigurationBase;
	private long originalSelectedProfileId;
	@Nullable
	private ConsoleView console;
	private CompositeSettingsDialog form;
	private ServiceManager serviceManager = ServiceManager.getInstance();
	public GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
	HighlightManipulationAction currentAction;
	private Project project;

	public MyConfigurable() {
		setDefaultProfileId();
	}

	public MyConfigurable(Project project) {
		setDefaultProfileId();
		this.project = project;
	}

	public MyConfigurable(Project project, @NotNull ConsoleView console) {
		this.console = console;
		originalSelectedProfileId = ServiceManager.getInstance().consoles.getSelectedProfileId(console);
		setDefaultProfileId();
		this.project = project;
	}

	public MyConfigurable(RunConfigurationBase runConfigurationBase) {
		this.runConfigurationBase = runConfigurationBase;
		originalSelectedProfileId = GrepConsoleData.getGrepConsoleData(runConfigurationBase).getSelectedProfileId();
		setDefaultProfileId();
		project = runConfigurationBase.getProject();
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

	protected void setDefaultProfileId() {
		if (originalSelectedProfileId == 0) {
			originalSelectedProfileId = applicationComponent.getState().getDefaultProfile().getId();
		}
	}

	@Override
	public boolean isModified() {
		return form != null && form.isSettingsModified(applicationComponent.getState());
	}

	@Override
	public void apply() throws ConfigurationException {
		apply(currentAction);
	}

	/**
	 * Run Configuration settings calls it all the time on change of models
	 */
	public void apply(@Nullable HighlightManipulationAction currentAction) {
		Profile selectedProfile = form.getSelectedProfile();
		if (selectedProfile != null) {
			PluginState formSettings = form.getSettings();
			applicationComponent.loadState(getClone(formSettings));


			long selectedProfileId = selectedProfile.getId();
			RunConfigurationBase runConfigurationBase = this.runConfigurationBase;
			if (runConfigurationBase == null && console != null) {
				runConfigurationBase = ServiceManager.getInstance().getRunConfigurationBase(console);
			}
			if (runConfigurationBase != null) {
				GrepConsoleData.getGrepConsoleData(runConfigurationBase).setSelectedProfileId(selectedProfileId);
			}
			if (selectedProfileId != originalSelectedProfileId) {
				Profile profile = applicationComponent.getState().getProfile(selectedProfileId);
				if (console != null) {
					serviceManager.profileChanged(console, profile);
				}
			}
			form.setOriginallySelectedProfileId(selectedProfileId);

			refreshServices(currentAction);
		}
	}

	protected PluginState getClone(PluginState formSettings) {
		return formSettings.clone();
	}

	protected void refreshServices(@Nullable HighlightManipulationAction currentAction) {
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

	public Project getProject() {
		return project;
	}
}
