package databasetool.ui;

import databasetool.DatabaseTool;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class StatusBar extends Box
{
    private JLabel mStatusText;
    private JProgressBar mProgressBar;

    public StatusBar()
    {
        super(BoxLayout.X_AXIS);
        mStatusText = new JLabel();
        add(mStatusText);
        mProgressBar = new JProgressBar();
        add(mProgressBar);
        add(new JLabel(DatabaseTool.NAME_AND_VERSION));

    }

    public void setProgress(int current, int max)
    {
        mProgressBar.setMaximum(max);
        mProgressBar.setValue(max);
    }

    public void enableProgress(boolean enable)
    {
        mProgressBar.setEnabled(enable);
    }

    public void setStatusText(String text)
    {
        mStatusText.setText(text);
    }
}
