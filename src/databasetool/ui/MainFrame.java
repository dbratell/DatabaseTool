package databasetool.ui;

import databasetool.ui.navigationtree.CatalogNode;
import databasetool.ui.navigationtree.NavigationTree;
import databasetool.ui.navigationtree.TableNode;
import databasetool.ui.navigationtree.AbstractTableMetaNode;
import databasetool.ui.navigationtree.TableMeta;
import databasetool.ui.navigationtree.AbstractDatabaseMetaNode;
import databasetool.ui.navigationtree.DatabaseMeta;
import databasetool.ui.navigationtree.DatabaseInfoNode;
import databasetool.DatabaseTool;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.prefs.Preferences;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLWarning;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;

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
    private MetaDataInfoPanel mMetaDataInfoPanel;
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

    private Map mActions;
    private static final String ACTION_QUIT = "Quit";
    private static final String ACTION_ABOUT = "About";

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

                if (mDataProgressSplitter != null)
                {
                    USER_PREFS.putInt(TABLE_SPLITTER_POS_PREF,
                                      mDataProgressSplitter.getDividerLocation());
                }
                if (mTreeSplitter != null)
                {
                    USER_PREFS.putInt(TREE_SPLITTER_POS_PREF,
                                      mTreeSplitter.getDividerLocation());
                }

                if (mDatabaseDriverField != null)
                {
                    mDatabaseDriverField.saveContents();
                }

                if (mConnectInfo != null)
                {
                    mConnectInfo.saveContents();
                }

                // Close and exit
                setVisible(false);
                System.exit(0);
            }
        });
    }

    private void initWindowContents()
    {
        Container rootPane = getContentPane();
        rootPane.setLayout(new BorderLayout());
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        mProgressArea = new ProgressArea();

//        mStatusBar = new StatusBar();
//        rootPane.add(mStatusBar, BorderLayout.SOUTH);

        mNavigationTree = new NavigationTree(mProgressArea);
        //Listen for when the selection changes.
        setTreeSelectionHandler();
        createSelectionPanels();
        mDataProgressSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                   mProgressArea, mSelectionInfoPane);
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

    private void createSelectionPanels()
    {
        mSelectionPaneLayout = new CardLayout();
        mSelectionInfoPane = new JPanel(mSelectionPaneLayout); // SelectionInfoPane(mProgressArea, mStatusBar);
//        mSelectionInfoPane.setBorder(BorderFactory.createLineBorder(Color.RED));
        mColumnsPanel = new ColumnsPanel(mProgressArea);
        mSelectionInfoPane.add(mColumnsPanel, "mColumnsPanel");
        mIndexesPanel = new IndexesPanel(mProgressArea);
        mSelectionInfoPane.add(mIndexesPanel, "mIndexesPanel");
        mDataPanel = new DataTable(mProgressArea);
        mSelectionInfoPane.add(mDataPanel, "mDataPanel");
        mConstraintsPanel = new ConstraintsPanel(mProgressArea);
        mSelectionInfoPane.add(mConstraintsPanel, "mConstraintsPanel");
        mResultSetPanel = new ResultSetPanel(mProgressArea);
        mSelectionInfoPane.add(mResultSetPanel, "mResultSetPanel");
        mMetaDataInfoPanel = new MetaDataInfoPanel(mProgressArea);
        mSelectionInfoPane.add(mMetaDataInfoPanel, "mMetaDataInfoPanel");
        mSelectionInfoPane.add(new JLabel("The operation was unsupported"),
                               "unsupported");
    }

    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        // Menu 1
        JMenu fileMenu;
        {
            fileMenu = new JMenu("File");
            fileMenu.add(new JMenuItem(getAction(ACTION_QUIT)));
        }

        // Menu 2
        JMenu helpMenu;
        {
            helpMenu = new JMenu("Help");
            helpMenu.add(new JMenuItem(getAction(ACTION_ABOUT)));
        }

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private Action getAction(String actionName)
    {
        if (mActions == null)
        {
            createActions();
        }
        return (Action)mActions.get(actionName);
    }

    private void createActions()
    {
        Action quitAction = new AbstractAction(ACTION_QUIT) {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        };
        quitAction.putValue(Action.LONG_DESCRIPTION, "Quits the program");
        quitAction.putValue(Action.SHORT_DESCRIPTION, "Quit program");
        quitAction.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));

        Action aboutAction = new AbstractAction(ACTION_ABOUT) {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(MainFrame.this,
                                              DatabaseTool.NAME_AND_VERSION);
            }
        };
        aboutAction.putValue(Action.LONG_DESCRIPTION, "Displays information " +
                                                      "about the program");
        aboutAction.putValue(Action.SHORT_DESCRIPTION, "About the program");
        aboutAction.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));

        mActions = new HashMap();
        mActions.put(quitAction.getValue(Action.NAME), quitAction);
        mActions.put(aboutAction.getValue(Action.NAME), aboutAction);
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
                else if (node instanceof AbstractDatabaseMetaNode)
                {
                    AbstractDatabaseMetaNode databaseMetaNode =
                            (AbstractDatabaseMetaNode)node;

                    readDatabaseMeta(databaseMetaNode);
                }
                else if (node instanceof CatalogNode)
                {
                    CatalogNode catalogNode = ((CatalogNode)node);
                    String catalogName = catalogNode.getCatalogName();

                    mProgressArea.appendProgress("Selected catalog "+
                                                 catalogName);
                }
                else if (node instanceof DatabaseInfoNode)
                {
                    mProgressArea.appendProgress("Selected databaseinfo");
                    if (mConnection != null)
                    {
                        mMetaDataInfoPanel.displayMetaData(mConnection);
                        mSelectionPaneLayout.show(mSelectionInfoPane,
                                                  "mMetaDataInfoPanel");
                    }
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

    private void readDatabaseMeta(AbstractDatabaseMetaNode node)
    {
        ResultSet rs = null;
        try
        {
            try
            {
                DatabaseMetaData dbMeta = mConnection.getMetaData();
                if (node instanceof DatabaseMeta.AttributesTableNode)
                {
                    rs = dbMeta.getAttributes(null, null, null, null);
                }
                else if (node instanceof DatabaseMeta.CatalogsNode)
                {
                    rs = dbMeta.getCatalogs();
                }
                else if (node instanceof DatabaseMeta.SchemasNode)
                {
                    rs = dbMeta.getSchemas();
                }
                else if (node instanceof DatabaseMeta.TableTypesNode)
                {
                    rs = dbMeta.getTableTypes();
                }
                else if (node instanceof DatabaseMeta.TypesNode)
                {
                    rs = dbMeta.getTypeInfo();
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Unknown type " + node.getClass() .getName());
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
        mDataPanel.displayTable(catalogName, scheme, tableName);

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
            String sql = mSQLStatement.getText();
            mProgressArea.appendProgress("Running sql '"+sql+"'");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try
            {
                ps = mConnection.prepareStatement(sql);
                rs = ps.executeQuery();
                mResultSetPanel.loadResultSet(rs);
            }
            finally
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (ps != null)
                {
                    ps.close();
                }
            }
            mSelectionPaneLayout.show(mSelectionInfoPane, "mResultSetPanel");
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    private void connect()
    {
        try
        {
            String connectUrl = mConnectInfo.getConnectString();
            Driver driver = DriverManager.getDriver(connectUrl);
            mProgressArea.appendProgress("JDBC Compliant: "+
                                         driver.jdbcCompliant());
            // This destroys the driver internal state so that we can't
            // connect
//            listConnectionProperties(driver, connectUrl);
            mConnection =
                    DriverManager.getConnection(connectUrl,
                                                mConnectInfo.getUser(),
                                                mConnectInfo.getPassword());
            mProgressArea.appendProgress("Got connection#1");
            mNavigationTree.getNavigationTreeModel().setConnection(mConnection);

            checkConnectionWarnings(mConnection.getWarnings());
            Connection dataConnection;
            dataConnection =
                    DriverManager.getConnection(connectUrl,
                                                mConnectInfo.getUser(),
                                                mConnectInfo.getPassword());
            mProgressArea.appendProgress("Got connection#2");
            mDataPanel.setConnection(dataConnection);
            checkConnectionWarnings(dataConnection.getWarnings());
        }
        catch (SQLException e)
        {
            mProgressArea.appendProgress(e);
        }
    }

    private void listConnectionProperties(Driver driver, String connectUrl)
    {
        try
        {
            DriverPropertyInfo[] propertyInfo =
                    driver.getPropertyInfo(connectUrl, new Properties());
            for (int i = 0; i < propertyInfo.length; i++)
            {
                DriverPropertyInfo propInfo = propertyInfo[i];
                mProgressArea.appendProgress(
                        propInfo.name + ": " + (propInfo.required ? "required" :
                                                "not required") + " " +
                        propInfo.description + ", current value="+propInfo.value+
                        ". Possible values: "+propInfo.choices);
            }
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
