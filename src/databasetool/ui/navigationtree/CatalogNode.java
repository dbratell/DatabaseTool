package databasetool.ui.navigationtree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class CatalogNode implements TreeNode
{
    private RootNode mParent;
    private String mCatalogName;
    private TreeNode[] mChildren;

    public CatalogNode(NavigationTreeModel navigationTreeModel,
                       RootNode parent, String catalogName)
    {
        mChildren = new TreeNode[3];
        mChildren[0] = new TablesNode(navigationTreeModel, this, catalogName);
        mChildren[1] = new ViewsNode(navigationTreeModel, this, catalogName);
        mChildren[2] = new NonTablesViewsNode(navigationTreeModel, this, catalogName);
        mParent = parent;
        mCatalogName = catalogName;

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
        return mChildren.length;
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
        for (int i = 0; i < mChildren.length; i++)
        {
            if (treeNode == mChildren[i])
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
        return new Enumeration() {
            int i = 0;
            public boolean hasMoreElements()
            {
                return i <= mChildren.length;
            }
            public Object nextElement()
            {
                if (i < mChildren.length)
                {
                    return mChildren[i++];
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
