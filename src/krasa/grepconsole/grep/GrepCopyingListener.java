package krasa.grepconsole.grep;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Key;

public abstract class GrepCopyingListener {

	private MyMatcher matcher;

	public GrepCopyingListener(CopyListenerModel copyListenerModel) {
		matcher = copyListenerModel.matcher();
	}

	public void process(String s, ConsoleViewContentType type) {
		Key stdout = ProcessOutputTypes.STDOUT;
		if (type == ConsoleViewContentType.ERROR_OUTPUT) {
			stdout = ProcessOutputTypes.STDERR;
		} else if (type == ConsoleViewContentType.SYSTEM_OUTPUT) {
			stdout = ProcessOutputTypes.SYSTEM;
		}
		if (matcher.matches(s)) {
			onMatch(s, stdout);
		}
	}

	protected abstract void onMatch(String s, Key key);

	public void set(CopyListenerModel copyListenerModel) {
		matcher = copyListenerModel.matcher();
	}

}
