package krasa.grepconsole.service;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.service.ansi.AnsiConsoleStyleProcessor;

public class AnsiFilterService extends AbstractGrepService implements InputFilter {
	int i;
	static Map<Integer, ConsoleViewContentType> cache = new HashMap<Integer, ConsoleViewContentType>();
	protected AnsiConsoleStyleProcessor ansiConsoleStyleProcessor;

	public AnsiFilterService(Project project) {
		super(project);
		ansiConsoleStyleProcessor = new AnsiConsoleStyleProcessor();
	}

	public AnsiFilterService(Profile profile, List<GrepFilter> grepFilters) {
		super(profile, grepFilters);
		ansiConsoleStyleProcessor = new AnsiConsoleStyleProcessor();
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s, ConsoleViewContentType consoleViewContentType) {
		List<Pair<String, ConsoleViewContentType>> list = ansiConsoleStyleProcessor.process(s, consoleViewContentType);
		if (list.isEmpty()) {
			return null;
		}
		return list;
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter();
	}

}
