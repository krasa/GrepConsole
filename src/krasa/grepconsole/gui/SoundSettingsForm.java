package krasa.grepconsole.gui;

import javax.swing.*;

import krasa.grepconsole.model.Sound;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

/**
 * @author Vojtech Krasa
 */
public class SoundSettingsForm {
	private JCheckBox enabledCheckBox;
	private TextFieldWithBrowseButton path;
	private JPanel root;

	public SoundSettingsForm() {
		path.addBrowseFolderListener("Select file", "", null,
				FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
	}

	public JPanel getRoot() {
		return root;
	}

	public void setData(Sound data) {
		enabledCheckBox.setSelected(data.isEnabled());
		path.setText(data.getPath());
	}

	public void getData(Sound data) {
		data.setEnabled(enabledCheckBox.isSelected());
		data.setPath(path.getText());
	}

	public boolean isModified(Sound data) {
		if (enabledCheckBox.isSelected() != data.isEnabled())
			return true;
		if (path.getText() != null ? !path.getText().equals(data.getPath()) : data.getPath() != null)
			return true;
		return false;
	}

}
