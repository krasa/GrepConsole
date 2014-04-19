package krasa.grepconsole.action;

import com.intellij.execution.process.DefaultJavaProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import krasa.grepconsole.integration.RunContentExecutor;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Vojtech Krasa
 */
public class OpenFileInConsole extends DumbAwareAction {

	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		final FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
				BrowseFilesListener.SINGLE_FILE_DESCRIPTOR, project, null);

		final VirtualFile[] choose = fileChooser.choose(null, project);
		if (choose.length > 0) {
			final VirtualFile virtualFile = choose[0];
			final String path1 = virtualFile.getPath();
			openFileInConsole(project, path1);
		}
	}

	protected void openFileInConsole(final Project project, final String path) {
		final Process process = new MyProcess(path);
		final ProcessHandler osProcessHandler = new DefaultJavaProcessHandler(process, null, Charset.defaultCharset());
		final RunContentExecutor executor = new RunContentExecutor(project, osProcessHandler);
		executor.withRerun(new Runnable() {
			@Override
			public void run() {
				osProcessHandler.destroyProcess();
				osProcessHandler.waitFor(2000L);
				openFileInConsole(project, path);
			}
		});
		executor.run();
	}

	private class MyProcess extends Process {
		protected volatile boolean running = true;
		protected InputStream inputStream;

		private MyProcess(final String path) {
			try {
				inputStream = new FileInputStream(new File(path));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public OutputStream getOutputStream() {
			return new OutputStream() {
				@Override
				public void write(int b) throws IOException {

				}
			};
		}

		@Override
		public InputStream getInputStream() {
			return inputStream;

		}

		@Override
		public InputStream getErrorStream() {
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return 0;
				}
			};
		}

		@Override
		public int waitFor() throws InterruptedException {
			while (running) {
				Thread.sleep(1000);
			}
			return 0;
		}

		@Override
		public int exitValue() {
			return 0;
		}

		@Override
		public void destroy() {
			try {
				inputStream.close();
			} catch (IOException e) {
				// who cares
			}
			running = false;
		}
	}

}
