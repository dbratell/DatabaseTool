package databasetool.ui;

import databasetool.TimeTracker;

import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;

public class DataTableModel extends AbstractTableModel
{
    private ResultSet mResultSet;
    private ArrayList mLabels = new ArrayList();
    ProgressArea mProgressArea;
    private int mRowCount;
    private PreparedStatement mCurrentStatement;

    public DataTableModel(ProgressArea progressArea)
    {
        mProgressArea = progressArea;
    }

    public int getRowCount()
    {
        return mRowCount;
//        return mData.size();
    }

    public int getColumnCount()
    {
        return mLabels.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        try
        {
            mResultSet.absolute(rowIndex+1);
            return mResultSet.getString(columnIndex+1);
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
            return "<null>";
        }
    }

    public void loadTable(Connection connection, String scheme,
                          String catalogName, String tableName)
    {
        try
        {
            if (mResultSet != null)
            {
                mResultSet.close();
                mResultSet = null;
            }

            if (mCurrentStatement != null)
            {
                mCurrentStatement.close();
                mCurrentStatement = null;
            }
            mProgressArea.appendProgress("Loading from table " + tableName);
            mProgressArea.appendProgress("Setting catalog to " + catalogName);
            connection.setCatalog(catalogName);

            // First count so that we can use a progress bar
            TimeTracker time;
            mRowCount = getRowCount(connection, tableName);

            mProgressArea.appendProgress("Will load " + mRowCount + " rows");


            String sql = "SELECT * FROM [" + tableName + "]";
            mProgressArea.appendProgress("Running "+sql);
            time = new TimeTracker(sql);

            try
            {
                DatabaseMetaData metaData = connection.getMetaData();
                mProgressArea.appendProgress("IdentifierQuoteString="+metaData.getIdentifierQuoteString());
                int resultSetType = getBestResultSetType(metaData);
                int concurrency = getBestConcurrency(metaData, resultSetType);
                int holdability = getBestHoldability(metaData);

                try
                {
                    mCurrentStatement =
                            connection.prepareStatement(sql,
                                                        resultSetType,
                                                        concurrency,
                                                        holdability);
                }
                catch (UnsupportedOperationException e)
                {
                    mProgressArea.appendProgress(e);
                    try
                    {
                        mCurrentStatement =
                                connection.prepareStatement(sql,
                                                            resultSetType,
                                                            concurrency);
                    }
                    catch (UnsupportedOperationException e2)
                    {
                        mProgressArea.appendProgress(e2);
                        return;
                    }
                }

                mResultSet = mCurrentStatement.executeQuery();
                ResultSetMetaData meta = mResultSet.getMetaData();
                mLabels.clear();
                int columnCount = meta.getColumnCount();
                for (int column = 1; column <= columnCount; column++)
                {
                    mLabels.add(meta.getColumnName(column));
                }
//                mData.clear();
//                while (rs.next())
//                {
//                    ArrayList row = new ArrayList(columnCount);
//                    for (int column = 1; column <= columnCount; column++)
//                    {
//                        row.add(rs.getString(column));
//                    }
//                    mData.add(row);
//                    mStatusBar.setProgress(mData.size(), rowCount);
//                }
            }
            finally
            {
                mProgressArea.appendProgress(time.toString());
            }
            fireTableStructureChanged();
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    private int getBestResultSetType(DatabaseMetaData metaData) throws SQLException
    {
        int resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        if (!metaData.supportsResultSetType(resultSetType))
        {
            resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            if (!metaData.supportsResultSetType(resultSetType))
            {
                mProgressArea.appendProgress("Type forward only!");
                resultSetType = ResultSet.TYPE_FORWARD_ONLY;
            }
        }
        return resultSetType;
    }

    private int getBestConcurrency(DatabaseMetaData metaData,
                                   int resultSetType) throws SQLException
    {
        int concurrency = ResultSet.CONCUR_UPDATABLE;
        try
        {
            if (!metaData.supportsResultSetConcurrency(resultSetType,
                                                       concurrency))
            {
                concurrency = ResultSet.CONCUR_READ_ONLY;
            }
            else
            {
                // XXX
                // It lies
                concurrency = ResultSet.CONCUR_READ_ONLY;
            }
        }
        catch (UnsupportedOperationException e)
        {
            // Fallback
            concurrency = ResultSet.CONCUR_READ_ONLY;
        }
        return concurrency;
    }

    private int getBestHoldability(DatabaseMetaData metaData) throws SQLException
    {
        int holdability = ResultSet.HOLD_CURSORS_OVER_COMMIT;
        try
        {
            if (!metaData.supportsResultSetHoldability(holdability))
            {
                holdability = ResultSet.CLOSE_CURSORS_AT_COMMIT;
            }
        }
        catch (UnsupportedOperationException e)
        {
            // Fallback
            holdability = ResultSet.CLOSE_CURSORS_AT_COMMIT;
        }
        return holdability;
    }

    private int getRowCount(Connection connection, String tableName) throws SQLException
    {
        String countSql = "SELECT COUNT(*) FROM [" + tableName + "]";
        TimeTracker time = new TimeTracker(countSql);
        PreparedStatement ps2 = connection.prepareStatement(countSql);
        ResultSet rs2 = ps2.executeQuery();
        rs2.next();
        int rowCount = rs2.getInt(1);
        mProgressArea.appendProgress(time.toString());
        rs2.close();
        ps2.close();
        return rowCount;
    }

    public String getColumnName(int column)
    {
        return (String)mLabels.get(column);
    }
}
