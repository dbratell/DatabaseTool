package databasetool.ui;

import javax.swing.JTable;
import java.sql.Connection;

public class MetaTable extends JTable
{
    public MetaTable(ProgressArea progressArea, StatusBar statusBar)
    {
        MetaTableModel metaTableModel = new MetaTableModel(progressArea,
                                                           statusBar);
        setModel(metaTableModel);
    }

    public void displayTable(Connection connection, String scheme,
                             String catalogName, String tableName)
    {
        MetaTableModel dataModel = getMetaModel();
        dataModel.loadTable(connection, scheme, catalogName, tableName);
    }

    private MetaTableModel getMetaModel()
    {
        return (MetaTableModel)getModel();
    }
}
