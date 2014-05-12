package krasa.grepconsole.plugin;

import java.io.File;

import javax.swing.*;

import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.Sound;

import org.jetbrains.annotations.*;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState>, ExportableApplicationComponent {

	private SettingsDialog form;
	private PluginState settings;

	private HighlightManipulationAction currentAction;
	private ServiceManager serviceManager = ServiceManager.getInstance();

	public GrepConsoleApplicationComponent() {
	}

	public void initComponent() {
	}

	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@NotNull
	public String getComponentName() {
		return "GrepConsole";
	}

	public static GrepConsoleApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(GrepConsoleApplicationComponent.class);
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

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	public JComponent createComponent() {
		// if (form == null) {
		form = new SettingsDialog(getState().clone());
		// }
		return form.getRootComponent();
	}

	public boolean isModified() {
		return form.isSettingsModified(settings);
	}

	public void apply() throws ConfigurationException {
		settings = form.getSettings().clone();
		serviceManager.resetSettings();
		Sound.soundMode = SoundMode.DISABLED;
		if (currentAction != null) {
			currentAction.applySettings();
		}
		Sound.soundMode = SoundMode.ENABLED;
	}

	public void reset() {
		if (form != null) {
			form.importFrom(settings.clone());
		}
	}

	public void disposeUIResources() {
		form = null;
	}

	@NotNull
	public PluginState getState() {
		if (settings == null) {
			settings = new PluginState();
			settings.setProfiles(DefaultState.createDefault());
		}
		return settings;
	}

	public void loadState(PluginState state) {
		this.settings = state;
	}

	public Profile getProfile(Project project) {
		return getState().getDefaultProfile();
	}

	public void setCurrentAction(HighlightManipulationAction currentEditor) {
		this.currentAction = currentEditor;
	}

	@NotNull
	@Override
	public File[] getExportFiles() {
		return new File[] { PathManager.getOptionsFile("grepConsole") };
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return "Grep Console";
	}
}
