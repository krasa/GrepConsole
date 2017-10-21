package krasa.grepconsole.gui;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.ui.components.JBList;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.plugin.PluginState;
import krasa.grepconsole.tail.TailIntegrationForm;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CompositeSettingsDialog {
	private PluginState settings;
	private JPanel profiles;
	private JBList jbList;
	private JButton addProfile;
	private JButton setAsDefault;
	private JButton duplicateButton;
	private JButton renameButton;
	private JPanel profileDetail;
	private JPanel root;
	private JButton fileTailSettings;
	private JButton deleteButton;
	private ProfileDetail profileDetailComponent;
	private DefaultListModel profilesModel;
	private long originallySelectedProfileId;


	public CompositeSettingsDialog(MyConfigurable myConfigurable, PluginState settings, long originallySelectedProfileId) {
		this(myConfigurable, settings, SettingsContext.NONE, originallySelectedProfileId);
	}

	public CompositeSettingsDialog(MyConfigurable myConfigurable, PluginState settingsForCloning, SettingsContext settingsContext, long originallySelectedProfileId) {
		this.settings = settingsForCloning.clone();
		this.originallySelectedProfileId = originallySelectedProfileId;

		profileDetailComponent = new ProfileDetail(myConfigurable, settingsContext);
		profileDetail.add(profileDetailComponent.getRootComponent());
//		Dimension minimumSize = new Dimension(0, 0);
//		profiles.setMinimumSize(minimumSize);

		jbList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				Profile selectedValue = (Profile) jbList.getSelectedValue();
				if (selectedValue != null) {
					profileDetailComponent.importFrom(selectedValue);
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
				profileDetailComponent.foldingsEnabled(true);
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
		fileTailSettings.addActionListener(new FileTailSettingsActionListener());
		jbList.addKeyListener(getKeyListener());
		initModel();
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
	  
	protected void initModel() {
		profilesModel.clear();
		for (Profile profile : settings.getProfiles()) {
			profilesModel.addElement(profile);
		}


		selectProfile(originallySelectedProfileId);
	}

	private void createUIComponents() {
		profileDetail = new JPanel(new GridLayout());
		profilesModel = new DefaultListModel();
		jbList = new JBList(profilesModel);
		jbList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
														  boolean cellHasFocus) {
				final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				Profile profile = (Profile) value;
				String name = profile.getPresentableName();
				if (!"default".equals(name) && profile.isDefaultProfile()) {
					name += " (default)";
				}
				setText(name);
				return comp;
			}
		});
	}

	public JPanel getRootComponent() {
		return root;
	}

	public boolean isSettingsModified(PluginState state) {
		Profile selectedProfile = getSelectedProfile();
		profileDetailComponent.getData(selectedProfile);
		return originallySelectedProfileId != selectedProfile.getId() || !this.settings.equals(state);
	}

	public PluginState getSettings() {
		profileDetailComponent.getData(getSelectedProfile());
		return settings;
	}

	public void importFrom(PluginState pluginState) {
		this.settings = pluginState.clone();
		initModel();
		profileDetailComponent.importFrom(getSelectedProfile());
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
		profileDetailComponent.importFrom(getSelectedProfile());
	}

	private class FileTailSettingsActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final TailIntegrationForm form = new TailIntegrationForm();
			form.setData(settings.getTailSettings());

			DialogBuilder builder = new DialogBuilder(CompositeSettingsDialog.this.getRootComponent());
			builder.setCenterPanel(form.getRoot());
			builder.setDimensionServiceKey("GrepConsoleTailFileDialog");
			builder.setTitle("Tail File settings");
			builder.removeAllActions();
			builder.addOkAction();
			builder.addCancelAction();

			boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
			if (isOk) {
				form.getData(settings.getTailSettings());
				GrepConsoleApplicationComponent.getInstance().getState().setTailSettings(settings.getTailSettings());
				form.rebind(settings.getTailSettings());
			}
		}
	}
}
