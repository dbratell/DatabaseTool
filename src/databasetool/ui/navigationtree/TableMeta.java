package databasetool.ui.navigationtree;

import javax.swing.tree.TreeNode;

public class TableMeta
{
    public static class ColumnsForTableNode extends AbstractTableMetaNode
    {
        public ColumnsForTableNode(NavigationTreeModel navigationTreeModel,
                         TreeNode parent, String tableName, String tableType)
        {
            super(tableName, navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Columns";
        }
    }

    public static class ConstraintsForTableNode extends AbstractTableMetaNode
    {
        public ConstraintsForTableNode(NavigationTreeModel navigationTreeModel,
                         TreeNode parent, String tableName, String tableType)
        {
            super(tableName,  navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Constraints";
        }
    }

    public static class IndexesForTableNode extends AbstractTableMetaNode
    {
        public IndexesForTableNode(NavigationTreeModel navigationTreeModel,
                                   TreeNode parent, String tableName,
                                   String tableType)
        {
            super(tableName, navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Index";
        }
    }

    public static class ColumnPrivilegesNode extends AbstractTableMetaNode
    {
        public ColumnPrivilegesNode(NavigationTreeModel navigationTreeModel,
                                    TreeNode parent, String tableName,
                                    String tableType)
        {
            super(tableName, navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Column Privileges";
        }
    }

    public static class TablePrivilegesNode extends AbstractTableMetaNode
    {
        public TablePrivilegesNode(NavigationTreeModel navigationTreeModel,
                                   TreeNode parent, String tableName,
                                   String tableType)
        {
            super(tableName, navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Table Privileges";
        }
    }

    public static class ExportedKeysNode extends AbstractTableMetaNode
    {
        public ExportedKeysNode(NavigationTreeModel navigationTreeModel,
                                TreeNode parent, String tableName,
                                String tableType)
        {
            super(tableName, navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Exported Keys";
        }
    }

    public static class PrimaryKeysNode extends AbstractTableMetaNode
    {
        public PrimaryKeysNode(NavigationTreeModel navigationTreeModel,
                               TreeNode parent, String tableName,
                               String tableType)
        {
            super(tableName, navigationTreeModel, parent, tableType);
        }

        public String toString()
        {
            return "Primary Keys";
        }
    }
}
