package krasa.grepconsole.gui;

import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static com.intellij.openapi.application.PathManager.getScratchPath;

@Deprecated
class LivePluginExampleAction implements ActionListener {

	private final Project project;

	public LivePluginExampleAction(Project project) {
		this.project = project;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Boolean done = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override
			public Boolean compute() {
				try {

					String livePluginsPath = FileUtilRt.toSystemIndependentName(getScratchPath() + "/live-plugins");
					VfsUtil.createDirectoryIfMissing(livePluginsPath);
					String name = uniqueName(livePluginsPath);
					VirtualFile parentFolder = VfsUtil.createDirectoryIfMissing(livePluginsPath + "/" + name);
					if (parentFolder == null) {
						return false;
					}

					VirtualFile copy = copy(parentFolder, "/example/plugin.groovy", "plugin.groovy");

					//does not work properly in IJ 2016
					Project currentProject = project;
					if (currentProject == null) {
						currentProject = (Project) CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
					}
					if (currentProject != null) {
						FileEditorManagerEx.getInstanceEx(currentProject).openFile(copy, false);
						ToolWindow plugins = ToolWindowManager.getInstance(currentProject).getToolWindow("Plugins");
						if (plugins != null) {
							plugins.show(null);
						}
					}

					ApplicationManager.getApplication().invokeLater(new RefreshToolWindow());
					return true;
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}

	protected VirtualFile copy(VirtualFile parentFolder, String source, String destination) throws IOException {
		InputStream resourceAsStream = LivePluginExampleAction.class.getResourceAsStream(source);
		String text = FileUtil.loadTextAndClose(resourceAsStream);
		text = text.replace("//UNCOMMENT_THIS ", "");
		VirtualFile childData = parentFolder.createChildData("GrepConsole", destination);
		VfsUtil.saveText(childData, text);
		return childData;
	}

	@NotNull
	protected String uniqueName(String path) {
		VirtualFile parentFolder = LocalFileSystem.getInstance().findFileByPath(path);
		if (parentFolder == null) {
			throw new RuntimeException(path + " not found");
		}
		String name = "GrepConsole";

		if (parentFolder.findChild("GrepConsole") != null) {
			int i = 0;
			name = "GrepConsole-" + i;
			while (parentFolder.findChild(name) != null) {
				name = "GrepConsole-" + ++i;
			}
		}
		return name;
	}

	private static class RefreshToolWindow implements Runnable {
		private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(RefreshToolWindow.class);

		@Override
		public void run() {
			try {
				IdeaPluginDescriptorImpl descriptor = (IdeaPluginDescriptorImpl) PluginManager.getPlugin(PluginId.getId("LivePlugin"));
				if (descriptor == null || !descriptor.isEnabled()) {
					return;
				}
				ClassLoader pluginClassLoader = descriptor.getPluginClassLoader();
				Class<?> clazz = pluginClassLoader.loadClass("liveplugin.toolwindow.RefreshPluginsPanelAction");
				Method actionPerformed = clazz.getMethod("actionPerformed", AnActionEvent.class);
				actionPerformed.setAccessible(true);
				actionPerformed.invoke(clazz.newInstance(), new Object[]{null});
			} catch (Exception e1) {
				LOG.warn(e1);
			}
		}
	}
}
