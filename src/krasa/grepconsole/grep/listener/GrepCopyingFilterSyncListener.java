package krasa.grepconsole.grep.listener;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Key;
import krasa.grepconsole.grep.CopyListenerModel;
import krasa.grepconsole.grep.OpenGrepConsoleAction;

public class GrepCopyingFilterSyncListener implements GrepCopyingFilterListener {

	private CopyListenerModel.Matcher matcher;
	private final OpenGrepConsoleAction.LightProcessHandler myProcessHandler;

	public GrepCopyingFilterSyncListener(CopyListenerModel copyListenerModel, OpenGrepConsoleAction.LightProcessHandler myProcessHandler) {
		matcher = copyListenerModel.matcher();
		this.myProcessHandler = myProcessHandler;
	}

	@Override
	public void modelUpdated(CopyListenerModel copyListenerModel) {
		matcher = copyListenerModel.matcher();
	}

	@Override
	public void process(String s, ConsoleViewContentType type) {
		Key stdout = ProcessOutputTypes.STDOUT;
		if (type == ConsoleViewContentType.ERROR_OUTPUT) {
			stdout = ProcessOutputTypes.STDERR;
		} else if (type == ConsoleViewContentType.SYSTEM_OUTPUT) {
			stdout = ProcessOutputTypes.SYSTEM;
		}
		if (matcher.matches(s)) {
			myProcessHandler.notifyTextAvailable(s, stdout);
		}
	}

	@Override
	public void clearStats() {

	}

	@Override
	public void dispose() {

	}
}
