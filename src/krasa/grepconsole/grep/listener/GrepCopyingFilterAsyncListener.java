package krasa.grepconsole.grep.listener;

import com.intellij.execution.ui.ConsoleViewContentType;
import krasa.grepconsole.grep.CopyListenerModel;
import krasa.grepconsole.grep.HybridQueue;
import krasa.grepconsole.grep.OpenGrepConsoleAction;

public class GrepCopyingFilterAsyncListener implements GrepCopyingFilterListener {

	private HybridQueue consoleBuffer;
	private GrepCopyingFilterSyncListener grepCopyingFilterSyncListener;

	public GrepCopyingFilterAsyncListener(CopyListenerModel copyListenerModel, OpenGrepConsoleAction.LightProcessHandler myProcessHandler) {
		grepCopyingFilterSyncListener = new GrepCopyingFilterSyncListener(copyListenerModel, myProcessHandler);
		this.consoleBuffer = new HybridQueue(new EventConsumer() {
			@Override
			public void processEvent(String s) {
				grepCopyingFilterSyncListener.process(s, ConsoleViewContentType.NORMAL_OUTPUT);
			}
		});
	}

	@Override
	public void modelUpdated(CopyListenerModel copyListenerModel) {
		grepCopyingFilterSyncListener.modelUpdated(copyListenerModel);
	}

	@Override
	public void process(String s, ConsoleViewContentType type) {
		consoleBuffer.onData(s, type);
	}

	@Override
	public void clearStats() {
		consoleBuffer.clearStats();
	}

	@Override
	public void dispose() {
		consoleBuffer.dispose();   
	}
}
