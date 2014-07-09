package krasa.grepconsole.gui.table;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.ListTableModel;

public class TableRowTransferHandler extends TransferHandler {
	private static final Logger log = Logger.getInstance(TableRowTransferHandler.class.getName());

	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
			DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");
	private JTable table = null;

	public TableRowTransferHandler(JTable table) {
		this.table = table;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		assert (c == table);
		return new DataHandler(table.getSelectedRow(), localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
		table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		return b;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		JTable target = (JTable) info.getComponent();
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int index = dl.getRow();
		int max = table.getModel().getRowCount();
		if (index < 0 || index > max)
			index = max;
		target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		try {
			Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
			if (rowFrom != -1 && rowFrom != index) {
				final ListTableModel model = (ListTableModel) table.getModel();
				model.insertRow(index, model.getRowValue(rowFrom));
				if (rowFrom > index) {
					rowFrom++;
				} else {
					index--;
				}
				model.removeRow(rowFrom);

				target.getSelectionModel().addSelectionInterval(index, index);
				return true;
			}
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		if (act == TransferHandler.MOVE) {
			table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

}
