package databasetool.ui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class MetaDataInfoPanel extends JPanel
{
    private JTable mTable;
    private ProgressArea mProgress;

    public MetaDataInfoPanel(ProgressArea progress)
    {
        setLayout(new BorderLayout());
        mProgress = progress;
        mTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(mTable);
        scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void displayMetaData(Connection connection)
    {
        DefaultTableModel model = new DefaultTableModel();
        try
        {
            DatabaseMetaData meta = connection.getMetaData();
            int columnCount = 2;
            Object[] header = new Object[] {
                "Thing", "Value"
            };

            model.setColumnIdentifiers(header);

            Method[] methods = meta.getClass().getMethods();
            for (int i = 0; i < methods.length; i++)
            {
                Method method = methods[i];
                Class returnType = method.getReturnType();
                if (returnType == Boolean.TYPE ||
                    returnType == Integer.TYPE ||
                    returnType == String.class)
                {
                    String methodName = method.getName();
                    if (method.getParameterTypes().length == 0 &&
                        !"hashCode".equals(methodName) &&
                        !"toString".equals(methodName))
                    {
                        Object[] row = new Object[columnCount];
                        row[0] = methodName;
                        Object value;
                        try
                        {
                            value = method.invoke(meta, new Object[0]);
                        }
                        catch (IllegalAccessException e)
                        {
                            mProgress.appendProgress(e);
                            value = "Illegal Access: "+e.getMessage();
                        }
                        catch (IllegalArgumentException e)
                        {
                            mProgress.appendProgress(e);
                            value = "Illegal Argument: "+e.getMessage();
                        }
                        catch (InvocationTargetException e)
                        {
                            Throwable targetException = e.getTargetException();
                            String targetMessage = targetException.getMessage();
                            value = "Threw exception: "+
                                    targetException.getClass().getName()+
                                    (targetMessage!=null ? ": "+targetMessage:
                                     "");
                            if (targetException.getClass() !=
                                UnsupportedOperationException.class)
                            {
                                mProgress.appendProgress(targetException);
                            }
                        }
                        row[1] = value;
                        model.addRow(row);
                    }
                }
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
        mTable.setModel(model);
    }

}
