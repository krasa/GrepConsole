//package krasa.grepconsole.grep.listener;
//
//import com.intellij.execution.ui.ConsoleViewContentType;
//import com.intellij.openapi.project.Project;
//import krasa.grepconsole.grep.GrepModel;
//import krasa.grepconsole.grep.HybridQueue;
//import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;
//import krasa.grepconsole.model.Profile;
//
//public class GrepFilterAsyncListener implements GrepFilterListener {
//
//	private HybridQueue consoleBuffer;
//	private GrepFilterSyncListener grepGrepFilterSyncListener;
//
//	public GrepFilterAsyncListener(OpenGrepConsoleAction.LightProcessHandler myProcessHandler, Project eventProject, Profile profile) {
//		grepGrepFilterSyncListener = new GrepFilterSyncListener(myProcessHandler, eventProject, profile);
//		this.consoleBuffer = new HybridQueue(new EventConsumer() {
//			@Override
//			public void processEvent(String s) {
//				grepGrepFilterSyncListener.process(s, ConsoleViewContentType.NORMAL_OUTPUT);
//			}
//		});
//	}
//
//	@Override
//	public void modelUpdated(GrepModel grepModel) {
//		grepGrepFilterSyncListener.modelUpdated(grepModel);
//	}
//
//	@Override
//	public void profileUpdated(Profile profile) {
//		grepGrepFilterSyncListener.profileUpdated(profile);
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
