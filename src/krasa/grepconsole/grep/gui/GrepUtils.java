package krasa.grepconsole.grep.gui;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import krasa.grepconsole.grep.listener.GrepCopyingFilterListener;

import java.lang.reflect.Method;

public class GrepUtils {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(GrepUtils.class);

	public static void grepThroughExistingText(ConsoleView _originalConsole, GrepCopyingFilterListener copyingListener) {
		if (_originalConsole != null) {
			if (_originalConsole instanceof ConsoleViewImpl) {
				ConsoleViewImpl originalConsole = (ConsoleViewImpl) _originalConsole;
				originalConsole.flushDeferredText();
				Editor editor = originalConsole.getEditor();
				Document document = editor.getDocument();
				String text = document.getText();
				for (String s : text.split("\n")) {
					copyingListener.process(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
				}
			} else if (_originalConsole.getClass().getCanonicalName().equals("com.intellij.javascript.debugger.console.WebConsoleView")) {

				try {
					//TODO NOT TESTED
					//TODO need API
					Class clazz = Class.forName("com.intellij.javascript.debugger.console.WebConsoleView");
					Method getEditorDocument = clazz.getMethod("getEditorDocument");
					Object invoke = getEditorDocument.invoke(_originalConsole);

//					WebConsoleView originalConsole = (WebConsoleView) _originalConsole;
//							Document document = originalConsole.getEditorDocument();
//					originalConsole.flushDeferredText();           //TODO?

					Document document = (Document) invoke;

					String text = document.getText();
					for (String s : text.split("\n")) {
						copyingListener.process(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
					}

				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			} else {
				LOG.error("console not supported " + _originalConsole);
			}
		}
	}
}
