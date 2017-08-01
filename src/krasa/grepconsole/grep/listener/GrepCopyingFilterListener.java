package krasa.grepconsole.grep.listener;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import krasa.grepconsole.grep.GrepModel;
import krasa.grepconsole.model.Profile;

public interface GrepCopyingFilterListener extends Disposable {
	void modelUpdated(GrepModel grepModel);

	void profileUpdated(Profile profile);

	void process(String s, ConsoleViewContentType type);

	void clearStats();
}
