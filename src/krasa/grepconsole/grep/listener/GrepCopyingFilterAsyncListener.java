//package krasa.grepconsole.grep.listener;
//
//import com.intellij.execution.ui.ConsoleViewContentType;
//import com.intellij.openapi.project.Project;
//import krasa.grepconsole.grep.GrepModel;
//import krasa.grepconsole.grep.HybridQueue;
//import krasa.grepconsole.grep.OpenGrepConsoleAction;
//import krasa.grepconsole.model.Profile;
//
//public class GrepCopyingFilterAsyncListener implements GrepCopyingFilterListener {
//
//	private HybridQueue consoleBuffer;
//	private GrepCopyingFilterSyncListener grepCopyingFilterSyncListener;
//
//	public GrepCopyingFilterAsyncListener(OpenGrepConsoleAction.LightProcessHandler myProcessHandler, Project eventProject, Profile profile) {
//		grepCopyingFilterSyncListener = new GrepCopyingFilterSyncListener(myProcessHandler, eventProject, profile);
//		this.consoleBuffer = new HybridQueue(new EventConsumer() {
//			@Override
//			public void processEvent(String s) {
//				grepCopyingFilterSyncListener.process(s, ConsoleViewContentType.NORMAL_OUTPUT);
//			}
//		});
//	}
//
//	@Override
//	public void modelUpdated(GrepModel grepModel) {
//		grepCopyingFilterSyncListener.modelUpdated(grepModel);
//	}
//
//	@Override
//	public void profileUpdated(Profile profile) {
//		grepCopyingFilterSyncListener.profileUpdated(profile);
//	}
//
//	@Override
//	public void process(String s, ConsoleViewContentType type) {
//		consoleBuffer.onData(s, type);
//	}
//
//	@Override
//	public void clearStats() {
//		consoleBuffer.clearStats();
//	}
//
//	@Override
//	public void dispose() {
//		consoleBuffer.dispose();   
//	}
//}
