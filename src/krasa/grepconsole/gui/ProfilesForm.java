package krasa.grepconsole.gui;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.ui.components.JBList;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.PluginState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ProfilesForm {
	private long originallySelectedProfileId;
	private JPanel profiles;
	private JButton addProfile;
	private JButton setAsDefault;
	private JButton duplicateButton;
	private JButton renameButton;
	private JButton deleteButton;
	private JBList jbList;
	private JCheckBox enableRunConfigurationProfilesCheckBox;
	private JPanel root;
	protected PluginState settings;
	private DefaultListModel profilesModel;
	private boolean showControlButtons;
	protected MainSettingsForm mainSettingsForm;

	public ProfilesForm(PluginState pluginState, long originallySelectedProfileId, boolean showControlButtons) {
		this.showControlButtons = showControlButtons;
		this.settings = pluginState;
		this.originallySelectedProfileId = originallySelectedProfileId;

		enableRunConfigurationProfilesCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateComponents();
				PluginState.getInstance().setAllowRunConfigurationChanges(enableRunConfigurationProfilesCheckBox.isSelected());
				settings.setAllowRunConfigurationChanges(enableRunConfigurationProfilesCheckBox.isSelected());
			}
		});
		jbList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				Profile selectedValue = (Profile) jbList.getSelectedValue();
				if (selectedValue != null) {
					updateMainForm(selectedValue);
				}
			}
		});
		addProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Profile profile = settings.createProfile();
				initModel();
				selectProfile(profile.getId());
			}
		});
		setAsDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.setDefault(getSelectedProfile());
				jbList.repaint();
			}
		});
		duplicateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Profile profile = settings.copyProfile(getSelectedProfile());
				initModel();
				selectProfile(profile.getId());
			}
		});
		renameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rename();
			}
		});
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});
		importFrom(pluginState);
		updateComponents();
	}

	protected void initModel() {
		enableRunConfigurationProfilesCheckBox.setSelected(settings.isAllowRunConfigurationChanges());
		profilesModel.clear();
		for (Profile profile : settings.getProfiles()) {
			profilesModel.addElement(profile);
		}

		selectProfile(originallySelectedProfileId);
	}


	private void rename() {
		Profile selectedProfile = getSelectedProfile();
		String presentableName = selectedProfile.getPresentableName();
		String s = Messages.showInputDialog(renameButton, "New profile name:", "Rename profile",
				Messages.getQuestionIcon(), presentableName, new NonEmptyInputValidator());
		if (s != null) {
			selectedProfile.setName(s);
			jbList.repaint();

//					initModel(settings);
		}
	}

	private void delete() {
		settings.delete(getSelectedProfile());
		initModel();
	}

	private KeyAdapter getKeyListener() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					delete();
				} else if (e.getKeyCode() == KeyEvent.VK_F2) {
					rename();
				}
			}
		};
	}

	private void updateComponents() {
		boolean allowRunConfigurationChanges = enableRunConfigurationProfilesCheckBox.isSelected();
		if (showControlButtons) {
//			if (!allowRunConfigurationChanges) {
//				for (Profile profile : settings.getProfiles()) {
//					if (profile.isDefaultProfile()) {
//						selectProfile(profile.getId());
//						break;
//					}
//				}
//			}
		} else {
			jbList.setEnabled(allowRunConfigurationChanges);
			renameButton.setVisible(false);
			addProfile.setVisible(false);
			setAsDefault.setVisible(false);
			duplicateButton.setVisible(false);
			renameButton.setVisible(false);
			deleteButton.setVisible(false);
		}
	}

	private void createUIComponents() {
		profilesModel = new DefaultListModel();
		jbList = new JBList(profilesModel);
		jbList.getMinimumSize();
		jbList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
														  boolean cellHasFocus) {
				final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				Profile profile = (Profile) value;
				setText(profile.getPresentablename2());
				return comp;
			}

		});
	}

	public JPanel getRootComponent() {
		return root;
	}

	public JPanel getProfiles() {
		return profiles;
	}

	public boolean isSettingsModified(PluginState state) {
		Profile selectedProfile = getSelectedProfile();
		return selectedProfile != null && originallySelectedProfileId != selectedProfile.getId()
				|| enableRunConfigurationProfilesCheckBox.isSelected() != settings.isAllowRunConfigurationChanges();
	}

	public PluginState getPluginState() {
		settings.setAllowRunConfigurationChanges(enableRunConfigurationProfilesCheckBox.isSelected());
		return settings;
	}

	public void updateMainForm(Profile profile) {
		if (mainSettingsForm != null) {
			if (profile != null) {
				mainSettingsForm.importFrom(profile);
			}
		}
	}


	public void importFrom(PluginState pluginState) {
		this.settings = pluginState;
		initModel();
		updateMainForm(getSelectedProfile());
	}

	@Nullable
	public Profile getSelectedProfile() {
		if (jbList.isEmpty()) {
			return null;
		}
		return (Profile) jbList.getSelectedValue();
	}

	public void setOriginallySelectedProfileId(long originallySelectedProfileId) {
		this.originallySelectedProfileId = originallySelectedProfileId;
		selectProfile(originallySelectedProfileId);
	}

	public void selectProfile(long selectedProfileId) {
		for (Profile profile : settings.getProfiles()) {
			if (profile.getId() == selectedProfileId) {
				jbList.setSelectedValue(profile, true);
			}
		}
		if (getSelectedProfile() == null) {
			Profile defaultProfile = settings.getDefaultProfile();
			jbList.setSelectedValue(defaultProfile, true);
		}
		updateMainForm(getSelectedProfile());
	}


}
