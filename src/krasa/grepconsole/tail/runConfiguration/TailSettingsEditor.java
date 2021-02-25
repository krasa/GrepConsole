package krasa.grepconsole.tail.runConfiguration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import krasa.grepconsole.action.TailFileInConsoleAction;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TailSettingsEditor extends SettingsEditor<TailRunConfiguration> {
	private final Project project;
	private JPanel myPanel;
	private JCheckBox autodetectEncodingCheckBox;
	private JTextField encoding;
	private JPanel paths;

	public TailSettingsEditor(Project project) {
		this.project = project;
		paths.setLayout(new BoxLayout(paths, BoxLayout.Y_AXIS));
		paths.setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	@Override
	protected void resetEditorFrom(TailRunConfiguration configuration) {
		setData(configuration.mySettings);
		init(configuration.mySettings.getPaths());
	}

	@Override
	protected void applyEditorTo(TailRunConfiguration configuration) throws ConfigurationException {
		configuration.mySettings.setPaths(getPaths());
		getData(configuration.mySettings);
	}

	private List<String> getPaths() {
		ArrayList<String> strings = new ArrayList<>();
		Component[] components = paths.getComponents();
		for (Component component : components) {
			LabeledComponent<TextFieldWithBrowseButton> field = (LabeledComponent<TextFieldWithBrowseButton>) component;
			String text = field.getComponent().getText();
			if (StringUtils.isNotBlank(text)) {
				strings.add(text);
			}
		}
		return strings;
	}

	@NotNull
	@Override
	protected JComponent createEditor() {
		return myPanel;
	}

	private void init(List<String> files) {
		paths.removeAll();
		for (String file : files) {
			addField(file, false);
		}
		addField("", true);
	}

	private void addField(String file, boolean last) {
		LabeledComponent<TextFieldWithBrowseButton> myPathField = new LabeledComponent<>();
		AtomicBoolean lastField = new AtomicBoolean(last);
		myPathField.setComponent(new TextFieldWithBrowseButton(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {

				PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
				VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();

				String value = propertiesComponent.getValue(TailFileInConsoleAction.TAIL_FILE_IN_CONSOLE_ACTION_LAST_FILE);

				VirtualFile lastFile = null;
				String text = myPathField.getComponent().getText();
				if (StringUtils.isNotBlank(text)) {
					lastFile = virtualFileManager.findFileByUrl("file://" + text);
				}
				if (lastFile == null && value != null) {
					lastFile = virtualFileManager.findFileByUrl(value);
				}

				final VirtualFile file =
						FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(), myPathField.getComponent(), project, lastFile);
				if (file != null) {
					propertiesComponent.setValue(TailFileInConsoleAction.TAIL_FILE_IN_CONSOLE_ACTION_LAST_FILE, file.getUrl());
					myPathField.getComponent().setText(FileUtil.toSystemDependentName(file.getPath()));
					if (lastField.get()) {
						addField("", true);
						lastField.set(false);
					}
				}
			}
		}));

		myPathField.getComponent().getTextField().getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			private void update() {
				if (lastField.get()) {
					addField("", true);
					lastField.set(false);
				}
			}
		});
		myPathField.getComponent().setText(file);
		paths.add(myPathField);
	}

	public void setData(TailRunConfigurationSettings data) {
		autodetectEncodingCheckBox.setSelected(data.isAutodetectEncoding());
		encoding.setText(data.getEncoding());
	}

	public void getData(TailRunConfigurationSettings data) {
		data.setAutodetectEncoding(autodetectEncodingCheckBox.isSelected());
		data.setEncoding(encoding.getText());
	}

	public boolean isModified(TailRunConfigurationSettings data) {
		if (autodetectEncodingCheckBox.isSelected() != data.isAutodetectEncoding()) return true;
		if (encoding.getText() != null ? !encoding.getText().equals(data.getEncoding()) : data.getEncoding() != null)
			return true;
		return false;
	}
}