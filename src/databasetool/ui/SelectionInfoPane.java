package databasetool.ui;

import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import java.sql.Connection;
import java.sql.SQLException;

public class SelectionInfoPane extends JTabbedPane
{
    private DataTable mDataTable;
    private JTextComponent mInfoArea;
    private MetaTable mMetaTable;
    private ProgressArea mProgressArea;

    public SelectionInfoPane(ProgressArea progressArea, StatusBar statusBar)
    {
        mProgressArea = progressArea;

        mDataTable = new DataTable(progressArea);
        addTab("Data", new JScrollPane(mDataTable));

        mMetaTable = new MetaTable(progressArea, statusBar);
        addTab("Columns", new JScrollPane(mMetaTable));

        mInfoArea = new JEditorPane("text/html", "");
        mInfoArea.setEditable(false);
        addTab("Meta", new JScrollPane(mInfoArea));
    }

    public void displayTable(Connection connection, String scheme,
                             String catalogName, String tableName)
    {
        mDataTable.displayTable(scheme, catalogName, tableName);
        mMetaTable.displayTable(connection, scheme, catalogName, tableName);
        try
        {
            mInfoArea.setText("<html><head><title></title></head><body>" +
                              "<b>Catalog:</b> "+toHTML(catalogName) + "<br>\n"+
                              "<b>Scheme:</b> "+toHTML(scheme)+"<br>\n"+
                              "<b>Table:</b> "+toHTML(tableName)+"<br>\n"+
                              "<b>Auto commit:</b> "+connection.getAutoCommit()+"<br>\n"+
                              "<b>Holdability:</b> "+getHTMLHoldability(connection)+"<br>\n"+
                              "<b>Isolation:</b> "+connection.getTransactionIsolation()+"<br>\n"+
                              "<b>Read only:</b> "+connection.isReadOnly()+"<br>\n"+
                              "</body></html>");
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    private String getHTMLHoldability(Connection connection)
            throws SQLException
    {
        try
        {
            return String.valueOf(connection.getHoldability());
        }
        catch (UnsupportedOperationException uoe)
        {
            return "<i>Not supported</i>";
        }
    }

    private String toHTML(String string)
    {
        if (string == null)
        {
            return "<i>null</i>";
        }
        // XXX quote
        return string;
    }
}
