package databasetool.ui;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.util.prefs.Preferences;

/**
 * User: Bratell
 */
public class ConnectInfo extends JPanel
{
    private static final Preferences USER_PREFS =
            Preferences.userNodeForPackage(DatabaseDriverField.class);
    private static final String CONNECT_PREF = "connectTo";
    private static final String USER_PREF = "user";
    private static final String PASSWORD_PREF = "password";

    private JPasswordField mPasswordField;
    private JTextField mUserField;
    private JTextField mConnectField;

    public ConnectInfo()
    {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(new JLabel("Connect string"));
        mConnectField = new JTextField();
        add(mConnectField);
        add(new JLabel("User"));
        mUserField = new JTextField();
        add(mUserField);
        add(new JLabel("Password"));
        mPasswordField = new JPasswordField();
        add(mPasswordField);

        mConnectField.setText(USER_PREFS.get(CONNECT_PREF, ""));
        mUserField.setText(USER_PREFS.get(USER_PREF, ""));
        mPasswordField.setText(USER_PREFS.get(PASSWORD_PREF, "")); // XXX obfuscate
    }

    public String getConnectString()
    {
        return mConnectField.getText();
    }
    
    public String getUser()
    {
        return mUserField.getText();
    }

    public String getPassword()
    {
        return new String(mPasswordField.getPassword());
    }

    public void saveContents()
    {
        USER_PREFS.put(CONNECT_PREF, getConnectString());
        USER_PREFS.put(USER_PREF, getUser());
        USER_PREFS.put(PASSWORD_PREF, getPassword());
    }
}
