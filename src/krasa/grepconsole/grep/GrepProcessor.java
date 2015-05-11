package krasa.grepconsole.grep;

import krasa.grepconsole.model.GrepExpressionItem;

/**
 * @author Vojtech Krasa
 */
public interface GrepProcessor {
	GrepExpressionItem getGrepExpressionItem();

	int getMatches();

	void resetMatches();

	FilterState process(FilterState state);
}
