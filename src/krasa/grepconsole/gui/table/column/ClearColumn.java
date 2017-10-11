package krasa.grepconsole.gui.table.column;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.gui.ProfileDetail;
import krasa.grepconsole.model.GrepExpressionItem;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ClearColumn extends ButtonColumnInfo<GrepExpressionItem> {
	public static final Icon DISABLED = AllIcons.Actions.GC;
	public static final Icon ENABLED = IconLoader.getIcon("clearEnabled.png", ClearColumn.class);
	private final ProfileDetail profileDetail;

	public ClearColumn(String title, ProfileDetail profileDetail) {
		super(title);
		this.profileDetail = profileDetail;
	}


	@Override
	void onButtonClicked(GrepExpressionItem item) {
		item.setClearConsole(!item.isClearConsole());
		if (item.isClearConsole()) {
			profileDetail.profile.setEnabledInputFiltering(true);
			profileDetail.setData(profileDetail.profile);
		}
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(GrepExpressionItem o) {
		return new ButtonEditor<GrepExpressionItem>(new JCheckBox()) {
			@Override
			protected void setStyle(GrepExpressionItem grepExpressionItem) {
				if (grepExpressionItem.isClearConsole()) {
					button.setIcon(ENABLED);
				} else {
					button.setIcon(DISABLED);
				}
			}

			@Override
			protected void onButtonClicked(GrepExpressionItem item) {
				ClearColumn.this.onButtonClicked(item);
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
				if (grepExpressionItem.isClearConsole()) {
					setIcon(ENABLED);
				} else {
					setIcon(DISABLED);
				}
			}
		};
	}
}
