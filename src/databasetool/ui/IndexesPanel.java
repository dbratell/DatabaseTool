package databasetool.ui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.awt.BorderLayout;

public class IndexesPanel extends JPanel
{
    private JTable mIndexesTable;
    private ProgressArea mProgress;

    public IndexesPanel(ProgressArea progress)
    {
        setLayout(new BorderLayout());
        mProgress = progress;
        mIndexesTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(mIndexesTable);
        scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mIndexesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(scrollPane, BorderLayout.CENTER);
//        add(mIndexesTable);
//        setBorder(BorderFactory.createLineBorder(Color.RED));
    }

    public void loadIndexes(Connection connection, String catalogName,
                            String schemeName,
                                String tableName)
    {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(IndexInfo.getTableHeader());
        try
        {
            mProgress.appendProgress("Loading meta from table " + tableName);
            String oldCatalog = connection.getCatalog();
            mProgress.appendProgress("Setting catalog to " + catalogName);
            connection.setCatalog(catalogName);

            DatabaseMetaData meta = connection.getMetaData();
            ResultSet indexRs = null;
            try
            {
                indexRs = meta.getIndexInfo(catalogName, schemeName,
                                             tableName,
                                                     false, false);
                while(indexRs.next())
                {
                    IndexInfo column = new IndexInfo(indexRs);
                    model.addRow(column.getAsTableRow());
                }
            }
            finally
            {
                if (indexRs != null)
                {
                    indexRs.close();
                }
            }

            mProgress.appendProgress("Resetting catalog to " + oldCatalog);
            if (oldCatalog != null)
            {
                connection.setCatalog(oldCatalog);
            }
        }
        catch (SQLException e)
        {
            mProgress.appendProgress(e);
        }
        mIndexesTable.setModel(model);
        mIndexesTable.invalidate();
        validate();
    }

    private static String indexTypeToString(short type)
    {
        switch (type)
        {
            case DatabaseMetaData.tableIndexClustered:
                return "clustered";
            case DatabaseMetaData.tableIndexHashed:
                return "hashed";
            case DatabaseMetaData.tableIndexOther:
                return "other";
            case DatabaseMetaData.tableIndexStatistic:
                return "statistic";
            default:
                return "unknown (" + type + ")";
        }
    }

    private static class IndexInfo
    {
        public String text;

        public IndexInfo(ResultSet rs) throws SQLException
        {
            short type = rs.getShort(7);
            String column = rs.getString(9);

            if (type == DatabaseMetaData.tableIndexStatistic)
            {
                int cardinality = rs.getInt(11);
                int pages = rs.getInt(12);
                if (column != null)
                {
                    text = "Statistics for column "+column+
                           ": cardinality: "+cardinality+
                       ", pages: "+pages;
                }
                else
                {
                    text = "Statistics: cardinality: "+cardinality+
                       ", pages: "+pages;
                }
            }
            else
            {
                boolean unique = rs.getBoolean(4);
                String name = rs.getString(6);
                text = "Index "+name+" at column "+column + " " +
                              (unique ? "unique" : "not unique") + " type: " +
                              indexTypeToString(type);
            }
        }

        Object[] getAsTableRow()
        {
            return new Object[] {
                text,
            };
        }

        static Object[] getTableHeader()
        {
            return new Object[] {
                "Text",
            };
        }
    }
}
