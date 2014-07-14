package krasa.grepconsole.action;

import com.intellij.openapi.util.Disposer;
import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

import java.util.concurrent.atomic.AtomicReference;
import krasa.grepconsole.tail.TailContentExecutor;

import com.intellij.execution.impl.ConsoleBuffer;
import com.intellij.execution.process.BaseOSProcessHandler;
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
public class OpenFileInConsoleAction extends DumbAwareAction {

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

	public void openFileInConsole(final Project project, final String path) {
		final Process process = new MyProcess(path);
		
		final AtomicReference<WeakReference<TailContentExecutor>> atomicReference = new AtomicReference<WeakReference<TailContentExecutor>>();
		final ProcessHandler osProcessHandler = new BaseOSProcessHandler(process, null, Charset.defaultCharset()) {
			@Override
			public boolean isSilentlyDestroyOnClose() {
				return true;
			}

			@Override
			public void destroyProcess() {
				super.destroyProcess();
				WeakReference<TailContentExecutor> tailContentExecutorWeakReference = atomicReference.get();
				if (tailContentExecutorWeakReference != null) {
					TailContentExecutor disposable = tailContentExecutorWeakReference.get();
					if (disposable != null) {
						disposable.dispose();
					}
				}
			}
		};
		final TailContentExecutor executor = new TailContentExecutor(project, osProcessHandler);
		final WeakReference<TailContentExecutor> weakReference = new WeakReference<TailContentExecutor>(executor);
		atomicReference.set(weakReference);
		
		executor.withRerun(new Runnable() {
			@Override
			public void run() {
				osProcessHandler.destroyProcess();
				osProcessHandler.waitFor(2000L);
				openFileInConsole(project, path);
			}
		});
		executor.withTitle(new File(path).getName());
		executor.run();
	}
	private class MyProcess extends Process {
		protected volatile boolean running = true;
		protected FileInputStream inputStream;

		private MyProcess(final String path) {
			try {
				inputStream = new FileInputStream(new File(path));
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
			}finally {
				running = false;
			}
		}
	}

}
