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
import java.lang.reflect.Field;

public class ColumnsPanel extends JPanel
{
    private JTable mColumnsTable;
    private ProgressArea mProgress;

    public ColumnsPanel(ProgressArea progress)
    {
        setLayout(new BorderLayout());
        mProgress = progress;
        mColumnsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(mColumnsTable);
        scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mColumnsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(scrollPane, BorderLayout.CENTER);
//        add(mColumnsTable);
//        setBorder(BorderFactory.createLineBorder(Color.RED));
    }

    public void loadColumns(Connection connection, String catalogName,
                            String schemeName,
                                String tableName)
    {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(ColumnInfo.getTableHeader());
        try
        {
            mProgress.appendProgress("Loading meta from table " + tableName);
            String oldCatalog = connection.getCatalog();
            mProgress.appendProgress("Setting catalog to " + catalogName);
            connection.setCatalog(catalogName);

            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columnRs = null;
            try
            {
                columnRs = meta.getColumns(catalogName, schemeName,
                                                     tableName, null);
                while(columnRs.next())
                {
                    ColumnInfo column = new ColumnInfo(columnRs);
                    model.addRow(column.getAsTableRow());
                }
            }
            finally
            {
                if (columnRs != null)
                {
                    columnRs.close();
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
        mColumnsTable.setModel(model);
        mColumnsTable.invalidate();
        validate();
    }

    private static String getJDBCTypeAsString(int jdbcType)
    {
        // XXX Don't do this on demand 
        try
        {
            Field[] fields = java.sql.Types.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                if (field.getType() == Integer.TYPE)
                {
                    if (field.getInt(null) == jdbcType)
                    {
                        return field.getName();
                    }
                }
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return e.getMessage();
        }
        return "Unknown ("+jdbcType+")";
    }


    private static class ColumnInfo
    {
        public String name;
        public int jdbcType;
        public String nativeType;
        public int size;
        public int nullableJDBC;
        public String remark;
        public String definition;
        public String charLength;
        public String nullableNative;
  //      public String refCatalog;
 //       public String refSchema;
  //      public String refTable;

        public ColumnInfo(ResultSet columnRs) throws SQLException
        {
            name = columnRs.getString(4);
            jdbcType = columnRs.getInt(5);
            nativeType = columnRs.getString(6);
            size = columnRs.getInt(7);
            nullableJDBC = columnRs.getInt(11);
            remark = columnRs.getString(12);
            definition = columnRs.getString(13);
            charLength = columnRs.getString(16);
            nullableNative = columnRs.getString(18);
//                column.refCatalog = columnRs.getString(19);
//                column.refSchema = columnRs.getString(20);
//                column.refTable = columnRs.getString(21);

        }

        Object[] getAsTableRow()
        {
            return new Object[] {
                name,
                getJDBCTypeAsString(jdbcType),
//                new Integer(jdbcType),
                    nativeType,
                    new Integer(size),
                    new Integer(nullableJDBC),
                    remark,
                    definition,
                    charLength,
                    nullableNative,
//                    case 9: return refCatalog;
  //                  case 10: return refSchema;
    //                case 11: return refTable;
            };
        }

        static Object[] getTableHeader()
        {
            return new Object[] {
                "Name", "JDBC Type", "Type name", "Size", "Nullable in JDBC",
                "Remark", "Definition", "Charlength", "Nullable native"
            };
        }
    }
}
