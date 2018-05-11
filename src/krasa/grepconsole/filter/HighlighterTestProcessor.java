package krasa.grepconsole.filter;

import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * In case you want to keep lines that are matched by highlighters and remove everything else.
 * This wastes CPU as actual highlighting is done much later.
 */
public class HighlighterTestProcessor implements GrepProcessor {
	private List<GrepProcessor> grepProcessors = new ArrayList<>();
	private static final GrepExpressionItem DUMMY = new GrepExpressionItem();

	public HighlighterTestProcessor(List<GrepExpressionItem> allGrepExpressionItems) {
		for (GrepExpressionItem allGrepExpressionItem : allGrepExpressionItems) {
			GrepProcessor processor = allGrepExpressionItem.createProcessor();
			grepProcessors.add(processor);
		}
	}

	@Override
	public GrepExpressionItem getGrepExpressionItem() {
		return DUMMY;
	}

	@Override
	public int getMatches() {
		return 0;
	}

	@Override
	public void resetMatches() {

	}

	@Override
	public FilterState process(FilterState state) {
		for (GrepProcessor grepProcessor : grepProcessors) {
			FilterState process = grepProcessor.process(state);
			if (process.isMatchesSomething()) {
				break;
			}
		}
		state.setNextOperation(Operation.CONTINUE_MATCHING);
		return state;
	}
}
