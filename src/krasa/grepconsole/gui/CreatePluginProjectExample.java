package krasa.grepconsole.gui;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.util.io.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

public class CreatePluginProjectExample implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			VirtualFile virtualFile = FileChooser.chooseFile(BrowseFilesListener.SINGLE_DIRECTORY_DESCRIPTOR, ProjectImportBuilder.getCurrentProject(), null);
			if (virtualFile == null) {
				return;
			}
			String canonicalPath = virtualFile.getCanonicalPath();

			File outputDir = new File(canonicalPath);
			if (outputDir.exists() && outputDir.isDirectory() && outputDir.list().length > 0) {
				outputDir = new File(outputDir, "GrepConsoleExtension");
			}
			outputDir.mkdirs();

			URL resource = CreatePluginProjectExample.class.getResource("/example/ExtensionProjectExample.jar");
			File jar = new File(outputDir, "temp.jar");
			FileUtils.copyURLToFile(resource, jar);

			boolean exists = jar.exists();
			if (!exists) {
				throw new RuntimeException(resource.getPath());
			}
			ZipUtil.extract(jar, outputDir, TrueFileFilter.TRUE);

			jar.delete();

			ShowFilePathAction.openDirectory(outputDir);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

}
