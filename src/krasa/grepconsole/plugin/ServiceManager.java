package krasa.grepconsole.plugin;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.containers.WeakList;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.AbstractFilter;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.MainInputFilter;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.grep.gui.GrepPanel;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.plugin.runConfiguration.GrepConsoleData;
import krasa.grepconsole.utils.Rehighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Vojtech Krasa
 */
public class ServiceManager {
	private static final Logger LOG = Logger.getInstance(ServiceManager.class);

	private static final ServiceManager SERVICE_MANAGER = new ServiceManager();

	/**
	 * for tracking settings change
	 */
	private List<WeakReference<HighlightingFilter>> highlightFilters = new ArrayList<>();
	private List<WeakReference<MainInputFilter>> inputFilters = new ArrayList<>();

	private Consoles consoles = new Consoles();
	private boolean createInputFilter = true;
	protected RunConfigurationBase lastRunConfiguration;

	public static ServiceManager getInstance() {
		return SERVICE_MANAGER;
	}

	@NotNull
	public Profile getProfile(@Nullable ConsoleView consoleView) {
		if (consoleView == null) {
			return GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
		}
		return consoles.getProfile(consoleView);
	}

	public void projectClosed(Project project) {
		consoles.projectClosed(project);
	}

	public long getSelectedProfileId(ConsoleView console) {
		return consoles.getSelectedProfileId(console);
	}

	public void registerChildGrepConsole(ConsoleView parentConsoleView, MyConsoleViewImpl newConsole) {
		consoles.getOrCreateData(parentConsoleView).childs.add(newConsole);
	}

	public void unregisterGrepPanel(GrepPanel grepPanel) {
		MyConsoleViewImpl console = grepPanel.getConsole();
		ConsoleView originalConsole = console.getParentConsoleView();
		if (originalConsole != null) {
			consoles.getOrCreateData(originalConsole).childs.remove(console);
		}
	}

	public List<MyConsoleViewImpl> findChildGreps(ConsoleView parentConsoleView) {
		Consoles.ConsoleViewData consoleViewData = consoles.get(parentConsoleView);
		if (consoleViewData != null) {
			List<MyConsoleViewImpl> myConsoleViews = consoleViewData.childs.toStrongList();
			return myConsoleViews;
		}
		return Collections.emptyList();
	}

	static class Consoles {
		private WeakHashMap<ConsoleView, ConsoleViewData> consoleDataMap = new WeakHashMap<>();

		public void put(@NotNull ConsoleView consoleView, HighlightingFilter highlightingFilter) {
			ConsoleViewData consoleViewData = getOrCreateData(consoleView);
			consoleViewData.highlightingFilter = highlightingFilter;
		}

		public void put(@NotNull ConsoleView console, GrepFilter lastCopier) {
			getOrCreateData(console).grepFilter = lastCopier;
		}

		public void put(@NotNull ConsoleView console, RunConfigurationBase lastRunConfiguration) {
			LOG.debug("runConfigurationBase=", lastRunConfiguration);
			getOrCreateData(console).runConfigurationBase = lastRunConfiguration;
			if (LOG.isDebugEnabled()) {
				LOG.debug("getSelectedProfileId=", getSelectedProfileId(console));
			}
		}

		public void put(@NotNull ConsoleView console, MainInputFilter lastMainInputFilter) {
			getOrCreateData(console).mainInputFilter = lastMainInputFilter;
		}


		private ConsoleViewData getOrCreateData(@NotNull ConsoleView consoleView) {
			ConsoleViewData consoleViewData = consoleDataMap.get(consoleView);
			if (consoleViewData == null) {
				consoleViewData = new ConsoleViewData();
				Disposer.register(consoleView, () -> consoleDataMap.remove(consoleView));
				consoleDataMap.put(consoleView, consoleViewData);
			}
			return consoleViewData;
		}

		public HighlightingFilter getHighlightFilter(@NotNull ConsoleView console) {
			return getOrCreateData(console).highlightingFilter;
		}

