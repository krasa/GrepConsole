package krasa.grepconsole.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import krasa.grepconsole.builder.GrepTextConsoleBuilderFactoryImpl;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.service.Cache;
import krasa.grepconsole.service.GrepConsoleService;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState> {

	public static final String KEY = TextConsoleBuilderFactory.class.getName();
	private SettingsDialog form;
	private PluginState settings = new PluginState();
	private List<GrepConsoleService> runningServices = new ArrayList<GrepConsoleService>();

	public GrepConsoleApplicationComponent() {
	}

	public void initComponent() {
		MutablePicoContainer container = (MutablePicoContainer) ApplicationManager.getApplication().getPicoContainer();
		container.unregisterComponent(KEY);
		container.registerComponentInstance(KEY, new GrepTextConsoleBuilderFactoryImpl());
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
		form = new SettingsDialog(settings.clone());
		// }
		return form.getRootComponent();
	}

	public boolean isModified() {
		return form.isModified(settings);
	}

	public void apply() throws ConfigurationException {
		Cache.reset();
		settings = form.getSettings().clone();
		for (GrepConsoleService listener : runningServices) {
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
		return settings;
	}

	public void loadState(PluginState state) {
		this.settings = state;
	}

	public void register(GrepConsoleService grepConsoleService) {
		runningServices.add(grepConsoleService);
	}

	public void remove(GrepConsoleService grepConsoleService) {
		runningServices.remove(grepConsoleService);
	}

	public void changeProfile(ConsoleView console, Profile profile) {
		for (GrepConsoleService runningService : runningServices) {
			if (runningService.getConsoleView() == console) {
				runningService.setProfile(profile);
			}
		}
	}
}
