package databasetool.ui;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.io.StringWriter;
import java.io.PrintWriter;

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

    public void appendProgress(Exception e)
    {
        StringWriter sw = new StringWriter(400);
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        appendProgress(sw.toString());
    }
}
