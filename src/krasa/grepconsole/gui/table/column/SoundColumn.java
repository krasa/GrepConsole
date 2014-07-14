package krasa.grepconsole.gui.table.column;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.gui.SoundSettingsForm;
import krasa.grepconsole.model.GrepExpressionItem;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;

/**
 * @author Vojtech Krasa
 */
public class SoundColumn extends ButtonColumnInfo<GrepExpressionItem> {
	public static final Icon SOUND_OFF = IconLoader.getIcon("soundOff.gif", SoundColumn.class);
	public static final Icon SOUND_ON = IconLoader.getIcon("soundOn.gif", SoundColumn.class);
	private final SettingsDialog settingsDialog;
	protected SoundSettingsForm soundSettingsForm;

	public SoundColumn(String sound, SettingsDialog settingsDialog) {
		super(sound);
		this.settingsDialog = settingsDialog;
		soundSettingsForm = new SoundSettingsForm();
	}

	private boolean showDialog(GrepExpressionItem item) {
		DialogBuilder builder = new DialogBuilder(settingsDialog.getRootComponent());
		builder.setCenterPanel(soundSettingsForm.getRoot());
		builder.setDimensionServiceKey("GrepConsoleSound");
		builder.setTitle("Sound settings");
		builder.removeAllActions();
		builder.addOkAction();
		builder.addCancelAction();

		soundSettingsForm.setData(item.getSound());
		boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
		if (isOk) {
			soundSettingsForm.getData(item.getSound());
		}
		return isOk;
	}

	@Override
	void onButtonClicked(GrepExpressionItem item) {
		showDialog(item);
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(GrepExpressionItem o) {
		return new ButtonEditor<GrepExpressionItem>(new JCheckBox()) {
			@Override
			protected void setStyle(GrepExpressionItem grepExpressionItem) {
				if (grepExpressionItem.getSound().isEnabled()) {
					button.setIcon(SOUND_ON);
				} else {
					button.setIcon(SOUND_OFF);
				}
			}

			@Override
			protected void onButtonClicked(GrepExpressionItem item) {
				SoundColumn.this.onButtonClicked(item);
			}
		};
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(GrepExpressionItem aVoid) {
		return new ButtonRenderer() {
			@Override
			protected void setStyle(Object value) {
				GrepExpressionItem grepExpressionItem = (GrepExpressionItem) value;
				if (grepExpressionItem.getSound().isEnabled()) {
					setIcon(SOUND_ON);
				} else {
					setIcon(SOUND_OFF);
				}
			}
		};
	}
}
