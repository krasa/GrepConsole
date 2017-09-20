package krasa.grepconsole.action;

import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.impl.ConsoleBuffer;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import krasa.grepconsole.tail.MyProcessHandler;
import krasa.grepconsole.tail.TailContentExecutor;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Vojtech Krasa
 */
public class OpenFileInConsoleAction extends DumbAwareAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		if (project == null) return;
		final FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
				BrowseFilesListener.SINGLE_FILE_DESCRIPTOR, project, null);

		final VirtualFile[] choose = fileChooser.choose(null, project);
		if (choose.length > 0) {
			final VirtualFile virtualFile = choose[0];
			final String path1 = virtualFile.getPath();
			openFileInConsole(project, new File(path1));
		}
	}

	public void openFileInConsole(@NotNull final Project project, final File file) {
		final Process process = new MyProcess(file);

		final ProcessHandler osProcessHandler = new MyProcessHandler(process, file.getName(), Charset.defaultCharset()) {
			@Override
			public boolean isSilentlyDestroyOnClose() {
				return true;
			}
		};
		try {
			osProcessHandler.putUserDataIfAbsent(BuildManager.ALLOW_AUTOMAKE, true);
		} catch (NoClassDefFoundError e) {
			//phpstorm does not have it
		}
		final TailContentExecutor executor = new TailContentExecutor(project, osProcessHandler);
		Disposer.register(project, executor);
		executor.withRerun(new Runnable() {
			@Override
			public void run() {
				osProcessHandler.destroyProcess();
				osProcessHandler.waitFor(2000L);
				openFileInConsole(project, file);
			}
		});
		executor.forFile(file);
		executor.withTitle(file.getName());
		executor.withStop(new Runnable() {
			@Override
			public void run() {
				osProcessHandler.destroyProcess();
			}
		}, new Computable<Boolean>() {
			@Override
			public Boolean compute() {
				return !osProcessHandler.isProcessTerminated();
			}
		});
		executor.run();
	}

	private class MyProcess extends Process {
		protected volatile boolean running = true;
		protected FileInputStream inputStream;

		private MyProcess(final File file) {
			try {
				inputStream = new FileInputStream(file);
				long size = inputStream.getChannel().size();
				// close enough, it does not work for binary files very well, but i hope it does at least for text
				inputStream.getChannel().position(Math.max(size - ConsoleBuffer.getCycleBufferSize(), 0));
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
			} finally {
				running = false;
			}
		}
	}

}
