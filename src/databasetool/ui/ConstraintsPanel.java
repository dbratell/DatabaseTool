package databasetool.ui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class ConstraintsPanel extends JPanel
{
    private JTable mConstraintsTable;
    private ProgressArea mProgress;

    public ConstraintsPanel(ProgressArea progress)
    {
        mProgress = progress;
        mConstraintsTable = new JTable();
        add(mConstraintsTable);
    }

    public void loadConstraints(Connection connection, String catalogName,
                            String schemeName,
                                String tableName)
    {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(ConstraintInfo.getTableHeader());
        try
        {
            ResultSet importedKeys = null;
            DatabaseMetaData metaData = connection.getMetaData();
            try
            {
                importedKeys = metaData.getImportedKeys(
                                catalogName, schemeName, tableName);
                while (importedKeys.next())
                {
                    ConstraintInfo ci = new ConstraintInfo(importedKeys);
                    model.addRow(ci.getAsTableRow());
                }
            }
            finally
            {

                if (importedKeys != null)
                {
                    importedKeys.close();
                }
            }
            model.setColumnIdentifiers(new Object[] {
                "Name", "Description"
            });

            model.addRow(new Object[] {
                "Kalle", "Fem bokstäver"
            });
            model.addRow(new Object[] {
                "Olle", "Fyra bokstäver"
            });
        }
        catch (SQLException e)
        {
            mProgress.appendProgress(e);
            if (model.getRowCount() == 0)
            {
                model.setColumnCount(1);
                model.setColumnIdentifiers(new Object[] {"Info"});
                model.addRow(new Object[] {e.getMessage()});
            }
        }
        mConstraintsTable.setModel(model);
    }

    private static class ConstraintInfo
    {
        String mPrimaryTable;
        String mPrimaryTableColumn;
        String mThisTableColumn;

        public ConstraintInfo(ResultSet importedKeys) throws SQLException
        {
            String primCat = importedKeys.getString(1);
            String primScheme = importedKeys.getString(2);
            String primTable = importedKeys.getString(3);
            mPrimaryTable = (primCat == null ? "" : primCat+".") +
                           (primScheme == null ? "" : primScheme+".") +
                                  primTable;
            mPrimaryTableColumn = importedKeys.getString(3);
            mThisTableColumn = importedKeys.getString(8);

        }

        public ConstraintInfo(String primaryTable, String primaryTableColumn,
                                       String thisTableColumn)
        {
            mPrimaryTable = primaryTable;
            mPrimaryTableColumn = primaryTableColumn;
            mThisTableColumn = thisTableColumn;
        }

        Object[] getAsTableRow()
        {
            return new Object[] {
                mPrimaryTable, mPrimaryTableColumn, mThisTableColumn
            };
        }

        static Object[] getTableHeader()
        {
            return new Object[] {
                "Primary table", "Primary column", "This table column"
            };
        }
    }
}
