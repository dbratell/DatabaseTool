package databasetool.ui;

import javax.swing.JTable;
import java.sql.Connection;

public class DataTable extends JTable
{
    public DataTable(ProgressArea progressArea, StatusBar statusBar)
    {
        DataTableModel dataTableModel = new DataTableModel(progressArea,
                                                           statusBar);
        setModel(dataTableModel);
    }

    public void displayTable(Connection connection, String scheme,
                             String catalogName, String tableName)
    {
        DataTableModel dataModel = getDataModel();
        dataModel.loadTable(connection, scheme, catalogName, tableName);
    }

    private DataTableModel getDataModel()
    {
        return (DataTableModel)getModel();
    }
}
