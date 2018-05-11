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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.lang.reflect.Method;

import static com.intellij.openapi.application.PathManager.getPluginsPath;

class LivePluginExampleAction implements ActionListener {
	private final JButton addLivePluginScript;

	public LivePluginExampleAction(JButton addLivePluginScript) {
		this.addLivePluginScript = addLivePluginScript;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Boolean done = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override
			public Boolean compute() {
				try {
					InputStream resourceAsStream = LivePluginExampleAction.class.getResourceAsStream("LivePlugin.groovy.txt");
					String text = FileUtil.loadTextAndClose(resourceAsStream);

					String livePluginsPath = FileUtilRt.toSystemIndependentName(getPluginsPath() + "/live-plugins");
					String name = uniqueName(livePluginsPath);
					VirtualFile parentFolder = VfsUtil.createDirectoryIfMissing(livePluginsPath + "/" + name);
					if (parentFolder == null) {
						return false;
					}

					VirtualFile childData = parentFolder.createChildData("GrepConsole", "plugin.groovy");
					VfsUtil.saveText(childData, text);


					ApplicationManager.getApplication().invokeLater(new RefreshToolWindow());
					return true;
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		if (done) {
			addLivePluginScript.setEnabled(false);
		}
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
