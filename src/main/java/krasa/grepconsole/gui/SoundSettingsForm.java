package krasa.grepconsole.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import krasa.grepconsole.model.Sound;

/**
 * @author Vojtech Krasa
 */
public class SoundSettingsForm {
	private JCheckBox enabledCheckBox;
	private TextFieldWithBrowseButton path;
	private JPanel root;
	private JButton test;

	public SoundSettingsForm() {
		path.addBrowseFolderListener("Select file", "", null,
				FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
		test.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Sound data = new Sound();
				data.setEnabled(true);
				data.setPath(path.getText());
				data.play();
			}
		});
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
