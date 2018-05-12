package krasa.grepconsole.gui;

import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static com.intellij.openapi.application.PathManager.getPluginsPath;

class LivePluginExampleAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		Boolean done = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override
			public Boolean compute() {
				try {

					String livePluginsPath = FileUtilRt.toSystemIndependentName(getPluginsPath() + "/live-plugins");
					String name = uniqueName(livePluginsPath);
					VirtualFile parentFolder = VfsUtil.createDirectoryIfMissing(livePluginsPath + "/" + name);
					if (parentFolder == null) {
						return false;
					}

					copy(parentFolder, "/example/plugin.groovy", "plugin.groovy");
					copy(parentFolder, "/example/support.groovy", "support.groovy");


					ApplicationManager.getApplication().invokeLater(new RefreshToolWindow());
					return true;
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}

	protected void copy(VirtualFile parentFolder, String source, String destination) throws IOException {
		InputStream resourceAsStream = LivePluginExampleAction.class.getResourceAsStream(source);
		String text = FileUtil.loadTextAndClose(resourceAsStream);
		text = text.replace("//REMOVE_COMMENT ", "");
		VirtualFile childData = parentFolder.createChildData("GrepConsole", destination);
		VfsUtil.saveText(childData, text);
	}

	@NotNull
	protected String uniqueName(String path) {
		VirtualFile parentFolder = LocalFileSystem.getInstance().findFileByPath(path);
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
				throw new RuntimeException(e1);
			}
		}
	}
}
