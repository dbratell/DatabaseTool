package databasetool.ui.navigationtree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class ConstraintsForTableNode implements TreeNode
{
    private String mTableName;
    private NavigationTreeModel mNavigationTreeModel;
    private TreeNode mParent;
    private String mTableType;

    public ConstraintsForTableNode(NavigationTreeModel navigationTreeModel,
                     TreeNode parent, String tableName, String tableType)
    {
        mNavigationTreeModel = navigationTreeModel;
        mParent = parent;
        mTableName = tableName;
        mTableType = tableType;
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex)
    {
        return null;
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount()
    {
        return 0;
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
        return 0;
    }

    /**
     * Returns true if the receiver allows children.
     */
    public boolean getAllowsChildren()
    {
        return false;
    }

    /**
     * Returns true if the receiver is a leaf.
     */
    public boolean isLeaf()
    {
        return true;
    }

    /**
     * Returns the children of the receiver as an <code>Enumeration</code>.
     */
    public Enumeration children()
    {
        return null;
    }

    public String toString()
    {
        return "Constraints";
    }
    public String getTableName()
    {
        return mTableName;
    }

    public String getCatalogName()
    {
        TreeNode ancestor = mParent;
        while (ancestor != null && !(ancestor instanceof CatalogNode))
        {
            ancestor = ancestor.getParent();
        }

        if (ancestor == null)
        {
            return null;
        }

        return ((CatalogNode)ancestor).getCatalogName();
    }

    public String getScheme()
    {
        return null;
    }
}
