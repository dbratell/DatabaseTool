package databasetool.ui;

import databasetool.ui.navigationtree.CatalogNode;
import databasetool.ui.navigationtree.NavigationTree;
import databasetool.ui.navigationtree.TableNode;
import databasetool.ui.navigationtree.AbstractTableMetaNode;
import databasetool.ui.navigationtree.TableMeta;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.prefs.Preferences;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.sql.DatabaseMetaData;

/**
 * @author Daniel Bratell
 */
public class MainFrame extends JFrame
{
    private static final Preferences USER_PREFS =
            Preferences.userNodeForPackage(MainFrame.class);
    private DatabaseDriverField mDatabaseDriverField;
    private ProgressArea mProgressArea;
    private ConnectInfo mConnectInfo;
    private JButton mConnectButton;
    private Connection mConnection;
    private JTextField mSQLStatement;
    private JButton mExecuteButton;
    private NavigationTree mNavigationTree;
    private JSplitPane mDataProgressSplitter;
    private JSplitPane mTreeSplitter;
    private ConstraintsPanel mConstraintsPanel;
    private ColumnsPanel mColumnsPanel;
    private DataTable mDataPanel;
    private ResultSetPanel mResultSetPanel;
    private static final String FRAME_WIDTH_PREF = "frameWidth";
    private static final String FRAME_HEIGHT_PREF = "frameHeight";
    private static final String TABLE_SPLITTER_POS_PREF = "TableSplitter";
    private static final String TREE_SPLITTER_POS_PREF = "TreeSplitter";
//    private SelectionInfoPane mSelectionInfoPane;
    private JPanel mSelectionInfoPane;
    private StatusBar mStatusBar;
    private IndexesPanel mIndexesPanel;
    private CardLayout mSelectionPaneLayout;

    public MainFrame()
    {
        super("Database tool");

        setSize(USER_PREFS.getInt(FRAME_WIDTH_PREF, 400),
                USER_PREFS.getInt(FRAME_HEIGHT_PREF, 300));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setOurWindowListener();

        initWindowContents();
    }

