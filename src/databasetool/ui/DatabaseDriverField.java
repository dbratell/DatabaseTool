package databasetool.ui;

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.Color;
import java.util.prefs.Preferences;

public class DatabaseDriverField extends JTextField
{
    private static final Preferences USER_PREFS =
            Preferences.userNodeForPackage(DatabaseDriverField.class);
    private static final String DRIVER_PREF = "driver";

    private ProgressArea mProgress;

    public DatabaseDriverField(ProgressArea progress)
    {
        mProgress = progress;
        getDocument().addDocumentListener(getDatabaseDriverFieldListener());
        setText(USER_PREFS.get(DRIVER_PREF, ""));
    }

    private DocumentListener getDatabaseDriverFieldListener()
    {
        return new DocumentListener(){
            public void insertUpdate(DocumentEvent e)
            {
                reactToChange();
            }

            public void removeUpdate(DocumentEvent e)
            {
                reactToChange();
            }

            public void changedUpdate(DocumentEvent e)
            {
                reactToChange();
            }
            private void reactToChange()
            {
                String text = getText();
                if (text.length() > 0)
                {
                    try
                    {
                        Class.forName(text);
                    }
                    catch (ClassNotFoundException e)
                    {
                        setForeground(Color.RED); // XXX
                        mProgress.appendProgress("Class not found: "+
                                                 e.getMessage());
                        return;
                    }
                }
                setForeground(Color.BLACK); // XXX skin
                return;
            }
        };
    }

    public void saveContents()
    {
        USER_PREFS.put(DRIVER_PREF, getText());
    }


}
