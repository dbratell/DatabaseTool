package databasetool.ui;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.prefs.Preferences;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
    private static final String FRAME_WIDTH_PREF = "frameWidth";
    private static final String FRAME_HEIGHT_PREF = "frameHeight";
    private static final String TABLE_SPLITTER_POS_PREF = "TableSplitter";
    private static final String TREE_SPLITTER_POS_PREF = "TreeSplitter";
    private SelectionInfoPane mSelectionInfoPane;
    private StatusBar mStatusBar;

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
        mSelectionInfoPane = new SelectionInfoPane(mProgressArea, mStatusBar);
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

                    mSelectionInfoPane.displayTable(mConnection, scheme,
                                            catalogName, tableName);
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
