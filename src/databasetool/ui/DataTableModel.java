package databasetool.ui;

import databasetool.TimeTracker;

import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class DataTableModel extends AbstractTableModel
{
    private ArrayList mData = new ArrayList();
    private ArrayList mLabels = new ArrayList();
    ProgressArea mProgressArea;
    private StatusBar mStatusBar;

    public DataTableModel(ProgressArea progressArea, StatusBar statusBar)
    {
        mProgressArea = progressArea;
        mStatusBar = statusBar;
    }

    public int getRowCount()
    {
        return mData.size();
    }

    public int getColumnCount()
    {
        return mLabels.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return ((ArrayList)mData.get(rowIndex)).get(columnIndex);
    }

    public void loadTable(Connection connection, String scheme,
                          String catalogName, String tableName)
    {
        try
        {
            mProgressArea.appendProgress("Loading from table " + tableName);
            String oldCatalog = connection.getCatalog();
            mProgressArea.appendProgress("Setting catalog to " + catalogName);
            connection.setCatalog(catalogName);

            // First count so that we can use a progress bar
            String countSql = "SELECT COUNT(*) FROM ["+tableName+"]";
            TimeTracker time = new TimeTracker(countSql);
            PreparedStatement ps = connection.prepareStatement(countSql);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int rowCount = rs.getInt(1);
            mProgressArea.appendProgress(time.toString());
            rs.close();
            ps.close();

            mProgressArea.appendProgress("Will load "+rowCount+" rows");


//            ProgressMonitor progress = new ProgressMonitor(mProgressArea,
//                                                           "Loading table",
//                                                           null, 0, rowCount);
            mStatusBar.enableProgress(true);
            String sql = "SELECT * FROM ["+tableName+"]";
            time = new TimeTracker(sql);
            try
            {
                ps = connection.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                mLabels.clear();
                int columnCount = meta.getColumnCount();
                for (int column = 1; column <= columnCount; column++)
                {
                    mLabels.add(meta.getColumnName(column));
                }
                mData.clear();
                while (rs.next())
                {
                    ArrayList row = new ArrayList(columnCount);
                    for (int column = 1; column <= columnCount; column++)
                    {
                        row.add(rs.getString(column));
                    }
                    mData.add(row);
                    mStatusBar.setProgress(mData.size(), rowCount);
                }
            }
            finally
            {
                mProgressArea.appendProgress(time.toString());
                mStatusBar.setEnabled(false);
            }
            rs.close();
            ps.close();
            mProgressArea.appendProgress("Resetting catalog to " + oldCatalog);
            if (oldCatalog != null)
            {
                connection.setCatalog(oldCatalog);
            }
            fireTableStructureChanged();
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    public String getColumnName(int column)
    {
        return (String)mLabels.get(column);
    }
}
