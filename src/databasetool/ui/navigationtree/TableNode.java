package databasetool.ui.navigationtree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Arrays;

public class TableNode implements TreeNode
{
    private String mTableName;
    private NavigationTreeModel mNavigationTreeModel;
    private TreeNode mParent;
    private String mTableType;
    private IndexesForTableNode mIndexNode;
    private ConstraintsForTableNode mConstraintNode;
    private ColumnsForTableNode mColumnNode;
    private final TreeNode[] mChildren;

    public TableNode(NavigationTreeModel navigationTreeModel,
                     TreeNode parent, String tableName, String tableType)
    {
        mNavigationTreeModel = navigationTreeModel;
        mParent = parent;
        mTableName = tableName;
        mTableType = tableType;
        mIndexNode = new IndexesForTableNode(navigationTreeModel,
                                             this, tableName, tableType);
        mConstraintNode = new ConstraintsForTableNode(navigationTreeModel,
                                             this, tableName, tableType);
        mColumnNode = new ColumnsForTableNode(navigationTreeModel,
                                             this, tableName, tableType);
        mChildren = new TreeNode[] {mColumnNode, mIndexNode, mConstraintNode};
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex)
    {
        return mChildren[childIndex];
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount()
    {
        return mChildren.length; // The indexes and constraints
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
        return Arrays.asList(mChildren).indexOf(treeNode);
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
        return new Enumeration(){
            int i = 0;
            public boolean hasMoreElements()
            {
                return i < mChildren.length;
            }

            public Object nextElement()
            {
                return mChildren[i++];
            }
        };
    }

    public String toString()
    {
        return mTableName + " (" + mTableType + ")";
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
