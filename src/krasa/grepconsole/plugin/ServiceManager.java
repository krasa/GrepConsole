package krasa.grepconsole.plugin;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.*;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.grep.GrepCopyingFilter;
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
	private List<WeakReference<GrepHighlightFilter>> highlightFilters = new ArrayList<WeakReference<GrepHighlightFilter>>();
	private List<WeakReference<GrepInputFilter>> inputFilters = new ArrayList<WeakReference<GrepInputFilter>>();
	private List<WeakReference<AnsiInputFilter>> ansiFilters = new ArrayList<WeakReference<AnsiInputFilter>>();

	/** to couple console with filters */
	private WeakReference<AnsiInputFilter> lastAnsi;
	private WeakReference<GrepCopyingFilter> lastCopier;
	private WeakReference<GrepHighlightingInputFilter> lastGrepHighlightFilter;
	private WeakReference<GrepHighlightFilter> lastQuickFilter;
	private long lastExecutionId;

	/** for providing attached filters for certain console */
	private WeakHashMap<ConsoleView, GrepHighlightFilter> weakHighlightersMap = new WeakHashMap<ConsoleView, GrepHighlightFilter>();
	private WeakHashMap<ConsoleView, GrepCopyingFilter> weakCopiersMap = new WeakHashMap<ConsoleView, GrepCopyingFilter>();

	public static ServiceManager getInstance() {
		return SERVICE_MANAGER;
	}

	public void resetSettings() {
		iterate(highlightFilters);
		iterate(inputFilters);
		iterate(ansiFilters);
		// todo this may not work properly, regenerate GrepExpressionItem id
		Cache.reset();

	}

	private <T extends AbstractFilter> void iterate(final List<WeakReference<T>> cacheAnsi1) {
		Iterator<WeakReference<T>> iterator = cacheAnsi1.iterator();
		while (iterator.hasNext()) {
			WeakReference<T> next = iterator.next();
			T ansiInputFilter = next.get();
			if (ansiInputFilter == null) {
				iterator.remove();
			} else {
				ansiInputFilter.onChange();
			}
		}
	}

	public GrepInputFilter createInputFilter(Project project) {
		final GrepInputFilter grepInputFilter = new GrepInputFilter(project);
		inputFilters.add(new WeakReference<GrepInputFilter>(grepInputFilter));
		return grepInputFilter;
	}

	public AnsiInputFilter createAnsiFilter(Project project) {
		AnsiInputFilter service = new AnsiInputFilter(project);
		ansiFilters.add(new WeakReference<AnsiInputFilter>(service));
		lastAnsi = new WeakReference<AnsiInputFilter>(service);
		return service;
	}
`

	public GrepHighlightingInputFilter createHighlightInputFilter(Project project) {
		GrepHighlightingInputFilter grepHighlightFilter = new GrepHighlightingInputFilter(project);
		grepHighlightFilter.setExecutionId(getLastExecutionId());
		highlightFilters.add(new WeakReference<GrepHighlightFilter>(grepHighlightFilter));
		lastGrepHighlightFilter = new WeakReference<GrepHighlightingInputFilter>(grepHighlightFilter);
		return grepHighlightFilter;
	}
		
	public GrepHighlightFilter createHighlightFilter(@NotNull Project project, @Nullable ConsoleView consoleView) {
		final GrepHighlightFilter grepHighlightFilter = new GrepHighlightFilter(project);
		grepHighlightFilter.setExecutionId(getLastExecutionId());
		highlightFilters.add(new WeakReference<GrepHighlightFilter>(grepHighlightFilter));
		if (consoleView != null) {
			weakHighlightersMap.put(consoleView, grepHighlightFilter);
		}
		return grepHighlightFilter;
	}

	public GrepCopyingFilter createCopyingFilter(@NotNull Project project) {
		final GrepCopyingFilter grepInputFilter = new GrepCopyingFilter(project);
		lastCopier = new WeakReference<GrepCopyingFilter>(grepInputFilter);
		return grepInputFilter;
	}

	@Nullable
	public GrepHighlightFilter getHighlightFilter(@NotNull ConsoleView console) {
		return getGrepHighlightFilter(console, weakHighlightersMap, "GrepHighlightFilter");
	}

	@Nullable
	public GrepCopyingFilter getCopyingFilter(@NotNull ConsoleView console) {
		return weakCopiersMap.get(console);
	}

	@Nullable
	private GrepHighlightFilter getGrepHighlightFilter(@NotNull ConsoleView console,
			WeakHashMap<ConsoleView, GrepHighlightFilter> weakHighlightersMap, final String grepHighlightFilter1) {
		GrepHighlightFilter grepHighlightFilter = weakHighlightersMap.get(console);
		if (grepHighlightFilter == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Something is wrong. " + grepHighlightFilter1 + " not found for ").append(
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

	public boolean isRegistered(@NotNull ConsoleView console) {
		return weakHighlightersMap.containsKey(console);
	}

	public long getLastExecutionId() {
		return lastExecutionId;
	}

	public void setLastExecutionId(long lastExecutionId) {
		this.lastExecutionId = lastExecutionId;
	}

	public void registerConsole(ConsoleView console) {
		AnsiInputFilter lastAnsi = getLastAnsi();
		if (lastAnsi != null && !lastAnsi.isRegistered()) {
			lastAnsi.setConsole(console);
			this.lastAnsi = null;
		}
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
	private AnsiInputFilter getLastAnsi() {
		if (lastAnsi != null) {
			return lastAnsi.get();
		} else {
			return null;
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
	
}
