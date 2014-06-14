package krasa.grepconsole.plugin;

import java.lang.ref.WeakReference;
import java.util.*;

import krasa.grepconsole.filter.*;
import krasa.grepconsole.grep.Cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;

/**
 * @author Vojtech Krasa
 */
public class ServiceManager {

	private static final ServiceManager SERVICE_MANAGER = new ServiceManager();

	private List<WeakReference<GrepHighlightFilter>> highlightFilters = new ArrayList<WeakReference<GrepHighlightFilter>>();
	private List<WeakReference<GrepInputFilter>> inputFilters = new ArrayList<WeakReference<GrepInputFilter>>();
	private List<WeakReference<AnsiInputFilter>> ansiFilters = new ArrayList<WeakReference<AnsiInputFilter>>();
	private WeakReference<AnsiInputFilter> lastAnsi;
	private WeakReference<GrepHighlightFilter> lastGrepHighlightFilter;
	private WeakHashMap<ConsoleView, GrepHighlightFilter> weakHashMap = new WeakHashMap<ConsoleView, GrepHighlightFilter>();
	private long lastExecutionId;

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

	public GrepHighlightFilter createHighlightFilter(Project project) {
		final GrepHighlightFilter grepHighlightFilter = new GrepHighlightFilter(project);
		grepHighlightFilter.setExecutionId(getLastExecutionId());
		highlightFilters.add(new WeakReference<GrepHighlightFilter>(grepHighlightFilter));
		lastGrepHighlightFilter = new WeakReference<GrepHighlightFilter>(grepHighlightFilter);
		return grepHighlightFilter;
	}

	@Nullable
	public AnsiInputFilter getLastAnsi() {
		if (lastAnsi != null) {
			return lastAnsi.get();
		} else {
			return null;
		}
	}

	@Nullable
	public GrepHighlightFilter getLastGrepHighlightFilter() {
		if (lastGrepHighlightFilter != null) {
			return lastGrepHighlightFilter.get();
		} else {
			return null;
		}
	}

	@Nullable
	public GrepHighlightFilter getHighlightFilter(@NotNull ConsoleView console) {
		return weakHashMap.get(console);
	}

	public void register(@NotNull ConsoleView console, @NotNull GrepHighlightFilter lastGrepHighlightFilter) {
		weakHashMap.put(console, lastGrepHighlightFilter);
	}

	public void setLastExecutionId(long lastExecutionId) {
		this.lastExecutionId = lastExecutionId;
	}

	public long getLastExecutionId() {
		return lastExecutionId;
	}
}
