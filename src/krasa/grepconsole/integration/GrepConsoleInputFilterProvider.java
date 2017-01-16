package krasa.grepconsole.integration;

import java.util.List;

import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.filter.GrepHighlightingInputFilter;
import krasa.grepconsole.filter.GrepInputFilter;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		ServiceManager manager = ServiceManager.getInstance();

		GrepHighlightingInputFilter highlightInputFilter = null;
		AnsiInputFilter ansiFilter = null;
		GrepInputFilter inputFilter = manager.createInputFilter(project);

		if (Integer.parseInt(ApplicationInfo.getInstance().getMajorVersion()) >= 163) {
			highlightInputFilter = createHighlightingFilter(project, manager);
		} else {
			ansiFilter = manager.createAnsiFilter(project);
		}

		return new InputFilter[] { manager.createCopyingFilter(project),
				new MyCompositeInputFilter(inputFilter, highlightInputFilter, ansiFilter) };
	}

	protected GrepHighlightingInputFilter createHighlightingFilter(@NotNull Project project, ServiceManager manager) {
		if (GrepConsoleApplicationComponent.getInstance().getState().isSynchronousHighlighting()) {
			return manager.createHighlightInputFilter(project);
		}
		return null;
	}

	private static class MyCompositeInputFilter implements InputFilter {
		private final GrepInputFilter inputFilter;
		private final GrepHighlightingInputFilter highlightInputFilter;
		private final AnsiInputFilter ansiFilter;

		public MyCompositeInputFilter(GrepInputFilter inputFilter, GrepHighlightingInputFilter highlightInputFilter,
				AnsiInputFilter ansiFilter) {
			this.inputFilter = inputFilter;
			this.highlightInputFilter = highlightInputFilter;
			this.ansiFilter = ansiFilter;
		}

		@Nullable
		@Override
		public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
				ConsoleViewContentType consoleViewContentType) {
			if (inputFilter != null) {
				List<Pair<String, ConsoleViewContentType>> pairs = inputFilter.applyFilter(s, consoleViewContentType);

				if (pairs != null && pairs.size() == 1) {// excluded
					return pairs;
				}
			}

			if (highlightInputFilter != null) {
				return highlightInputFilter.applyFilter(s, consoleViewContentType);
			}
			if (ansiFilter != null) {
				return ansiFilter.applyFilter(s, consoleViewContentType);
			}

			return null;
		}
	}
}
