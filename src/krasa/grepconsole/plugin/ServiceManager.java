package krasa.grepconsole.plugin;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.*;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.utils.Rehighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Vojtech Krasa
 */
public class ServiceManager {
	private static final Logger log = Logger.getInstance(ServiceManager.class.getName());

	private static final ServiceManager SERVICE_MANAGER = new ServiceManager();

	/** for tracking settings change */
	private List<WeakReference<GrepHighlightFilter>> highlightFilters = new ArrayList<>();
	private List<WeakReference<GrepInputFilter>> inputFilters = new ArrayList<>();

	/** to couple console with filters */
	private WeakReference<GrepCopyingFilter> lastCopier;
	private WeakReference<GrepHighlightingInputFilter> lastGrepHighlightFilter;

	/** for providing attached filters for certain console */
	private WeakHashMap<ConsoleView, GrepHighlightFilter> weakHighlightersMap = new WeakHashMap<>();
	private WeakHashMap<ConsoleView, GrepCopyingFilter> weakCopiersMap = new WeakHashMap<>();
	private boolean createInputFilter = true;

	public static ServiceManager getInstance() {
		return SERVICE_MANAGER;
	}

	public void resetSettings() {
		iterate(highlightFilters);
		iterate(inputFilters);
		iterate(weakCopiersMap.values());
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
			T filter = next.get();
			if (filter == null) {
				iterator.remove();
			} else {
				filter.onChange();
			}
		}
	}

	@Nullable
	public GrepInputFilter createInputFilter(Project project) {
		if (!createInputFilter) {
			return null;
		}
		GrepInputFilter lastInputFilter = new GrepInputFilter(project);
		inputFilters.add(new WeakReference<>(lastInputFilter));
		return lastInputFilter;
	}


	public GrepHighlightingInputFilter createHighlightInputFilter(Project project) {
		GrepHighlightingInputFilter grepHighlightFilter = new GrepHighlightingInputFilter(project);
		highlightFilters.add(new WeakReference<>(grepHighlightFilter));
		lastGrepHighlightFilter = new WeakReference<>(grepHighlightFilter);
		return grepHighlightFilter;
	}

	public GrepHighlightFilter createHighlightFilter(@NotNull Project project, @Nullable ConsoleView consoleView) {
		final GrepHighlightFilter grepHighlightFilter = new GrepHighlightFilter(project);
		highlightFilters.add(new WeakReference<>(grepHighlightFilter));
		if (consoleView != null) {
			weakHighlightersMap.put(consoleView, grepHighlightFilter);
		}
		return grepHighlightFilter;
	}

	public GrepCopyingFilter createCopyingFilter(@NotNull Project project) {
		final GrepCopyingFilter grepInputFilter = new GrepCopyingFilter(project);
		lastCopier = new WeakReference<>(grepInputFilter);
		return grepInputFilter;
	}

	@Nullable
	public GrepHighlightFilter getHighlightFilter(@NotNull ConsoleView console) {
		GrepHighlightFilter grepHighlightFilter = weakHighlightersMap.get(console);
		if (grepHighlightFilter == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Something is wrong. " + "GrepHighlightFilter" + " not found for ").append(
					System.identityHashCode(console)).append("-").append(console);
			sb.append(". Registered: [");
			boolean i = false;
			for (Map.Entry<ConsoleView, GrepHighlightFilter> consoleViewGrepHighlightFilterEntry : weakHighlightersMap.entrySet()) {
				if (i) {
					sb.append(",");
				}
				sb.append(System.identityHashCode(console)).append("-").append(
						consoleViewGrepHighlightFilterEntry.getKey());
				i = true;
			}
			sb.append("]");
			log.warn(sb.toString());
			return null;
		}
		return grepHighlightFilter;
	}

	@Nullable
	public GrepCopyingFilter getCopyingFilter(@NotNull ConsoleView console) {
		return weakCopiersMap.get(console);
	}

	public boolean isRegistered(@NotNull ConsoleView console) {
		return weakHighlightersMap.containsKey(console);
	}

	public void registerConsole(ConsoleView console) {
		GrepCopyingFilter lastCopier = getLastCopier();
		if (lastCopier != null) {
			weakCopiersMap.put(console, lastCopier);
			this.lastCopier = null;
		}
		GrepHighlightingInputFilter lastGrepHighlightFilter = getLastGrepHighlightFilter();
		if (lastGrepHighlightFilter != null) {
			weakHighlightersMap.put(console, lastGrepHighlightFilter);
			this.lastGrepHighlightFilter = null;
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
	private GrepHighlightingInputFilter getLastGrepHighlightFilter() {
		if (lastGrepHighlightFilter != null) {
			return lastGrepHighlightFilter.get();
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
		for (ConsoleView consoleView : weakHighlightersMap.keySet()) {
			rehighlighter.resetHighlights(consoleView);
		}
		Sound.soundMode = SoundMode.ENABLED;

	}
}
