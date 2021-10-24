package krasa.grepconsole.grep.listener;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.model.Profile;

public interface GrepFilterListener extends Disposable {
	void modelUpdated(GrepCompositeModel grepModel);

	void profileUpdated(Profile profile);

	void process(String s, ConsoleViewContentType type);

	void clearStats();
}
