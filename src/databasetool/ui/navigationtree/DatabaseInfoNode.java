package databasetool.ui.navigationtree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Arrays;

public class DatabaseInfoNode implements TreeNode
{
    private NavigationTreeModel mNavigationTreeModel;
    private TreeNode mParent;
    private DatabaseMeta.AttributesTableNode mAttributesNode;
    private DatabaseMeta.CatalogsNode mCatalogsNode;
    private DatabaseMeta.SchemasNode mSchemasNode;
    private DatabaseMeta.TableTypesNode mTableTypeNode;
    private DatabaseMeta.TypesNode mTypeNode;
    private final TreeNode[] mChildren;

    public DatabaseInfoNode(NavigationTreeModel navigationTreeModel,
                            TreeNode parent)
    {
        mNavigationTreeModel = navigationTreeModel;
        mParent = parent;
        mAttributesNode = new DatabaseMeta.AttributesTableNode(
                navigationTreeModel, this);
        mCatalogsNode = new DatabaseMeta.CatalogsNode(navigationTreeModel,
                                                      this);
        mSchemasNode = new DatabaseMeta.SchemasNode(navigationTreeModel, this);
        mTableTypeNode = new DatabaseMeta.TableTypesNode(navigationTreeModel,
                                                         this);
        mTypeNode = new DatabaseMeta.TypesNode(navigationTreeModel, this);

        mChildren = new TreeNode[] {
            mAttributesNode,
            mCatalogsNode,
            mSchemasNode,
            mTableTypeNode,
            mTypeNode,
        };
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
        return "Database Info";
    }
}
