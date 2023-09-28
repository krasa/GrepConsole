package krasa.grepconsole.tail.runConfiguration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import krasa.grepconsole.action.TailFileInConsoleAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TailSettingsEditor extends SettingsEditor<TailRunConfiguration> {
	private final Project project;
	private JPanel myPanel;
	private JCheckBox autodetectEncodingCheckBox;
	private JTextField encoding;
	private JPanel paths;
	private JCheckBox selectNewestMatchingFile;
	private JButton clearButton;

	public TailSettingsEditor(Project project) {
		this.project = project;
		paths.setLayout(new BoxLayout(paths, BoxLayout.Y_AXIS));
		paths.setAlignmentX(Component.LEFT_ALIGNMENT);
		clearButton.addActionListener(e -> {
			clear();
		});
	}

	private void clear() {
		init(Collections.emptyList());
	}

	@Override
	protected void resetEditorFrom(TailRunConfiguration configuration) {
		TailRunConfigurationSettings settings = configuration.mySettings;
		resetEditorFrom(settings);
	}

	public void resetEditorFrom(TailRunConfigurationSettings settings) {
		setData(settings);
		init(settings.getPaths());
	}

	@Override
	protected void applyEditorTo(TailRunConfiguration configuration) throws ConfigurationException {
		applyEditorTo(configuration.mySettings);
	}

	public void applyEditorTo(TailRunConfigurationSettings settings) {
		settings.setPaths(getPaths());
		getData(settings);
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
			addField(file, -1);
		}
		addField("", -1);
		paths.revalidate();
		focusLastField();
	}

	public JComponent getPreferredFocusComponent() {
		Component component = paths.getComponent(paths.getComponentCount() - 1);
		if (component instanceof LabeledComponent) {
			LabeledComponent<?> component2 = (LabeledComponent<?>) component;
			return component2.getComponent();
		}
		return null;
	}

	private void focusLastField() {
		JComponent preferredFocusComponent = getPreferredFocusComponent();
		if (preferredFocusComponent != null) {
			preferredFocusComponent.requestFocus();
		}
	}

	private LabeledComponent<TextFieldWithBrowseButton> addField(String file, int index) {
		LabeledComponent<TextFieldWithBrowseButton> myPathField = new LabeledComponent<>();
		myPathField.setComponent(new TextFieldWithBrowseButton(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				browse(myPathField);
			}
		}));

		myPathField.getComponent().setText(file);
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
				addFieldIfNeeded();
			}
		});
		if (index > -1) {
			paths.add(myPathField, index + 1);
		} else {
			paths.add(myPathField);
		}
		return myPathField;
	}

	private void addFieldIfNeeded() {
		Component[] components = paths.getComponents();
		if (components.length > 0) {
			LabeledComponent<TextFieldWithBrowseButton> component = (LabeledComponent<TextFieldWithBrowseButton>) components[components.length - 1];
			if (StringUtils.isNotBlank(component.getComponent().getText())) {
				addField("", -1);
				paths.revalidate();
			}
		}
	}

	private void browse(LabeledComponent<TextFieldWithBrowseButton> myPathField) {
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

		VirtualFile[] virtualFiles = FileChooser.chooseFiles(new FileChooserDescriptor(true, true, true, true, false, true), myPathField.getComponent(), project, lastFile);
		int index = -1;
		for (int i = 0; i < virtualFiles.length; i++) {
			VirtualFile file = virtualFiles[i];
			String path = FileUtil.toSystemDependentName(file.getPath());
			if (i == 0) {
				propertiesComponent.setValue(TailFileInConsoleAction.TAIL_FILE_IN_CONSOLE_ACTION_LAST_FILE, file.getUrl());
				myPathField.getComponent().setText(path);
				index = myPathField.getParent().getComponentZOrder(myPathField);
			} else {
				LabeledComponent<TextFieldWithBrowseButton> field = addField(path, index);
				index = myPathField.getParent().getComponentZOrder(field);
			}
		}
		addFieldIfNeeded();
		paths.revalidate();
	}

	public void setData(TailRunConfigurationSettings data) {
		autodetectEncodingCheckBox.setSelected(data.isAutodetectEncoding());
		encoding.setText(data.getEncoding());
		selectNewestMatchingFile.setSelected(data.isSelectNewestMatchingFile());
	}

	public void getData(TailRunConfigurationSettings data) {
		data.setAutodetectEncoding(autodetectEncodingCheckBox.isSelected());
		data.setEncoding(encoding.getText());
		data.setSelectNewestMatchingFile(selectNewestMatchingFile.isSelected());
	}

	public boolean isModified(TailRunConfigurationSettings data) {
		if (autodetectEncodingCheckBox.isSelected() != data.isAutodetectEncoding()) return true;
		if (encoding.getText() != null ? !encoding.getText().equals(data.getEncoding()) : data.getEncoding() != null)
			return true;
		if (selectNewestMatchingFile.isSelected() != data.isSelectNewestMatchingFile()) return true;
		return false;
	}


}