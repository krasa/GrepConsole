package krasa.grepconsole.plugin;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import krasa.grepconsole.Cache;
import krasa.grepconsole.GrepFilterService;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState> {

	private SettingsDialog form;
	private PluginState settings;
	private Map<Project, GrepFilterService> cache = new HashMap<Project, GrepFilterService>();

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
		Cache.reset();
		settings = form.getSettings().clone();
		for (GrepFilterService listener : cache.values()) {
			listener.onChange();
		}
	}

	public void reset() {
		if (form != null) {
			form.importFrom(settings.clone());
		}
	}

	public void disposeUIResources() {
		form = null;
	}

	public PluginState getState() {
		if (settings == null) {
			settings = new PluginState();
			settings.setProfiles(PluginState.createDefault());
		}
		return settings;
	}

	public void loadState(PluginState state) {
		this.settings = state;
	}

	public void changeProfile(Project project, Profile profile) {
		cache.get(project).setProfile(profile);
	}

	public void projectClosed(Project project) {
		cache.remove(project);
	}

	public GrepFilterService getGrepFilter(Project project) {
		GrepFilterService grepFilter = cache.get(project);
		if (grepFilter == null) {
			grepFilter = new GrepFilterService(project);
		}
		cache.put(project, grepFilter);
		// warm up
		grepFilter.applyFilter("foo", 3);
		return grepFilter;
	}

	public Profile getProfile(Project project) {
		return getState().getDefaultProfile();
	}
}
