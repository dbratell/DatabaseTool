package databasetool.ui;

import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.sql.Connection;
import java.awt.BorderLayout;

public class DataTable extends JPanel
{
    private JTable mTable = new JTable();
    private Connection mConnection;

    public DataTable(ProgressArea progressArea)
    {
        super(new BorderLayout());
        DataTableModel dataTableModel = new DataTableModel(progressArea);
        mTable.setModel(dataTableModel);
        JScrollPane scrollPane = new JScrollPane(mTable);
        scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(scrollPane);
    }

    public void displayTable(String scheme,
                             String catalogName, String tableName)
    {
        DataTableModel dataModel = getDataModel();
        dataModel.loadTable(mConnection, scheme, catalogName, tableName);
    }

    private DataTableModel getDataModel()
    {
        return (DataTableModel)(mTable.getModel());
    }

    public void setConnection(Connection connection)
    {
        mConnection = connection;
    }
}
