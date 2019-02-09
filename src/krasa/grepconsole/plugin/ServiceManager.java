package krasa.grepconsole.plugin;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.filters.CompositeInputFilter;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.AbstractFilter;
import krasa.grepconsole.filter.GrepCopyingFilter;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.filter.GrepInputFilter;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.filter.support.SoundMode;
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
	private List<WeakReference<GrepHighlightFilter>> highlightFilters = new ArrayList<>();
	private List<WeakReference<GrepInputFilter>> inputFilters = new ArrayList<>();

	/**
	 * to couple console with filters
	 */
	private WeakReference<GrepCopyingFilter> lastCopier;
	private WeakReference<GrepInputFilter> lastGrepInputFilter;


	Consoles consoles = new Consoles();
	private boolean createInputFilter = true;
	protected RunConfigurationBase lastRunConfiguration;

	public static ServiceManager getInstance() {
		return SERVICE_MANAGER;
	}

	@NotNull
	public Profile getProfile(ConsoleView consoleView) {
		if (consoleView == null) {
			return GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
		}
		return consoles.getProfile(consoleView);
	}


	static class Consoles {
		private WeakHashMap<ConsoleView, ConsoleViewData> consoleDataMap = new WeakHashMap<>();

		public void put(ConsoleView consoleView, GrepHighlightFilter grepHighlightFilter) {
			ConsoleViewData consoleViewData = getOrCreateData(consoleView);
			consoleViewData.grepHighlightFilter = grepHighlightFilter;
		}

		public void put(ConsoleView console, GrepCopyingFilter lastCopier) {
			getOrCreateData(console).grepCopyingFilter = lastCopier;
		}

		public void put(ConsoleView console, RunConfigurationBase lastRunConfiguration) {
			getOrCreateData(console).runConfigurationBase = lastRunConfiguration;
		}

		public void put(ConsoleView console, GrepInputFilter lastGrepInputFilter) {
			getOrCreateData(console).grepInputFilter = lastGrepInputFilter;
		}


		private ConsoleViewData getOrCreateData(ConsoleView consoleView) {
			ConsoleViewData consoleViewData = consoleDataMap.get(consoleView);
			if (consoleViewData == null) {
				consoleViewData = new ConsoleViewData();
				consoleDataMap.put(consoleView, consoleViewData);
			}
			return consoleViewData;
		}

		public GrepHighlightFilter getGrepHighlightFilter(ConsoleView console) {
			return getOrCreateData(console).grepHighlightFilter;
		}

		public GrepCopyingFilter getGrepCopyingFilter(ConsoleView console) {
			return getOrCreateData(console).grepCopyingFilter;
		}

		public Collection<GrepCopyingFilter> getCopiers() {
			return consoleDataMap.values().stream().map(value -> value.grepCopyingFilter).collect(Collectors.toCollection(ArrayList::new));
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
			PluginState state = GrepConsoleApplicationComponent.getInstance().getState();
			return state.getProfile(getSelectedProfileId(consoleView));
		}

		public long getSelectedProfileId(ConsoleView console) {
			ConsoleViewData consoleViewData = get(console);
			if (consoleViewData.runConfigurationBase != null) {
				GrepConsoleData grepConsoleData = GrepConsoleData.getGrepConsoleData(consoleViewData.runConfigurationBase);
				return grepConsoleData.getSelectedProfileId();
			} else {
				if (consoleViewData.profile != null) {
					return consoleViewData.profile.getId();
				}
				return 0;
			}
		}

		static class ConsoleViewData {
			RunConfigurationBase runConfigurationBase;
			/**
			 * for grep consoles - they don't have RunConfigurationBase
			 */
			Profile profile;

			GrepHighlightFilter grepHighlightFilter;
			GrepCopyingFilter grepCopyingFilter;
			GrepInputFilter grepInputFilter;

			public void setProfile(@NotNull Profile selectedProfile) {
				profile = selectedProfile;
				GrepCopyingFilter grepCopyingFilter = this.grepCopyingFilter;
				if (grepCopyingFilter != null) {
					grepCopyingFilter.setProfile(selectedProfile);
				}
				GrepHighlightFilter grepHighlightFilter = this.grepHighlightFilter;
				if (grepHighlightFilter != null) {
					grepHighlightFilter.setProfile(selectedProfile);
				}
				GrepInputFilter grepInputFilter = this.grepInputFilter;
				if (grepInputFilter != null) {
					grepInputFilter.setProfile(selectedProfile);
				}
			}
		}
	}


	public void resetSettings() {
		iterate(highlightFilters);
		iterate(inputFilters);
		iterate(consoles.getCopiers());
		// todo this may not work properly, regenerate GrepExpressionItem id
		Cache.reset();

	}

	private void iterate(Collection<GrepCopyingFilter> values) {
		for (GrepCopyingFilter filter : values) {
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
	public GrepInputFilter createInputFilter(@NotNull Project project, @NotNull Profile profile) {
		if (!createInputFilter) {
			return null;
		}
		GrepInputFilter lastInputFilter = new GrepInputFilter(project, profile);
		WeakReference<GrepInputFilter> weakReference = new WeakReference<>(lastInputFilter);
		inputFilters.add(weakReference);
		lastGrepInputFilter = weakReference;
		return lastInputFilter;
	}

	public GrepHighlightFilter createHighlightFilter(@NotNull Project project, @Nullable ConsoleView consoleView) {
		if (consoleView != null) {
			registerConsole(consoleView);
		}

		return createHighlightFilter2(project, consoleView);
	}

	@NotNull
	private GrepHighlightFilter createHighlightFilter2(@NotNull Project project, @Nullable ConsoleView consoleView) {
		Profile profile = getProfile(consoleView);
		GrepHighlightFilter grepHighlightFilter = new GrepHighlightFilter(project, profile);
		highlightFilters.add(new WeakReference<>(grepHighlightFilter));
		if (consoleView != null) {
			consoles.put(consoleView, grepHighlightFilter);
		}
		return grepHighlightFilter;
	}

	public GrepCopyingFilter createCopyingFilter(@NotNull Project project, Profile profile) {
		final GrepCopyingFilter grepInputFilter = new GrepCopyingFilter(project, profile);
		lastCopier = new WeakReference<>(grepInputFilter);
		return grepInputFilter;
	}

	@Nullable
	public GrepHighlightFilter getHighlightFilter(@NotNull ConsoleView console) {
		return consoles.getGrepHighlightFilter(console);
	}

	@Nullable
	public GrepCopyingFilter getCopyingFilter(@NotNull ConsoleView console) {
		return consoles.getGrepCopyingFilter(console);
	}

	public boolean isRegistered(@NotNull ConsoleView console) {
		return consoles.contains(console);
	}

	public void registerConsole(@NotNull ConsoleView console) {
		GrepCopyingFilter lastCopier = getLastCopier();
		lastCopier = checkConsistency(console, GrepCopyingFilter.class, lastCopier);
		if (lastCopier != null) {
			consoles.put(console, lastCopier);
			this.lastCopier = null;
		}
		GrepInputFilter lastGrepInputFilter = getLastGrepInputFilter();
		lastGrepInputFilter = checkConsistency(console, GrepInputFilter.class, lastGrepInputFilter);
		if (lastGrepInputFilter != null) {
			lastGrepInputFilter.init(new WeakReference<>(console), getProfile(console));
			consoles.put(console, lastGrepInputFilter);
			this.lastGrepInputFilter = null;
		}
		consoles.put(console, lastRunConfiguration);
	}


	@SuppressWarnings("unchecked")
	private <T> T checkConsistency(ConsoleView console, Class<T> clazz, InputFilter lastFilter) {
		if (console instanceof ConsoleViewImpl) {
			try {
				CompositeInputFilter myInputMessageFilter = (CompositeInputFilter) ReflectionUtils.getPropertyValue(console, "myInputMessageFilter");
				if (myInputMessageFilter != null) {
					List myFilters = (List) ReflectionUtils.getPropertyValue(myInputMessageFilter, "myFilters");
					if (myFilters != null) {
						for (Object myFilter : myFilters) {
							if (myFilter instanceof CompositeInputFilter) {        //old API has Pair<>
								Object actualFilter = ReflectionUtils.getPropertyValue(myFilter, "myOriginal");
								if (actualFilter != null && actualFilter.getClass().equals(clazz)) {
									if (actualFilter != lastFilter) {
//									LOG.error("Wrong filter " + lastFilter + " console="+console); //TODO
										return (T) actualFilter;
									}
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				LOG.error(e);
			}
		}

		return (T) lastFilter;
	}

	public void createHighlightFilterIfMissing(@NotNull ConsoleView console) {
		if (consoles.getGrepHighlightFilter(console) == null && console instanceof ConsoleViewImpl) {
			GrepHighlightFilter highlightFilter = createHighlightFilter2(((ConsoleViewImpl) console).getProject(), console);
			console.addMessageFilter(highlightFilter);
		}
	}

	@Nullable
	private GrepCopyingFilter getLastCopier() {
		if (lastCopier != null) {
			return lastCopier.get();
		} else {
			return null;
		}
	}


	@Nullable
	private GrepInputFilter getLastGrepInputFilter() {
		if (lastGrepInputFilter != null) {
			return lastGrepInputFilter.get();
		} else {
			return null;
		}
	}

	public ConsoleView createConsoleWithoutInputFilter(Project project, ConsoleViewImpl parentConsoleView) {
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
			throw new IllegalStateException("console not registered");
		}
	}

}
