package databasetool.ui;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: 2003-aug-12
 * Time: 21:15:30
 * To change this template use Options | File Templates.
 */
public abstract class AbstractTablesTypeNode implements TreeNode
{
    private final CatalogNode mParent;
    protected final NavigationTreeModel mNavigationTreeModel;
    protected final String mCatalogName;
    protected TableNode[] mTables;

    public AbstractTablesTypeNode(NavigationTreeModel navigationTreeModel,
                                  CatalogNode parent,
                                  String catalogName)
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

    protected abstract void ensureChildren();

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

    public abstract String toString();

    public String getCatalogName()
    {
        return mCatalogName;
    }
    protected boolean isTableType(String type)
    {
        return "TABLE".equals(type);
    }

    protected boolean isViewType(String type)
    {
        return "VIEW".equals(type);
    }
}
