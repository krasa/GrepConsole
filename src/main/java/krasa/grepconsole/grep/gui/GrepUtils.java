package krasa.grepconsole.grep.gui;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupIterator;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.util.Key;
import krasa.grepconsole.action.MoveErrorStreamToTheBottomAction;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.filter.LockingInputFilterWrapper;
import krasa.grepconsole.grep.listener.GrepFilterListener;

public class GrepUtils {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(GrepUtils.class);
	public static final int MINIMAL_RACE_CONDITION_SAFETY_MS = 5;

	public static void grepThroughExistingText(ConsoleView _originalConsole, GrepFilter grepFilter, GrepFilterListener grepListener) {
		LockingInputFilterWrapper lockingInputFilterWrapper = grepFilter.getLockingInputFilterWrapper();
		try {
			lockingInputFilterWrapper.lock();

			if (_originalConsole != null) {
				if (_originalConsole instanceof ConsoleViewImpl) {
					ConsoleViewImpl originalConsole = (ConsoleViewImpl) _originalConsole;

					flushAndProcess(grepListener, originalConsole);

					if (originalConsole.hasDeferredOutput()) {
						flushAndProcess(grepListener, originalConsole);
					}

					long delta = System.currentTimeMillis() - lockingInputFilterWrapper.getLockedSince();
					if (delta < MINIMAL_RACE_CONDITION_SAFETY_MS) {
						try {
							Thread.sleep(MINIMAL_RACE_CONDITION_SAFETY_MS - delta); //just making it extra safe
							if (originalConsole.hasDeferredOutput()) {
								flushAndProcess(grepListener, originalConsole);
							}
						} catch (InterruptedException e) {
							LOG.error(e);
						}
					}


//				} else if (_originalConsole.getClass().getCanonicalName().equals("com.intellij.javascript.debugger.console.WebConsoleView")) {
//
//					try {
//						Class clazz = Class.forName("com.intellij.javascript.debugger.console.WebConsoleView");
//						Method getEditorDocument = clazz.getMethod("getEditorDocument");
//						Object invoke = getEditorDocument.invoke(_originalConsole);
//
//						//					WebConsoleView originalConsole = (WebConsoleView) _originalConsole;
//						//					Document document = originalConsole.getEditorDocument();
//						//					originalConsole.flushDeferredText();           //TODO?
//
//						Document document = (Document) invoke;
//
//						String text = document.getText();
//						for (String s : text.split("\n")) {
//							grepListener.process(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
//						}
//
//					} catch (Throwable e) {
//						throw new RuntimeException(e);
//					}
				} else {
					LOG.error("Report this: console not supported, existing text not grepped " + _originalConsole);
				}
			}
		} finally {
			lockingInputFilterWrapper.unlock();
		}
	}

	protected static void flushAndProcess(GrepFilterListener grepListener, ConsoleViewImpl originalConsole) {
		originalConsole.flushDeferredText();
		Editor editor = originalConsole.getEditor();
		if (editor != null) {
			Document document = editor.getDocument();

			Key<ConsoleViewContentType> contentTypeKey = MoveErrorStreamToTheBottomAction.findConsoleViewContentTypeKey();
			if (contentTypeKey != null) {
				MarkupModelEx model = (MarkupModelEx) DocumentMarkupModel.forDocument(editor.getDocument(), originalConsole.getProject(), false);
				MarkupIterator<RangeHighlighterEx> iterator = model.overlappingIterator(0, document.getTextLength());
				int last = 0;
				ConsoleViewContentType lastType = ConsoleViewContentType.NORMAL_OUTPUT;
				String text = document.getText();

				try {
					while (iterator.hasNext()) {
						RangeHighlighterEx next = iterator.next();
						if (!next.isValid()) {
							continue;
						}

						ConsoleViewContentType contentType = next.getUserData(contentTypeKey);
						int startOffset = next.getStartOffset();
						int endOffset = next.getEndOffset();

						if (last < startOffset) {
							grepListener.process(text.substring(last, startOffset), lastType);
						}
						grepListener.process(text.substring(startOffset, endOffset), contentType);

						last = endOffset;
						lastType = contentType;
					}
				} finally {
					iterator.dispose();
				}
				grepListener.process(text.substring(last), lastType);
			} else {
				grepListener.process(document.getText(), ConsoleViewContentType.NORMAL_OUTPUT);
			}
		}
	}
}
