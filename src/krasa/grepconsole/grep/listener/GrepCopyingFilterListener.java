package krasa.grepconsole.grep.listener;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import krasa.grepconsole.grep.CopyListenerModel;

public interface GrepCopyingFilterListener extends Disposable {
	void modelUpdated(CopyListenerModel copyListenerModel);

	void process(String s, ConsoleViewContentType type);

	void clearStats();
}
