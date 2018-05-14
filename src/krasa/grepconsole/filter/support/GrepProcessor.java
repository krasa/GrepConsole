package krasa.grepconsole.filter.support;

import krasa.grepconsole.model.GrepExpressionItem;

/**
 * @author Vojtech Krasa
 */
public abstract class GrepProcessor {
	public abstract GrepExpressionItem getGrepExpressionItem();

	public abstract int getMatches();

	public abstract void resetMatches();

	public abstract FilterState process(FilterState state);

//	
//	private ThreadLocal<String> previousIncompleteToken = new ThreadLocal<>();
//	public String bufferUntilNewLine(String s) {
//		                   
//		if (!s.endsWith("\n")) {
//			if (previousIncompleteToken.get() != null) {
//				previousIncompleteToken.set(previousIncompleteToken.get() + s);
//			} else {
//				previousIncompleteToken.set(s);
//			}
//			return null;
//		}
//
//		if (previousIncompleteToken.get() != null) {
//			s = previousIncompleteToken.get() + s;
//			previousIncompleteToken.set(null);
//		}
//
//
//		return s;
//	}
}
