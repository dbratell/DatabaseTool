package databasetool.ui;

import databasetool.JDBCUtil;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.sql.SQLException;

public class ProgressArea extends JScrollPane
{
    private JTextArea mTextArea;

    public ProgressArea()
    {
        mTextArea = new JTextArea();
        super.setViewportView(mTextArea);
    }

    public void appendProgress(String text)
    {
        ensureLineBreak();
        mTextArea.append(text);
    }

    private void ensureLineBreak()
    {
        String currentText = mTextArea.getText();
        int textLength = currentText.length();
        if (textLength > 0)
        {
            char lastChar = currentText.charAt(textLength-1);
            if (lastChar != '\n' && lastChar != '\r')
            {
                mTextArea.append("\n");
            }
        }
    }

    public void appendProgress(Throwable e)
    {
        StringWriter sw = new StringWriter(400);
        PrintWriter pw = new PrintWriter(sw);
        pw.println("SQL Error Code: ");
        e.printStackTrace(pw);
        appendProgress(sw.toString());
    }

    public void appendProgress(SQLException e)
    {
        StringWriter sw = new StringWriter(400);
        PrintWriter pw = new PrintWriter(sw);
        String sqlState = e.getSQLState();
        pw.println("SQL Error Code: " + sqlState + " - " +
                   JDBCUtil.sqlStateToString(sqlState));
        e.printStackTrace(pw);
        appendProgress(sw.toString());
    }
}
