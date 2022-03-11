package krasa.grepconsole.gui.table.column;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import krasa.grepconsole.gui.table.ModelUtils;
import krasa.grepconsole.model.GrepExpressionGroup;

import org.jetbrains.annotations.Nullable;

import com.intellij.util.ui.ColumnInfo;

/**
 * @author Vojtech Krasa
 */
public class GroupNameAdapter extends FolderColumnInfoWrapper {

	private ColumnInfo groupColumnInfo;

	public GroupNameAdapter(ColumnInfo columnInfo) {
		super(columnInfo);
		groupColumnInfo = new JavaBeanColumnInfo<GrepExpressionGroup, String>("name", "name");
	}

	@Override
	public boolean isCellEditable(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return groupColumnInfo.isCellEditable(ModelUtils.unWrap(o));
		}
		return columnInfo.isCellEditable(ModelUtils.unWrap(o));
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return groupColumnInfo.getRenderer(o1);
		}
		return columnInfo.getRenderer(o1);
	}

	@Override
	public void setValue(Object o, Object value) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			groupColumnInfo.setValue(ModelUtils.unWrap(o), value);
		} else {
			columnInfo.setValue(ModelUtils.unWrap(o), value);
		}
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return groupColumnInfo.getEditor(o1);
		}
		return columnInfo.getEditor(o1);
	}

	@Nullable
	@Override
	public Object valueOf(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return groupColumnInfo.valueOf(o1);
		}
		return columnInfo.valueOf(o1);
	}
}
