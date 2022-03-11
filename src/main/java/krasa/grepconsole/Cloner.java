package krasa.grepconsole;

import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;

public class Cloner {
	public static <T> T deepClone(T o) {
		com.rits.cloning.Cloner cloner = new com.rits.cloning.Cloner();
		cloner.setNullTransient(true);
		T t = cloner.deepClone(o);

		if (t instanceof GrepExpressionItem) {
			((GrepExpressionItem) t).generateNewId();
		} else if (t instanceof GrepExpressionGroup) {
			GrepExpressionGroup t1 = (GrepExpressionGroup) t;
			for (GrepExpressionItem grepExpressionItem : t1.getGrepExpressionItems()) {
				grepExpressionItem.generateNewId();
			}
		}
		return t;
	}
}
