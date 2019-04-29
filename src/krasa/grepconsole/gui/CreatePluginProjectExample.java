package krasa.grepconsole.gui;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.ZipUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CreatePluginProjectExample implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
//			VirtualFile virtualFile =null;
			VirtualFile virtualFile = FileChooser.chooseFile(BrowseFilesListener.SINGLE_DIRECTORY_DESCRIPTOR, com.intellij.projectImport.ProjectImportBuilder.getCurrentProject(), null);
			if (virtualFile == null) {
				return;
			}
			String canonicalPath = virtualFile.getCanonicalPath();

			File outputDir = new File(canonicalPath);
			if (outputDir.exists() && outputDir.isDirectory() && outputDir.list().length > 0) {
				outputDir = new File(outputDir, "GrepConsoleExtension");
			}
			outputDir.mkdirs();

			InputStream resource = CreatePluginProjectExample.class.getResourceAsStream("/example/ExtensionProjectExample.jar");
			File jar = new File(outputDir, "temp.jar");

			FileOutputStream outputStream = new FileOutputStream(jar);
			try {
				StreamUtil.copyStreamContent(resource, outputStream);
			} finally {
				outputStream.close();
				resource.close();
			}
			
			boolean exists = jar.exists();
			if (!exists) {
				throw new RuntimeException(jar.getPath());
			}
			ZipUtil.extract(jar, outputDir, (dir, name) -> true);

			jar.delete();

			ShowFilePathAction.openDirectory(outputDir);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

}
