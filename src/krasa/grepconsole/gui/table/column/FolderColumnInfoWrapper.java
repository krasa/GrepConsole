package krasa.grepconsole.gui.table.column;

import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import krasa.grepconsole.gui.table.ModelUtils;
import krasa.grepconsole.model.GrepExpressionGroup;

import org.jetbrains.annotations.Nullable;

import com.intellij.util.ui.ColumnInfo;

/**
 * @author Vojtech Krasa
 */
public class FolderColumnInfoWrapper extends ColumnInfo {

	protected ColumnInfo columnInfo;

	public FolderColumnInfoWrapper(ColumnInfo columnInfo) {
		super(columnInfo.getName());
		this.columnInfo = columnInfo;
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return null;
		}
		return columnInfo.getRenderer(o1);
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return null;
		}
		return columnInfo.getEditor(o1);
	}

	@Nullable
	@Override
	public Object valueOf(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return null;
		}
		return columnInfo.valueOf(o1);
	}

	@Override
	@Nullable
	public Comparator getComparator() {
		return columnInfo.getComparator();
	}

	@Override
	public String getName() {
		return columnInfo.getName();
	}

	@Override
	public void setName(String s) {
		columnInfo.setName(s);
	}

	@Override
	public Class getColumnClass() {
		return columnInfo.getColumnClass();
	}

	@Override
	public boolean isCellEditable(Object o) {
		Object o1 = ModelUtils.unWrap(o);
		if (o1 instanceof GrepExpressionGroup) {
			return false;
		}
		return columnInfo.isCellEditable(ModelUtils.unWrap(o));
	}

	@Override
	public void setValue(Object o, Object value) {
		columnInfo.setValue(ModelUtils.unWrap(o), value);
	}

	@Override
	public TableCellRenderer getCustomizedRenderer(Object o, TableCellRenderer renderer) {
		return columnInfo.getCustomizedRenderer(ModelUtils.unWrap(o), renderer);
	}

	@Override
	@Nullable
	public String getMaxStringValue() {
		return columnInfo.getMaxStringValue();
	}

	@Override
	@Nullable
	public String getPreferredStringValue() {
		return columnInfo.getPreferredStringValue();
	}

	@Override
	public int getAdditionalWidth() {
		return columnInfo.getAdditionalWidth();
	}

	@Override
	public int getWidth(JTable table) {
		return columnInfo.getWidth(table);
	}

	@Override
	@Nullable
	public String getTooltipText() {
		return columnInfo.getTooltipText();
	}

	@Override
	@Nullable
	public Icon getIcon() {
		return columnInfo.getIcon();
	}

	@Override
	public String toString() {
		return columnInfo.toString();
	}
}
