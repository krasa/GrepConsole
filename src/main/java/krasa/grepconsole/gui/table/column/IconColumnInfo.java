package krasa.grepconsole.gui.table.column;

import com.intellij.ui.ClickListener;
import com.intellij.util.ui.table.IconTableCellRenderer;
import krasa.grepconsole.model.GrepExpressionItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class IconColumnInfo extends JavaBeanColumnInfo<GrepExpressionItem, GrepExpressionItem> {

	private IconTableCellRenderer<GrepExpressionItem> myRenderer;

	public IconColumnInfo(@NotNull String title) {
		super(title, null);
		myRenderer = new IconTableCellRenderer<GrepExpressionItem>() {
			MyListener myListener;

			protected boolean isCenterAlignment() {
				return false;
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
				if (myListener == null) {
					myListener = new MyListener(table, title);
					myListener.installOn(table);
				}

				super.getTableCellRendererComponent(table, value, selected, focus, row, column);
				this.setText("");
				return this;
			}

			@Nullable
			@Override
			protected Icon getIcon(@NotNull GrepExpressionItem value, JTable table, int row) {
				return IconColumnInfo.this.getIcon(value);
			}
		};
	}


	@Override
	public boolean isCellEditable(GrepExpressionItem grepExpressionItem) {
		return false;
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(GrepExpressionItem aVoid) {
		return myRenderer;
	}

	private class MyListener extends ClickListener {

		private final JTable myTable;
		private final String myColumnName;

		public MyListener(JTable table, String columnName) {
			myTable = table;
			myColumnName = columnName;
		}

		@Override
		public boolean onClick(@NotNull MouseEvent e, int clickCount) {
			if (e.getButton() == 1 && !e.isPopupTrigger()) {
				if (myTable.getRowCount() > 0) {
					final int row = myTable.rowAtPoint(e.getPoint());
					final int col = myTable.columnAtPoint(e.getPoint());

					String columnName = myTable.getColumnName(col);
					if (columnName.equals(myColumnName)) {
						Object value = myTable.getModel().getValueAt(row, col);
						if (value instanceof GrepExpressionItem) {

							execute((GrepExpressionItem) value);

							myTable.repaint(myTable.getCellRect(row, col, false));
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	protected abstract Icon getIcon(@NotNull GrepExpressionItem value);

	protected abstract void execute(GrepExpressionItem value);
}
