package databasetool.ui.navigationtree;

import databasetool.ui.ProgressArea;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RootNode implements TreeNode
{
    private final DatabaseInfoNode mInfoNode;
    private TreeNode[] mCatalogs = new TreeNode[0];
    private NavigationTreeModel mNavigationTreeModel;

    public RootNode(NavigationTreeModel navigationTreeModel)
    {
        mNavigationTreeModel = navigationTreeModel;
        mInfoNode = new DatabaseInfoNode(navigationTreeModel, this);
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex)
    {
        if (childIndex == 0)
        {
            return mInfoNode;
        }
        return mCatalogs[childIndex-1];
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount()
    {
        return mCatalogs.length+1;
    }

    /**
     * Returns the parent <code>TreeNode</code> of the receiver.
     */
    public TreeNode getParent()
    {
        return null;
    }

    public int getIndex(TreeNode treeNode)
    {
        if (treeNode == mInfoNode)
        {
            return 0;
        }

        for (int i = 0; i < mCatalogs.length; i++)
        {
            TreeNode catalog = mCatalogs[i];
            if (catalog == treeNode)
            {
                return i+1;
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
        return new Enumeration() {
            int i = -1;
            public boolean hasMoreElements()
            {
                return i < mCatalogs.length;
            }
            public Object nextElement()
            {
                if (i == -1)
                {
                    i++;
                    return mInfoNode;
                }
                if (i < mCatalogs.length)
                {
                    return mCatalogs[i++];
                }
                throw new NoSuchElementException();
            }
        };
    }

    public void addCatalogs()
    {
        ProgressArea progressArea = mNavigationTreeModel.getProgressArea();
        try
        {
            DatabaseMetaData metaData = mNavigationTreeModel.getConnection().getMetaData();
            progressArea.appendProgress("Getting metadata");
            ResultSet rs = metaData.getCatalogs();
//                printResultSet(rs);
//                rs.close();
//                rs = metaData.getCatalogs();
            ArrayList catalogs = new ArrayList();
            while (rs.next())
            {
                String catalogName = rs.getString(1);
                catalogs.add(catalogName);
            }

            if (catalogs.size() > 0)
            {
//                mPrimaryCatalog = (String)catalogs.get(0);
            }
            else
            {
                throw new SQLException("No catalogs?");
            }
            rs.close();
  //          mProgressArea.appendProgress("Primary catalog: "+mPrimaryCatalog);

            CatalogNode[] catalogArray = new CatalogNode[catalogs.size()];
            int i = 0;
            for (Iterator iterator = catalogs.iterator(); iterator.hasNext();)
            {
                String catalogName = (String)iterator.next();
                catalogArray[i++] = new CatalogNode(mNavigationTreeModel,
                                                    this, catalogName);
            }
            mCatalogs = catalogArray;
            mNavigationTreeModel.fireTreeStructureChanged(this,
                                                          new TreePath(this));
        }
        catch (SQLException e)
        {
            progressArea.appendProgress("SQLException: "+e.getMessage());
        }
    }

    public String toString()
    {
        return "Database ("+mCatalogs.length+")";
    }
}
