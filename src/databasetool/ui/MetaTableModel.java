package databasetool.ui;

import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;

public class MetaTableModel extends AbstractTableModel
{
    private ArrayList mData = new ArrayList();
    private ProgressArea mProgressArea;
    private StatusBar mStatusBar;

    public MetaTableModel(ProgressArea progressArea, StatusBar statusBar)
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
        return ColumnInfo.fieldCount;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        ColumnInfo columnInfo = (ColumnInfo)mData.get(rowIndex);
        return columnInfo.get(columnIndex);
    }

    public void loadTable(Connection connection, String scheme,
                          String catalogName, String tableName)
    {
        try
        {
            mProgressArea.appendProgress("Loading meta from table " + tableName);
            String oldCatalog = connection.getCatalog();
            mProgressArea.appendProgress("Setting catalog to " + catalogName);
            connection.setCatalog(catalogName);

            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columnRs = meta.getColumns(catalogName, scheme, tableName, null);
            ArrayList columns = new ArrayList();
            while(columnRs.next())
            {
                ColumnInfo column = new ColumnInfo();
                column.name = columnRs.getString(4);
                column.jdbcType = columnRs.getInt(5);
                column.nativeType = columnRs.getString(6);
                column.size = columnRs.getInt(7);
                column.nullableJDBC = columnRs.getInt(11);
                column.remark = columnRs.getString(12);
                column.definition = columnRs.getString(13);
                column.charLength = columnRs.getString(16);
                column.nullableNative = columnRs.getString(18);
//                column.refCatalog = columnRs.getString(19);
//                column.refSchema = columnRs.getString(20);
//                column.refTable = columnRs.getString(21);

                columns.add(column);
            }
            columnRs.close();

            mData = columns;

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
        return ColumnInfo.headers[column];
    }

    private static class ColumnInfo
    {
        static String[] headers = new String[] {
            "Column name", "JDBC Type", "Native type", "Size", "nullable in JDBC",
            "Remark", "Definition", "Char length", "Really nullable",
//            "Ref cat", "Ref schema", "Ref table"
        };
        static int fieldCount = headers.length;

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

        public Object get(int index)
        {
            switch (index)
            {
                case 0: return name;
                    case 1: return new Integer(jdbcType);
                    case 2: return nativeType;
                    case 3: return new Integer(size);
                    case 4: return new Integer(nullableJDBC);
                    case 5: return remark;
                    case 6: return definition;
                    case 7: return charLength;
                    case 8: return nullableNative;
//                    case 9: return refCatalog;
  //                  case 10: return refSchema;
    //                case 11: return refTable;
            }
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }
}
