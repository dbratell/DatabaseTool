package databasetool.ui.navigationtree;

import javax.swing.tree.TreeNode;

public class DatabaseMeta
{
    public static class AttributesTableNode extends AbstractDatabaseMetaNode
    {
        public AttributesTableNode(NavigationTreeModel navigationTreeModel,
                         TreeNode parent)
        {
            super(navigationTreeModel, parent);
        }

        public String toString()
        {
            return "Attributes for types";
        }
    }

    public static class SchemasNode extends AbstractDatabaseMetaNode
    {
        public SchemasNode(NavigationTreeModel navigationTreeModel,
                         TreeNode parent)
        {
            super(navigationTreeModel, parent);
        }

        public String toString()
        {
            return "Schemas";
        }
    }

    public static class CatalogsNode extends AbstractDatabaseMetaNode
    {
        public CatalogsNode(NavigationTreeModel navigationTreeModel,
                                   TreeNode parent)
        {
            super(navigationTreeModel, parent);
        }

        public String toString()
        {
            return "Catalogs";
        }
    }

    public static class TableTypesNode extends AbstractDatabaseMetaNode
    {
        public TableTypesNode(NavigationTreeModel navigationTreeModel,
                                    TreeNode parent)
        {
            super(navigationTreeModel, parent);
        }

        public String toString()
        {
            return "Table Types";
        }
    }

    public static class TypesNode extends AbstractDatabaseMetaNode
    {
        public TypesNode(NavigationTreeModel navigationTreeModel,
                                    TreeNode parent)
        {
            super(navigationTreeModel, parent);
        }

        public String toString()
        {
            return "Types";
        }
    }
}
