package databasetool.ui.navigationtree;

import databasetool.ui.ProgressArea;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import java.sql.Connection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class NavigationTreeModel implements TreeModel
{
    private Connection mConnection;
    private final Set mListeners = new HashSet();
    private RootNode mRoot = new RootNode(this);
    private ProgressArea mProgressArea;


    public NavigationTreeModel(ProgressArea progressArea)
    {
        mProgressArea = progressArea;
    }

    public void setConnection(Connection connection)
    {
        mConnection = connection;
        mRoot.addCatalogs();
        fireTreeStructureChanged(mRoot, new TreePath(mRoot));
    }

    public Object getRoot()
    {
        return mRoot;
    }

    public Object getChild(Object parent, int index)
    {
        if (parent instanceof TreeNode)
        {
            return ((TreeNode)parent).getChildAt(index);
        }
        notImplemented();
        return null;
    }


    public int getChildCount(Object parent)
    {
        if (parent instanceof TreeNode)
        {
            return ((TreeNode)parent).getChildCount();
        }
        notImplemented();
        return 0;
    }

    public boolean isLeaf(Object node)
    {
        if (node instanceof TreeNode)
        {
            return ((TreeNode)node).isLeaf();
        }
        return false;
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
        notImplemented();
    }

    private void notImplemented()
    {
        throw new RuntimeException("Not implemented");
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof TreeNode &&
                child instanceof TreeNode)
        {
            return ((TreeNode)parent).getIndex((TreeNode)child);
        }
        notImplemented();
        return 0;
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        mListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        mListeners.remove(l);
    }

    public Connection getConnection()
    {
        return mConnection;
    }

    public ProgressArea getProgressArea()
    {
        return mProgressArea;
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @see javax.swing.event.EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, TreePath path)
    {
        TreeModelEvent e = null;
        for (Iterator iterator = mListeners.iterator(); iterator.hasNext();)
        {
            TreeModelListener treeModelListener =
                    (TreeModelListener)iterator.next();

            // Lazily create the event:
            if (e == null)
            {
                e = new TreeModelEvent(source, path);
            }
            treeModelListener.treeStructureChanged(e);
        }
    }
}
