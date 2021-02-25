package krasa.grepconsole.tail.runConfiguration;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TailSettingsEditor extends SettingsEditor<TailRunConfiguration> {
	private final Project project;
	private JPanel myPanel;
	private LabeledComponent<TextFieldWithBrowseButton> myPathField;
	private JCheckBox autodetectEncodingCheckBox;
	private JTextField encoding;

	public TailSettingsEditor(Project project) {
		this.project = project;
	}

	@Override
	protected void resetEditorFrom(TailRunConfiguration configuration) {
		myPathField.getComponent().setText(configuration.mySettings.getPath());
		setData(configuration.mySettings);
	}

	@Override
	protected void applyEditorTo(TailRunConfiguration configuration) throws ConfigurationException {
		configuration.mySettings.setPath(myPathField.getComponent().getText());
		getData(configuration.mySettings);
	}

	@NotNull
	@Override
	protected JComponent createEditor() {
		return myPanel;
	}

	private void createUIComponents() {
		myPathField = new LabeledComponent<>();
		myPathField.setText("Path");
		myPathField.setLabelLocation(BorderLayout.WEST);

		myPathField.setComponent(new TextFieldWithBrowseButton(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				final VirtualFile file =
						FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(), myPathField.getComponent(), project, null);
				if (file != null) {
					myPathField.getComponent().setText(file != null ? FileUtil.toSystemDependentName(file.getPath()) : "");
				}
			}

		}));
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