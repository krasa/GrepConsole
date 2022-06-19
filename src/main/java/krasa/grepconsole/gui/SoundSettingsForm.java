package krasa.grepconsole.gui;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import krasa.grepconsole.model.Sound;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Vojtech Krasa
 */
public class SoundSettingsForm {
	private JCheckBox enabledCheckBox;
	private TextFieldWithBrowseButton path;
	private JPanel root;
	private JButton test;
	private JLabel supported;

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
		supported.setText("Supported formats: " + Arrays.stream(AudioSystem.getAudioFileTypes()).map(AudioFileFormat.Type::getExtension).collect(Collectors.joining(", ")));
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
