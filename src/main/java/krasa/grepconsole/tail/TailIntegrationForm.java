package krasa.grepconsole.tail;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.PathUtil;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.tail.remotecall.RemoteCallService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

public class TailIntegrationForm {
	private final Logger log = Logger.getInstance(TailIntegrationForm.class);
	private JPanel contentPane;
	private JTextField port;
	private JCheckBox listenOnPortCheckBox;
	private JButton windowsIntegration;
	private JLabel windowsIntegrationLabel;
	private JTextArea openFileInConsoleTextArea;
	private JButton bindButton;
	private JTextField defaultEncoding;
	private JCheckBox autodetectEncoding;
	private JCheckBox advancedTailDialog;

	public TailIntegrationForm() {
		for (JToggleButton button : Arrays.asList(listenOnPortCheckBox)) {
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateComponents();
				}
			});
		}

		for (JTextField field : Arrays.asList(port)) {
			field.getDocument().addDocumentListener(new DocumentAdapter() {
				@Override
				protected void textChanged(DocumentEvent e) {
					updateComponents();
				}
			});
		}
		windowsIntegration.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (WindowsRegistryChange.isSetupped(getJarPath(), getPort())) {
						WindowsRegistryChange.remove();
					} else {
						String path = getJarPath();
						if (path == null) {
							return;
						}
						WindowsRegistryChange.setup(path, getPort());
						listenOnPortCheckBox.setSelected(true);
					}
				} catch (final Exception e1) {
					ApplicationManager.getApplication().invokeLater(new Runnable() {
						@Override
						public void run() {
							Messages.showMessageDialog("Windows integration error: " + e1.toString(),
									"GrepConsole Plugin Error", Messages.getErrorIcon());
						}
					});
					log.error("Windows integration error", e1);
				}
				updateComponents();
			}

		});
		bindButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TailSettings tailSettings = new TailSettings();
				getData(tailSettings);
				boolean rebind = rebind(tailSettings);
				if (rebind) {
					Messages.showMessageDialog("Rebind OK", "Rebind OK", Messages.getInformationIcon());
				}
			}
		});
	}

	public boolean rebind(TailSettings tailSettings) {
		final RemoteCallService instance = RemoteCallService.getInstance();
		return instance.rebind(tailSettings);
	}

	private void updateComponents() {
		updateWindowsIntegration();
		bindButton.setEnabled(listenOnPortCheckBox.isSelected());
	}

	private void updateWindowsIntegration() {
		if (SystemInfo.isWindows) {
			try {
				final boolean setupped = WindowsRegistryChange.isSetupped(getJarPath(), getPort());
				windowsIntegration.setText(setupped ? "Remove Windows integration" : "Integrate with Windows context menu");
			} catch (Throwable e) {
				log.error(e);
				windowsIntegration.setEnabled(false);
				windowsIntegrationLabel.setEnabled(false);
			}
		} else {
			windowsIntegration.setVisible(false);
			windowsIntegrationLabel.setVisible(false);
		}
	}

	private int getPort() {
		if (port.getText() == null || port.getText().isEmpty()) {
			return -1;
		}
		try {
			return Integer.parseInt(port.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private File getFile(String jarPathForClass) {
		// F:\workspace\.IntelliJIdea12\config\plugins\GrepConsole\lib\GrepConsole.jar
		final File parentFile = new File(jarPathForClass).getParentFile();
		return new File(parentFile, WindowsRegistryChange.HTTP_CLIENT_JAR);
	}

	public String getJarPath() {
		final String jarPathForClass = PathUtil.getJarPathForClass(this.getClass());
		String path;
		if (isDevMode(jarPathForClass)) {
			log.info("dev mode, jarPathForClass=" + jarPathForClass);
			final File file = new File(jarPathForClass + "/../lib/" + WindowsRegistryChange.HTTP_CLIENT_JAR);
			if (!file.exists()) {
				log.error(file.getAbsolutePath() + " does not exists");
				return null;
			}
			path = file.getAbsolutePath();
		} else {
			log.info("production mode, jarPathForClass=" + jarPathForClass);
			final File file = getFile(jarPathForClass);
			if (!file.exists() || !file.getName().equals(WindowsRegistryChange.HTTP_CLIENT_JAR)) {
				log.error(jarPathForClass);
				return null;
			}
			path = file.getAbsolutePath();
		}
		return path;
	}

	private boolean isDevMode(String jarPathForClass) {
		return jarPathForClass.endsWith("classes");
	}

	public JPanel getRoot() {
		return contentPane;
	}

	public void setData(TailSettings data) {
		port.setText(data.getPort());
		listenOnPortCheckBox.setSelected(data.isEnabled());
		defaultEncoding.setText(data.getDefaultEncoding());
		autodetectEncoding.setSelected(data.isAutodetectEncoding());
		advancedTailDialog.setSelected(data.isAdvancedTailDialog());
	}

	public void getData(TailSettings data) {
		data.setPort(port.getText());
		data.setEnabled(listenOnPortCheckBox.isSelected());
		data.setDefaultEncoding(defaultEncoding.getText());
		data.setAutodetectEncoding(autodetectEncoding.isSelected());
		data.setAdvancedTailDialog(advancedTailDialog.isSelected());
	}

	public boolean isModified(TailSettings data) {
		if (port.getText() != null ? !port.getText().equals(data.getPort()) : data.getPort() != null) return true;
		if (listenOnPortCheckBox.isSelected() != data.isEnabled()) return true;
		if (defaultEncoding.getText() != null ? !defaultEncoding.getText().equals(data.getDefaultEncoding()) : data.getDefaultEncoding() != null)
			return true;
		if (autodetectEncoding.isSelected() != data.isAutodetectEncoding()) return true;
		if (advancedTailDialog.isSelected() != data.isAdvancedTailDialog()) return true;
		return false;
	}
}
