package krasa.grepconsole.tail.runConfiguration;

import com.intellij.util.Consumer;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TailUtils {
	public static void openAllMatching(String path, boolean selectNewestMatchingFile, Consumer<File> opener) {
		if (StringUtils.isBlank(path)) {
			return;
		}
		File file = new File(path);
		if (file.isFile()) {
			opener.consume(file);
		} else if (file.isDirectory()) {
			if (selectNewestMatchingFile) {
				opener.consume(getLatestFile(file));
			} else {
				openAll(file.listFiles(), opener);
			}
		} else {
			List<File> matching = getMatching(file);
			if (selectNewestMatchingFile) {
				opener.consume(getLatestFile(matching.toArray(File[]::new)));
			} else {
				openAll(matching.toArray(File[]::new), opener);
			}
		}
	}

	public static String sanitize(String filePath) {
		String replace = filePath.replace("\\", "/");
		//file path starting with // causes major delays for some reason
		replace = replace.replaceFirst("/+", "/");
		return replace;
	}

	private static void openAll(File[] files, Consumer<File> consumer) {
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					consumer.consume(f);
				}
			}
		}
	}

	protected static List<File> getMatching(File file) {
		List<File> files = new ArrayList<>();

		File parentDir = file.getParentFile();
		if (parentDir == null) {
			return Collections.emptyList();
		}
		//could be some shit like '/*', do not use canonical path
		String path = TailUtils.sanitize(file.getAbsolutePath());
		String fileName = org.apache.commons.lang3.StringUtils.substringAfterLast(path, "/");

		if (fileName.length() > 0 && !file.isDirectory() && parentDir.isDirectory()) {
			FileFilter filter = new WildcardFileFilter(fileName, IOCase.SYSTEM);
			File[] foundFiles = parentDir.listFiles(filter);
			if (foundFiles != null) {
				for (File f : foundFiles) {
					if (f.isFile()) {
						files.add(f);
					}
				}
				if (foundFiles.length == 0) {
					foundFiles = parentDir.listFiles((FilenameFilter) new PrefixFileFilter(fileName + ".", IOCase.SYSTEM));
					if (foundFiles != null) {
						files.addAll(Arrays.asList(foundFiles));
					}
				}
			}

		}

		return files;
	}

	private static File getLatestFile(File dir) {
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}

		return getLatestFile(files);
	}

	private static File getLatestFile(File[] files) {
		File lastModifiedFile = null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && (lastModifiedFile == null || lastModifiedFile.lastModified() < files[i].lastModified())) {
				lastModifiedFile = files[i];
			}
		}
		return lastModifiedFile;
	}

}
