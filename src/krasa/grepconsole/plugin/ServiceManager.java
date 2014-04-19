package krasa.grepconsole.plugin;

import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.*;
import krasa.grepconsole.grep.Cache;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Vojtech Krasa
 */
public class ServiceManager {

	private static final ServiceManager SERVICE_MANAGER = new ServiceManager();

	private List<WeakReference<GrepHighlightFilter>> cacheHighlight = new ArrayList<WeakReference<GrepHighlightFilter>>();
	private List<WeakReference<GrepInputFilter>> cacheInput = new ArrayList<WeakReference<GrepInputFilter>>();
	private List<WeakReference<AnsiInputFilter>> cacheAnsi = new ArrayList<WeakReference<AnsiInputFilter>>();
	private AnsiInputFilter lastAnsi;

	public static ServiceManager getInstance() {
		return SERVICE_MANAGER;
	}

	public void resetSettings() {
		iterate(cacheHighlight);
		iterate(cacheInput);
		iterate(cacheAnsi);
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
		cacheInput.add(new WeakReference<GrepInputFilter>(grepInputFilter));
		return grepInputFilter;
	}

	public AnsiInputFilter createAnsiFilter(Project project) {
		AnsiInputFilter service = new AnsiInputFilter(project);
		cacheAnsi.add(new WeakReference<AnsiInputFilter>(service));
		lastAnsi = service;
		return service;
	}

	public GrepHighlightFilter createHighlightFilter(Project project) {
		final GrepHighlightFilter grepHighlightFilter = new GrepHighlightFilter(project);
		cacheHighlight.add(new WeakReference<GrepHighlightFilter>(grepHighlightFilter));
		return grepHighlightFilter;
	}

	public AnsiInputFilter getLastAnsi() {
		return lastAnsi;
	}
}
