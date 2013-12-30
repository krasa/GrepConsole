package krasa.grepconsole.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.process.DefaultJavaProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

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

	protected void openFileInConsole(Project project, String path) {
		final Process process = new MyProcess(path);
		ProcessHandler osProcessHandler = new DefaultJavaProcessHandler(process, null, Charset.defaultCharset());
		RunContentExecutor executor = new RunContentExecutor(project, osProcessHandler);
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
