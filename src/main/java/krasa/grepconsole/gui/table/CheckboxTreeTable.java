package krasa.grepconsole.gui.table;

import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.dualView.TreeTableView;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CheckboxTreeTable extends com.intellij.ui.CheckboxTreeTable {


	public CheckboxTreeTable(CheckedTreeNode root, CheckboxTree.CheckboxTreeCellRenderer renderer, ColumnInfo[] columns) {
		super(root, renderer, columns);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableCellRenderer renderer = getColumnInfo(column).getRenderer(getRowElement(row));
		final TableCellRenderer baseRenderer = renderer == null ? super.getCellRenderer(row, column) : renderer;
		return new CellRendererWrapper(baseRenderer);
	}

	public static class CellRendererWrapper extends TreeTableView.CellRendererWrapper {

		public CellRendererWrapper(@NotNull TableCellRenderer baseRenderer) {
			super(baseRenderer);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JComponent rendererComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (isSelected) {
			} else {
				final Color bg = table.getBackground();
				Object tableValueAt = table.getValueAt(row, 0);

				if (tableValueAt instanceof GrepExpressionGroupTreeNode) {
					boolean uncheckedChildren = ((GrepExpressionGroupTreeNode) tableValueAt).hasUncheckedChildren();
					if (uncheckedChildren) {
						rendererComponent.setBackground(bg.darker());
						rendererComponent.setOpaque(true);
					}
				} else if (tableValueAt instanceof CheckedTreeNode) {
					boolean checked = ((CheckedTreeNode) tableValueAt).isChecked();
					if (!checked) {
						rendererComponent.setBackground(bg.darker());
						rendererComponent.setOpaque(true);
					}
				}

			}
			return rendererComponent;
		}
	}
}
