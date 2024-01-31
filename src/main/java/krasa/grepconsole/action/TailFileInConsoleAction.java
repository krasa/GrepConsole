package krasa.grepconsole.action;

import com.intellij.execution.impl.ConsoleBuffer;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.PluginState;
import krasa.grepconsole.plugin.TailHistory;
import krasa.grepconsole.tail.MyProcessHandler;
import krasa.grepconsole.tail.TailContentExecutor;
import krasa.grepconsole.tail.runConfiguration.TailRunConfigurationSettings;
import krasa.grepconsole.tail.runConfiguration.TailSettingsEditor;
import krasa.grepconsole.tail.runConfiguration.TailUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class TailFileInConsoleAction extends MyDumbAwareAction {
	private static final Logger LOG = Logger.getInstance(TailFileInConsoleAction.class);
	public static final String TAIL_FILE_IN_CONSOLE_ACTION_LAST_FILE = "TailFileInConsoleAction.lastFile";
	private VirtualFile lastVirtualFile;


	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		if (project == null) return;

		TailSettings tailSettings = PluginState.getInstance().getTailSettings();
		TailRunConfigurationSettings lastTail = tailSettings.getLastTail();
		if (tailSettings.isAdvancedTailDialog()) {
			showAdvancedDialog(project, tailSettings, lastTail);
		} else {
			final FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
					new FileChooserDescriptor(true, true, false, false, false, true), project, null);

			PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
			String value = propertiesComponent.getValue(TAIL_FILE_IN_CONSOLE_ACTION_LAST_FILE);
			VirtualFile lastFile = null;
			if (value != null) {
				lastFile = VirtualFileManager.getInstance().findFileByUrl(value);
			}
			final VirtualFile[] choose;
			if (lastFile != null) {
				choose = fileChooser.choose(project, lastFile);
			} else {
				choose = fileChooser.choose(project);
			}
			TailHistory.getState(project).add(choose);
			for (VirtualFile virtualFile : choose) {
				propertiesComponent.setValue(TAIL_FILE_IN_CONSOLE_ACTION_LAST_FILE, virtualFile.getUrl());
				TailUtils.openAllMatching(virtualFile.getPath(), false, file -> openFileInConsole(project, file, resolveEncoding(file)));
			}
		}
	}

	protected void showAdvancedDialog(Project project, TailSettings tailSettings, TailRunConfigurationSettings settings) {
		TailSettingsEditor form = new TailSettingsEditor(project);
		form.resetEditorFrom(settings);

		DialogBuilder builder = new DialogBuilder(project);
		builder.setCenterPanel(form.getComponent());
		builder.setPreferredFocusComponent(form.getPreferredFocusComponent());
		builder.setDimensionServiceKey("TailFileInConsoleActionDialog");
		builder.setTitle("Tail File");
		builder.removeAllActions();
		builder.addOkAction();
		builder.addCancelAction();

		boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
		if (isOk) {
			form.applyEditorTo(settings);
			List<String> paths = settings.getPaths();

			TailHistory.getState(project).add(paths, settings.isSelectNewestMatchingFile(), settings.getEncoding(), settings.isAutodetectEncoding());

			for (String path : paths) {
				TailUtils.openAllMatching(path, settings.isSelectNewestMatchingFile(), file -> openFileInConsole(project, file, settings.resolveEncoding(file)));
			}

			tailSettings.setLastTail(settings.clearPaths());
		}
	}

	public static void openFileInConsole(@NotNull final Project project, final File file, final Charset charset) {
		if (file == null || !file.exists() || !file.isFile()) {
			return;
		}

		final Process process = new MyProcess(file);

		final ProcessHandler osProcessHandler = new MyProcessHandler(process, file.getName(), charset) {
			@Override
			public boolean isSilentlyDestroyOnClose() {
				return true;
			}
		};
		osProcessHandler.putUserDataIfAbsent(TailContentExecutor.FILE_PATH, file.getAbsolutePath());
		try {
			osProcessHandler.putUserDataIfAbsent(com.intellij.compiler.server.BuildManager.ALLOW_AUTOMAKE, true);
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
				openFileInConsole(project, file, charset);
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

	@NotNull
	public static Charset resolveEncoding(File file, boolean autodetectEncoding, String defaultEncoding) {
		String encoding = null;
		if (autodetectEncoding) {
			encoding = detectEncoding(file);
		}
		if (StringUtils.isEmpty(encoding)) {
			encoding = defaultEncoding;
		}
		if (StringUtils.isEmpty(encoding)) {
			final TailSettings tailSettings = GrepConsoleApplicationComponent.getInstance().getState().getTailSettings();
			encoding = tailSettings.getDefaultEncoding();
		}
		return Charset.forName(encoding);
	}

	@NotNull
	public static Charset resolveEncoding(File file) {
		final TailSettings tailSettings = GrepConsoleApplicationComponent.getInstance().getState().getTailSettings();
		String encoding = null;
		if (tailSettings.isAutodetectEncoding()) {
			encoding = detectEncoding(file);
		}
		if (StringUtils.isEmpty(encoding)) {
			encoding = tailSettings.getDefaultEncoding();
		}
		return Charset.forName(encoding);
	}

	@Nullable
	public static String detectEncoding(File file) {
		String encoding = null;
		try {
			try (FileInputStream stream = new FileInputStream(file)) {
				encoding = UniversalDetector.detectCharset(stream);
				LOG.debug("AutoDetected encoding: " + encoding);
				Charset.forName(encoding);
			}
		} catch (Throwable e) {
			LOG.debug(e);
		}
		return encoding;
	}

	public static class MyProcess extends Process {
		protected volatile boolean running = true;
		protected InputStream inputStream;
		private File file;

		public MyProcess(final File file) {
			this.file = file;
			try {
				if (!file.exists()) {
					String s = "File not found '" + file.getAbsolutePath() + "'";
					inputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
					running = false;
				} else {
					FileInputStream inputStream = new FileInputStream(file);
					if (file.isFile()) {// Illegal seek for a named pipe on Linux #135
						long size = inputStream.getChannel().size();
						// close enough, it does not work for binary files very well, but i hope it does at least for text
						inputStream.getChannel().position(Math.max(size - ConsoleBuffer.getCycleBufferSize(), 0));
					}
					this.inputStream = inputStream;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
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