    private void setOurWindowListener()
    {
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                // Save window size
                Dimension size = getSize();
                USER_PREFS.putInt(FRAME_WIDTH_PREF, size.width);
                USER_PREFS.putInt(FRAME_HEIGHT_PREF, size.height);

                USER_PREFS.putInt(TABLE_SPLITTER_POS_PREF,
                                  mDataProgressSplitter.getDividerLocation());
                USER_PREFS.putInt(TREE_SPLITTER_POS_PREF,
                                  mTreeSplitter.getDividerLocation());

                mDatabaseDriverField.saveContents();
                mConnectInfo.saveContents();

                // Close and exit
                setVisible(false);
                System.exit(0);
            }
        });
    }

    private void initWindowContents()
    {
        JRootPane rootPane = getRootPane();
        rootPane.setLayout(new BorderLayout());
        mProgressArea = new ProgressArea();


        mStatusBar = new StatusBar();
        rootPane.add(mStatusBar, BorderLayout.SOUTH);

        mNavigationTree = new NavigationTree(mProgressArea);
        //Listen for when the selection changes.
        setTreeSelectionHandler();
        mSelectionPaneLayout = new CardLayout();
        mSelectionInfoPane = new JPanel(mSelectionPaneLayout); // SelectionInfoPane(mProgressArea, mStatusBar);
//        mSelectionInfoPane.setBorder(BorderFactory.createLineBorder(Color.RED));
        mColumnsPanel = new ColumnsPanel(mProgressArea);
        mSelectionInfoPane.add(mColumnsPanel, "mColumnsPanel");
        mIndexesPanel = new IndexesPanel(mProgressArea);
        mSelectionInfoPane.add(mIndexesPanel, "mIndexesPanel");
        mDataPanel = new DataTable(mProgressArea, mStatusBar);
        mSelectionInfoPane.add(mDataPanel, "mDataPanel");
        mConstraintsPanel = new ConstraintsPanel(mProgressArea);
        mSelectionInfoPane.add(mConstraintsPanel, "mConstraintsPanel");
        mResultSetPanel = new ResultSetPanel(mProgressArea);
        mSelectionInfoPane.add(mResultSetPanel, "mResultSetPanel");
        mSelectionInfoPane.add(new JLabel("Hej"), "dummy");
        mDataProgressSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                   mProgressArea,
                                   mSelectionInfoPane);
        int splitterPos = USER_PREFS.getInt(TABLE_SPLITTER_POS_PREF, -1);
        if (splitterPos != -1)
        {
            mDataProgressSplitter.setDividerLocation(splitterPos);
        }
        mTreeSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                 new JScrollPane(mNavigationTree),
                                                 mDataProgressSplitter);
        splitterPos = USER_PREFS.getInt(TREE_SPLITTER_POS_PREF, -1);
        if (splitterPos != -1)
        {
            mTreeSplitter.setDividerLocation(splitterPos);
        }
        rootPane.add(mTreeSplitter, BorderLayout.CENTER);
        Box topPart = Box.createVerticalBox();
        mDatabaseDriverField = new DatabaseDriverField(mProgressArea);
        topPart.add(mDatabaseDriverField);
        mConnectInfo = new ConnectInfo();
        topPart.add(mConnectInfo);
        mConnectButton = new JButton("Connect");
        mConnectButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                connect();
            }
        });
        topPart.add(mConnectButton);
        mSQLStatement = new JTextField();
        topPart.add(mSQLStatement);
        mExecuteButton = new JButton("Execute");
        mExecuteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                executeSQL();
            }
        });
        topPart.add(mExecuteButton);
        rootPane.add(topPart, BorderLayout.NORTH);
    }

    private void setTreeSelectionHandler()
    {
        mNavigationTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                TreePath path = mNavigationTree.getSelectionPath();
                if (path == null)
                {
                    return;
                }

                TreeNode node = (TreeNode)path.getLastPathComponent();

                if (node instanceof TableNode)
                {
                    TableNode tableNode = ((TableNode)node);
                    String tableName = tableNode.getTableName();
                    String scheme = tableNode.getScheme();
                    String catalogName = tableNode.getCatalogName();

                    readTable(catalogName, scheme, tableName);
                }
                else if (node instanceof TableMeta.IndexesForTableNode)
                {
                    TableMeta.IndexesForTableNode indexNode =
                            (TableMeta.IndexesForTableNode)node;
                    String catalogName = indexNode.getCatalogName();
                    String schemeName = indexNode.getScheme();
                    String tableName = indexNode.getTableName();

                    mProgressArea.appendProgress("Selected indexes for table "+
                                                 tableName);
                    readIndexes(catalogName, schemeName, tableName);
                }
                else if (node instanceof TableMeta.ConstraintsForTableNode)
                {
                    TableMeta.ConstraintsForTableNode indexNode =
                            (TableMeta.ConstraintsForTableNode)node;
                    String catalogName = indexNode.getCatalogName();
                    String schemeName = indexNode.getScheme();
                    String tableName = indexNode.getTableName();

                    mProgressArea.appendProgress("Selected constraints for table "+
                                                 tableName);
                    readConstraints(catalogName, schemeName, tableName);
                }
                else if (node instanceof TableMeta.ColumnsForTableNode)
                {
                    TableMeta.ColumnsForTableNode indexNode =
                            (TableMeta.ColumnsForTableNode)node;
                    String catalogName = indexNode.getCatalogName();
                    String schemeName = indexNode.getScheme();
                    String tableName = indexNode.getTableName();

                    mProgressArea.appendProgress("Selected columns for table "+
                                                 tableName);
                    readColumns(catalogName, schemeName, tableName);
                }
                else if (node instanceof AbstractTableMetaNode)
                {
                    AbstractTableMetaNode tableMetaNode =
                            (AbstractTableMetaNode)node;
                    String catalogName = tableMetaNode.getCatalogName();
                    String schemeName = tableMetaNode.getScheme();
                    String tableName = tableMetaNode.getTableName();

                    readTableMeta(tableMetaNode, catalogName,
                                  schemeName, tableName);
                }
                else if (node instanceof CatalogNode)
                {
                    CatalogNode catalogNode = ((CatalogNode)node);
                    String catalogName = catalogNode.getCatalogName();

                    mProgressArea.appendProgress("Selected catalog "+
                                                 catalogName);
                }
                else
                {
                    mProgressArea.appendProgress("Selected "+
                                                 node.getClass().getName() +
                                                 " - "+node.toString());
                }
            }
        });
    }

    private void readTableMeta(AbstractTableMetaNode node, String catalogName,
                               String schemeName, String tableName)
    {
        ResultSet rs = null;
        try
        {
            try
            {
                DatabaseMetaData dbMeta = mConnection.getMetaData();
                if (node instanceof TableMeta.ColumnPrivilegesNode)
                {
                    rs = dbMeta.getColumnPrivileges(catalogName, schemeName,
                                                    tableName, null);
                }
                else if (node instanceof TableMeta.ExportedKeysNode)
                {
                    rs = dbMeta.getExportedKeys(catalogName, schemeName,
                                                tableName);
                }
                else if (node instanceof TableMeta.PrimaryKeysNode)
                {
                    rs = dbMeta.getPrimaryKeys(catalogName, schemeName,
                                               tableName);
                }
                else if (node instanceof TableMeta.TablePrivilegesNode)
                {
                    rs = dbMeta.getTablePrivileges(catalogName, schemeName,
                                                    tableName);
                }
                mResultSetPanel.loadResultSet(rs);
            }
            finally
            {
                if (rs != null)
                {
                    rs.close();
                }
            }
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
        mSelectionPaneLayout.show(mSelectionInfoPane, "mResultSetPanel");
    }

    private void readTable(String catalogName, String scheme, String tableName)
    {
        mSelectionPaneLayout.show(mSelectionInfoPane, "mDataPanel");
        mDataPanel.displayTable(mConnection, catalogName, scheme, tableName);

//        mSelectionInfoPane.displayTable(mConnection, scheme,
//                                catalogName, tableName);
    }

    private void readConstraints(String catalogName, String schemeName,
                             String tableName)
    {
        mConstraintsPanel.loadConstraints(mConnection, catalogName, schemeName,
                                          tableName);
        mSelectionPaneLayout.show(mSelectionInfoPane, "mSelectionInfoPane");
    }

    private void readColumns(String catalogName, String schemeName,
                             String tableName)
    {
//        mColumnsPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        mColumnsPanel.loadColumns(mConnection, catalogName, schemeName,
                                          tableName);
        mSelectionPaneLayout.show(mSelectionInfoPane, "mColumnsPanel");
    }

    private void readIndexes(String catalogName, String schemeName,
                             String tableName)
    {
        mIndexesPanel.loadIndexes(mConnection, catalogName, schemeName,
                                          tableName);
        mSelectionPaneLayout.show(mSelectionInfoPane, "mIndexesPanel");
    }

    private void executeSQL()
    {
        try
        {
            PreparedStatement ps =
                    mConnection.prepareStatement(mSQLStatement.getText());
            ResultSet rs = ps.executeQuery();
            printResultSet(rs);
            rs.close();
            ps.close();
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    private void printResultSet(ResultSet rs)
            throws SQLException
    {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuffer label = new StringBuffer();
        for (int i = 1; i <= columnCount; i++)
        {
            if (i > 1)
            {
                label.append(", ");
            }
            label.append(metaData.getColumnLabel(i));
        }
        mProgressArea.appendProgress(label.toString());

        int rowCount = 0;
        while (rs.next())
        {
            rowCount++;
            StringBuffer row = new StringBuffer();
            for (int i = 1; i <= columnCount; i++)
            {
                if (i > 1)
                {
                    row.append(", ");
                }
                row.append(rs.getString(i));
            }
            mProgressArea.appendProgress(row.toString());
        }

        mProgressArea.appendProgress("Got " + rowCount + " rows");
    }

    private void connect()
    {
        try
        {
            mConnection =
                    DriverManager.getConnection(mConnectInfo.getConnectString(),
                                                mConnectInfo.getUser(),
                                                mConnectInfo.getPassword());
            mProgressArea.appendProgress("Got connection");
            mNavigationTree.getNavigationTreeModel().setConnection(mConnection);
            checkConnectionWarnings(mConnection.getWarnings());
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    /**
     * If a SQLWarning object was given, display the
     * warning messages.  Note that there could be
     * multiple warnings chained together
     *
     * @param warning
     */
    private void checkConnectionWarnings(SQLWarning warning)
    {
        if (warning != null)
        {
            mProgressArea.appendProgress(" *** Warning ***");
            while (warning != null)
            {
                mProgressArea.appendProgress("SQLState: " +
                                             warning.getSQLState());
                mProgressArea.appendProgress("Message:  " +
                                             warning.getMessage());
                mProgressArea.appendProgress("Vendor:   " +
                                             warning.getErrorCode());
                mProgressArea.appendProgress("");
                warning = warning.getNextWarning();
            }
        }
    }
}
