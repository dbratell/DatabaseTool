package databasetool.ui.navigationtree;

import databasetool.ui.ProgressArea;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

public class NavigationTree extends JTree
{
    ProgressArea mProgressArea;

    public NavigationTree(ProgressArea progressArea)
    {
        mProgressArea = progressArea;
        NavigationTreeModel model = new NavigationTreeModel(progressArea);
        setModel(model);
//        setEditable(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setShowsRootHandles(true);
    }

    public NavigationTreeModel getNavigationTreeModel()
    {
        return (NavigationTreeModel)getModel();
    }
}
