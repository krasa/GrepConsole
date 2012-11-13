package krasa.grepconsole.console;

import com.intellij.execution.ui.ConsoleViewContentType;

public interface GrepConsoleView {
	void printProcessedResult(String text, ConsoleViewContentType contentType);
}