		public GrepFilter getGrepFilter(@NotNull ConsoleView console) {
			return getOrCreateData(console).grepFilter;
		}

		public Collection<GrepFilter> getCopiers() {
			return consoleDataMap.values().stream().map(value -> value.grepFilter).collect(Collectors.toCollection(ArrayList::new));
		}

		public RunConfigurationBase getRunConfigurationBase(@NotNull ConsoleView console) {
			return getOrCreateData(console).runConfigurationBase;
		}

		public boolean contains(ConsoleView console) {
			return consoleDataMap.containsKey(console);
		}

		public Set<Map.Entry<ConsoleView, ConsoleViewData>> entrySet() {
			return consoleDataMap.entrySet();
		}

		public ConsoleViewData get(ConsoleView console) {
			return consoleDataMap.get(console);
		}

		@NotNull
		public Profile getProfile(ConsoleView consoleView) {
			PluginState state = PluginState.getInstance();
			return state.getProfile(getSelectedProfileId(consoleView));
		}

		public long getSelectedProfileId(ConsoleView console) {
			ConsoleViewData consoleViewData = get(console);
			if (consoleViewData == null) {
				return 0;
			} else if (consoleViewData.runConfigurationBase != null) {
				if (PluginState.getInstance().isAllowRunConfigurationChanges()) {
					GrepConsoleData grepConsoleData = GrepConsoleData.getGrepConsoleData(consoleViewData.runConfigurationBase);
					return grepConsoleData.getSelectedProfileId();
				}
			} else {
				if (consoleViewData.profile != null) {
					return consoleViewData.profile.getId();
				}
			}
			return 0;
		}

		public void projectClosed(Project project) {
			consoleDataMap.entrySet()
					.stream()
					.filter(entry -> {
						ConsoleViewData value = entry.getValue();
						return (value.highlightingFilter != null && value.highlightingFilter.getProject() == project)
								|| (value.mainInputFilter != null && value.mainInputFilter.getProject() == project)
								|| (value.grepFilter != null && value.grepFilter.getProject() == project);
					})
					.collect(Collectors.toList())
					.forEach(entry -> consoleDataMap.remove(entry.getKey()));
		}

		static class ConsoleViewData {
			public WeakList<MyConsoleViewImpl> childs = new WeakList<>();
			RunConfigurationBase runConfigurationBase;
			/**
			 * for grep consoles - they don't have RunConfigurationBase
			 */
			Profile profile;

			HighlightingFilter highlightingFilter;
			GrepFilter grepFilter;
			MainInputFilter mainInputFilter;

			public ConsoleViewData() {
			}

			public void setProfile(@NotNull Profile selectedProfile) {
				profile = selectedProfile;
				GrepFilter grepFilter = this.grepFilter;
				if (grepFilter != null) {
					grepFilter.setProfile(selectedProfile);
				}
				HighlightingFilter highlightingFilter = this.highlightingFilter;
				if (highlightingFilter != null) {
					highlightingFilter.setProfile(selectedProfile);
				}
				MainInputFilter mainInputFilter = this.mainInputFilter;
				if (mainInputFilter != null) {
					mainInputFilter.setProfile(selectedProfile);
				}
			}
		}
	}


	public synchronized void resetSettings() {
		iterate(highlightFilters);
		iterate(inputFilters);
		iterate(consoles.getCopiers());
		// todo this may not work properly, regenerate GrepExpressionItem id
		Cache.reset();

	}

	private void iterate(Collection<GrepFilter> values) {
		for (GrepFilter filter : values) {
			if (filter != null) {
				filter.onChange();
			}
		}
	}

	private <T extends AbstractFilter> void iterate(final List<WeakReference<T>> filters) {
		Iterator<WeakReference<T>> iterator = filters.iterator();
		while (iterator.hasNext()) {
			WeakReference<T> next = iterator.next();
			if (next == null) {    //this should never happen, but still someone got NPE https://github.com/krasa/GrepConsole/issues/100
				continue;
			}
			T filter = next.get();
			if (filter == null) {
				iterator.remove();
			} else {
				filter.onChange();
			}
		}
	}

