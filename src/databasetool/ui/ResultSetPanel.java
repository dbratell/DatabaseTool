package databasetool.ui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.awt.BorderLayout;

public class ResultSetPanel extends JPanel
{
    private JTable mResultSetTable;
    private ProgressArea mProgress;

    public ResultSetPanel(ProgressArea progress)
    {
        setLayout(new BorderLayout());
        mProgress = progress;
        mResultSetTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(mResultSetTable);
        scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mResultSetTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadResultSet(ResultSet rs)
    {
        DefaultTableModel model = new DefaultTableModel();
        try
        {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Object[] header = new Object[columnCount];
            for (int i = 0; i < header.length; i++)
            {
                header[i] = metaData.getColumnLabel(i + 1);
            }
            model.setColumnIdentifiers(header);
            while (rs.next())
            {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < row.length; i++)
                {
                    row[i] = rs.getObject(i + 1);

                }
                model.addRow(row);
            }
        }
        catch (SQLException e)
        {
            mProgress.appendProgress(e);
            if (model.getRowCount() == 0)
            {
                model.setColumnCount(1);
                model.setColumnIdentifiers(new Object[]{"Info"});
                model.addRow(new Object[]{e.getMessage()});
            }
        }
        mResultSetTable.setModel(model);
    }
}
