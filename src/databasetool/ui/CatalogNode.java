package databasetool.ui;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CatalogNode implements TreeNode
{
    private RootNode mParent;
    private NavigationTreeModel mNavigationTreeModel;
    private String mCatalogName;
    private TableNode[] mTables;

    public CatalogNode(NavigationTreeModel navigationTreeModel,
                       RootNode parent, String catalogName)
    {
        mNavigationTreeModel = navigationTreeModel;
        mParent = parent;
        mCatalogName = catalogName;
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex)
    {
        ensureChildren();
        return mTables[childIndex];
    }

    private void ensureChildren()
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
                String tableName = tables.getString(3);
                tableNames.add(tableName);
                String tableType = tables.getString(4);
                tableTypes.add(tableType);
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

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount()
    {
        ensureChildren();
        return mTables.length;
    }

    /**
     * Returns the parent <code>TreeNode</code> of the receiver.
     */
    public TreeNode getParent()
    {
        return mParent;
    }

    public int getIndex(TreeNode treeNode)
    {
        ensureChildren();

        for (int i = 0; i < mTables.length; i++)
        {
            if (treeNode == mTables[i])
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the receiver allows children.
     */
    public boolean getAllowsChildren()
    {
        return true;
    }

    /**
     * Returns true if the receiver is a leaf.
     */
    public boolean isLeaf()
    {
        return false;
    }

    /**
     * Returns the children of the receiver as an <code>Enumeration</code>.
     */
    public Enumeration children()
    {
        ensureChildren();
        return new Enumeration() {
            int i = 0;
            public boolean hasMoreElements()
            {
                return i <= mTables.length;
            }
            public Object nextElement()
            {
                if (i < mTables.length)
                {
                    return mTables[i++];
                }
                throw new NoSuchElementException();
            }
        };
    }

    public String toString()
    {
        return mCatalogName;
    }

    public String getCatalogName()
    {
        return mCatalogName;
    }
}