	@Nullable
	public synchronized MainInputFilter createInputFilter(@NotNull Project project, @NotNull Profile profile, GrepFilter grepFilter, ConsoleView consoleView) {
		if (!createInputFilter) {
			return null;
		}
		Profile defaultProfile = GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
		MainInputFilter inputFilter = new MainInputFilter(project, profile, defaultProfile.isFilterOutBeforeGrep(), grepFilter);
		inputFilter.init(new WeakReference<>(consoleView), profile);
		inputFilters.add(new WeakReference<>(inputFilter));
		consoles.put(consoleView, inputFilter);
		return inputFilter;
	}

	public synchronized HighlightingFilter createOrGetHighlightFilter(@NotNull Project project, @NotNull ConsoleView consoleView) {
		HighlightingFilter highlightingFilter = consoles.getHighlightFilter(consoleView);
		if (highlightingFilter == null) {
			Profile profile = getProfile(consoleView);
			highlightingFilter = new HighlightingFilter(project, profile);
			highlightFilters.add(new WeakReference<>(highlightingFilter));
			consoles.put(consoleView, highlightingFilter);
		}
		return highlightingFilter;
	}

	public GrepFilter createGrepFilter(@NotNull Project project, Profile profile, ConsoleView consoleView) {
		GrepFilter grepFilter = new GrepFilter(project, profile);
		consoles.put(consoleView, grepFilter);
		return grepFilter;
	}

	@Nullable
	public HighlightingFilter getHighlightFilter(@NotNull ConsoleView console) {
		return consoles.getHighlightFilter(console);
	}

	@Nullable
	public GrepFilter getGrepFilter(@NotNull ConsoleView console) {
		GrepFilter grepFilter = consoles.getGrepFilter(console);
		if (grepFilter == null) {
			grepFilter = consoles.getGrepFilter(console);
		}
		return grepFilter;
	}

	public boolean isRegistered(@NotNull ConsoleView console) {
		return consoles.contains(console);
	}

	public synchronized void registerConsole(@NotNull ConsoleView console) {
		consoles.put(console, lastRunConfiguration);
	}

	/**
	 * workaround for consoles that does not use ConsoleDependentFilterProvider
	 */
	public void createHighlightFilterIfMissing(@NotNull ConsoleView console) {
		if (consoles.getHighlightFilter(console) == null && console instanceof ConsoleViewImpl) {
			HighlightingFilter highlightingFilter = createOrGetHighlightFilter(((ConsoleViewImpl) console).getProject(), console);
			console.addMessageFilter(highlightingFilter);
		}
	}

	public MyConsoleViewImpl createConsoleWithoutInputFilter(Project project, ConsoleView parentConsoleView) {
		try {
			createInputFilter = false;
			return new MyConsoleViewImpl(project, false, parentConsoleView);
		} finally {
			createInputFilter = true;
		}
	}

	public void rehighlight() {
		Rehighlighter rehighlighter = new Rehighlighter();

		Sound.soundMode = SoundMode.DISABLED;
		for (Map.Entry<ConsoleView, Consoles.ConsoleViewData> consoleView : consoles.entrySet()) {
			rehighlighter.resetHighlights(consoleView.getKey());
		}
		Sound.soundMode = SoundMode.ENABLED;

	}

	public RunConfigurationBase getRunConfigurationBase(@NotNull ConsoleView console) {
		return consoles.getRunConfigurationBase(console);
	}

	public void profileChanged(@NotNull ConsoleView console, @NotNull Profile selectedProfile) {
		Consoles.ConsoleViewData consoleViewData = consoles.get(console);
		if (consoleViewData != null) {
			consoleViewData.setProfile(selectedProfile);
		} else {
//			throw new IllegalStateException("console not registered");//TODO
		}
	}

}
