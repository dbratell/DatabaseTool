package databasetool.ui;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NonTablesViewsNode extends AbstractTablesTypeNode
{

    public NonTablesViewsNode(NavigationTreeModel navigationTreeModel,
                       CatalogNode parent, String catalogName)
    {
        super(navigationTreeModel, parent, catalogName);
    }

    protected void ensureChildren()
    {
        if (mTables != null)
        {
            return;
        }

        try
        {
            Connection connection = mNavigationTreeModel.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(mCatalogName, null, null, null);
            //ResultSet tables = meta.getTables(mCatalogName, "", "", null);
            ArrayList tableNames = new ArrayList();
            ArrayList tableTypes = new ArrayList();
            while(tables.next())
            {
                String tableType = tables.getString(4);
                if (!isTableType(tableType) && !isViewType(tableType))
                {
                    String tableName = tables.getString(3);
                    tableNames.add(tableName);
                    tableTypes.add(tableType);
                }
            }
            tables.close();

            mTables = new TableNode[tableNames.size()];
            for (int i = 0; i < mTables.length; i++)
            {
                mTables[i] = new TableNode(mNavigationTreeModel, this,
                                           (String)tableNames.get(i),
                                           (String)tableTypes.get(i));
            }
        }
        catch (SQLException e)
        {
            mNavigationTreeModel.getProgressArea().appendProgress(e);
            mTables = new TableNode[0];
        }
    }

    public String toString()
    {
        return "Other objects";
    }

}
