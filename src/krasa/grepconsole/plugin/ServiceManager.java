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
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.MainInputFilter;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.plugin.runConfiguration.GrepConsoleData;
import krasa.grepconsole.utils.Rehighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
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

	/**
	 * to couple console with filters
	 */
	private WeakReference<GrepFilter> lastCopier;
	private WeakReference<MainInputFilter> lastGrepInputFilter;


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

		public void put(ConsoleView consoleView, HighlightingFilter highlightingFilter) {
			ConsoleViewData consoleViewData = getOrCreateData(consoleView);
			consoleViewData.highlightingFilter = highlightingFilter;
		}

		public void put(ConsoleView console, GrepFilter lastCopier) {
			getOrCreateData(console).grepFilter = lastCopier;
		}

		public void put(ConsoleView console, RunConfigurationBase lastRunConfiguration) {
			getOrCreateData(console).runConfigurationBase = lastRunConfiguration;
		}

		public void put(ConsoleView console, MainInputFilter lastMainInputFilter) {
			getOrCreateData(console).mainInputFilter = lastMainInputFilter;
		}


		private ConsoleViewData getOrCreateData(ConsoleView consoleView) {
			ConsoleViewData consoleViewData = consoleDataMap.get(consoleView);
			if (consoleViewData == null) {
				consoleViewData = new ConsoleViewData();
				consoleDataMap.put(consoleView, consoleViewData);
			}
			return consoleViewData;
		}

		public HighlightingFilter getHighlightFilter(ConsoleView console) {
			return getOrCreateData(console).highlightingFilter;
		}

		public GrepFilter getGrepFilter(ConsoleView console) {
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
			PluginState state = GrepConsoleApplicationComponent.getInstance().getState();
			return state.getProfile(getSelectedProfileId(consoleView));
		}

		public long getSelectedProfileId(ConsoleView console) {
			ConsoleViewData consoleViewData = get(console);
			if (consoleViewData == null) {
				return 0;
			} else if (consoleViewData.runConfigurationBase != null) {
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

			HighlightingFilter highlightingFilter;
			GrepFilter grepFilter;
			MainInputFilter mainInputFilter;

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


	public void resetSettings() {
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
	public MainInputFilter createInputFilter(@NotNull Project project, @NotNull Profile profile, GrepFilter grepFilter) {
		if (!createInputFilter) {
			return null;
		}
		Profile defaultProfile = GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
		MainInputFilter lastInputFilter = new MainInputFilter(project, profile, defaultProfile.isFilterOutBeforeGrep(), grepFilter);
		WeakReference<MainInputFilter> weakReference = new WeakReference<>(lastInputFilter);
		inputFilters.add(weakReference);
		lastGrepInputFilter = weakReference;
		return lastInputFilter;
	}

	public HighlightingFilter createHighlightFilter(@NotNull Project project, @Nullable ConsoleView consoleView) {
		if (consoleView != null) {
			registerConsole(consoleView);
		}

		return createHighlightFilter2(project, consoleView);
	}

	@NotNull
	private HighlightingFilter createHighlightFilter2(@NotNull Project project, @Nullable ConsoleView consoleView) {
		Profile profile = getProfile(consoleView);
		HighlightingFilter highlightingFilter = new HighlightingFilter(project, profile);
		highlightFilters.add(new WeakReference<>(highlightingFilter));
		if (consoleView != null) {
			consoles.put(consoleView, highlightingFilter);
		}
		return highlightingFilter;
	}

	public GrepFilter createGrepFilter(@NotNull Project project, Profile profile) {
		final GrepFilter grepInputFilter = new GrepFilter(project, profile);
		lastCopier = new WeakReference<>(grepInputFilter);
		return grepInputFilter;
	}

	@Nullable
	public HighlightingFilter getHighlightFilter(@NotNull ConsoleView console) {
		return consoles.getHighlightFilter(console);
	}

	@Nullable
	public GrepFilter getGrepFilter(@NotNull ConsoleView console) {
		return consoles.getGrepFilter(console);
	}

	public boolean isRegistered(@NotNull ConsoleView console) {
		return consoles.contains(console);
	}

	public void registerConsole(@NotNull ConsoleView console) {

		MainInputFilter lastMainInputFilter = getLastGrepInputFilter();
		lastMainInputFilter = checkConsistency(console, MainInputFilter.class, lastMainInputFilter);
		if (lastMainInputFilter != null) {
			lastMainInputFilter.init(new WeakReference<>(console), getProfile(console));
			consoles.put(console, lastMainInputFilter);
			this.lastGrepInputFilter = null;
		}


		GrepFilter lastCopier = getLastCopier();
		lastCopier = checkConsistency(console, GrepFilter.class, lastCopier);
		if (lastMainInputFilter != null && lastMainInputFilter.getGrepFilter() != null) {
			lastCopier = lastMainInputFilter.getGrepFilter();
		}
		if (lastCopier != null) {
			consoles.put(console, lastCopier);
			this.lastCopier = null;
		}

		consoles.put(console, lastRunConfiguration);
	}

	static boolean broken = false;

	@SuppressWarnings("unchecked")
	private <T> T checkConsistency(ConsoleView console, Class<T> clazz, InputFilter lastFilter) {
		if (console instanceof ConsoleViewImpl) {
			try {
				Field field = getInputFilterField();  //2018.1- is obfucated
				if (field == null) {
					return (T) lastFilter;
				}
				CompositeInputFilter myInputMessageFilter = (CompositeInputFilter) field.get(console);
				if (myInputMessageFilter != null) {
					List myFilters = (List) ReflectionUtils.getPropertyValue(myInputMessageFilter, "myFilters");
					if (myFilters != null) {
						for (Object myFilter : myFilters) {
							if (myFilter != null) {
								if (myFilter.getClass().getSimpleName().equals("InputFilterWrapper")) {        //for 2018+, old API won't work
									Object actualFilter = ReflectionUtils.getPropertyValue(myFilter, "myOriginal");
									if (actualFilter != null && actualFilter.getClass().equals(clazz)) {
										return (T) actualFilter;
									}
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				if (!broken) {
					broken = true;
					LOG.error("Please report this", e);
				}
			}
		}

		return (T) lastFilter;
	}

	private static Field inputFilterField;

	//field is obfuscated, must find it by type
	@Nullable
	private static Field getInputFilterField() {
		if (inputFilterField == null) {
			Field[] declaredFields = ConsoleViewImpl.class.getDeclaredFields();
			for (Field declaredField : declaredFields) {
				if (declaredField.getType().equals(InputFilter.class)) {
					inputFilterField = declaredField;
					inputFilterField.setAccessible(true);
				}
			}
		}
		return inputFilterField;
	}

	public void createHighlightFilterIfMissing(@NotNull ConsoleView console) {
		if (consoles.getHighlightFilter(console) == null && console instanceof ConsoleViewImpl) {
			HighlightingFilter highlightingFilter = createHighlightFilter2(((ConsoleViewImpl) console).getProject(), console);
			console.addMessageFilter(highlightingFilter);
		}
	}

	@Nullable
	private GrepFilter getLastCopier() {
		if (lastCopier != null) {
			return lastCopier.get();
		} else {
			return null;
		}
	}


	@Nullable
	private MainInputFilter getLastGrepInputFilter() {
		if (lastGrepInputFilter != null) {
			return lastGrepInputFilter.get();
		} else {
			return null;
		}
	}

	public ConsoleView createConsoleWithoutInputFilter(Project project, ConsoleView parentConsoleView) {
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
