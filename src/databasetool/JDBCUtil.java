package databasetool;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class JDBCUtil
{
    // Must be sorted!
    private static String[] sNotImplementedStates = {
        "IM001",
        "S1C00",
    };

    private static String[][] sStateToStringArray = {
        {"00000", "Success"},
        {"01000", "General warning"},
        {"01002", "Disconnect error"},
        {"01004", "Data truncated"},
        {"01006", "Privilege not revoked"},
        {"01S00", "Invalid connection string attribute"},
        {"01S01", "Error in row"},
        {"01S02", "Option value changed"},
        {"01S03", "No rows updated or deleted"},
        {"01S04", "More than one row updated or deleted"},
        {"07001", "Wrong number of parameters"},
        {"07002", "Mismatching parameters"},
        {"07003", "Cursor specification cannot be executed"},
        {"07004", "Missing parameters"},
        {"07006", "Restricted data type attribute violation"},
        {"07008", "Invalid descriptor count"},
        {"08000", "Connection exception"},
        {"08001", "Unable to connect to the data source"},
        {"08002", "Connection in use"},
        {"08003", "Connection not open"},
        {"08004", "Data source rejected establishment of connection"},
        {"08007", "Connection failure during transaction"},
        {"08900", "Server lookup failed"},
        {"08S01", "Communication link failure"},
        {"21000", "Cardinality violation"},
        {"21S01", "Insert value list does not match column list"},
        {"21S02", "Degree of derived table does not match column list"},
        {"22000", "Data exception"},
        {"22001", "String data, right truncation"},
        {"22003", "Numeric value out of range"},
        {"22005", "Error in assignment"},
        {"22008", "Datetime field overflow"},
        {"22012", "Division by zero"},
        {"22026", "String data, length mismatch"},
        {"23000", "Integrity constraint violation"},
        {"24000", "Invalid cursor state"},
        {"25000", "Invalid transaction state"},
        {"25S02", "Transaction is still active"},
        {"25S03", "Transaction has been rolled back"},
        {"26000", "Invalid SQL statement identifier"},
        {"28000", "Invalid authorization specification"},
        {"34000", "Invalid cursor name"},
        {"37000", "Syntax error or access violation"},
        {"3C000", "Duplicate cursor name"},
        {"40000", "Commit transaction resulted in rollback transaction"},
        {"40001", "Transaction rollback outcome unknown"},
        {"42000", "Syntax error or access rule violation"},
        {"44000", "WITH CHECK OPTION violation"},
        {"70100", "Operation aborted"},
        {"HZ010", "RDA error: Access control violation"},
        {"HZ020", "RDA error: Bad repetition count"},
        {"HZ080", "RDA error: Resource not available"},
        {"HZ090", "RDA error: Resource already open"},
        {"HZ100", "RDA error: Resource unknown"},
        {"HZ380", "RDA error: SQL usage violation"},
        {"IM001", "Driver does not support this function"},
        {"IM002", "Data source name not found and no default driver specified"},
        {"IM003", "Specified driver could not be loaded"},
        {"IM004", "Driver's AllocEnv failed"},
        {"IM005", "Driver's AllocConnect failed"},
        {"IM006", "Driver's SetConnectOption failed"},
        {"IM007", "No data source or driver specified, dialog prohibited"},
        {"IM008", "Dialog failed"},
        {"IM009", "Unable to load translation DLL"},
        {"IM010", "Data source name too long"},
        {"IM011", "Driver name too long"},
        {"IM012", "DRIVER keyword syntax error"},
        {"IM013", "Trace file error"},
        {"S0001", "Base table or view already exists"},
        {"S0002", "Base table not found"},
        {"S0011", "Index already exists"},
        {"S0012", "Index not found"},
        {"S0021", "Column already exists"},
        {"S0022", "Column not found"},
        {"S0023", "No default for column"},
        {"S1000", "General error"},
        {"S1001", "Storage allocation failure"},
        {"S1002", "Invalid column number"},
        {"S1003", "Program type out of range"},
        {"S1004", "SQL Data type out of range"},
        {"S1008", "Operation cancelled"},
        {"S1009", "Invalid argument value"},
        {"S1010", "Function sequence error"},
        {"S1011", "Operation invalid at this time"},
        {"S1012", "Invalid transaction operation code specified"},
        {"S1015", "No cursor name available"},
        {"S1090", "Invalid string or buffer length"},
        {"S1091", "Descriptor type out of range"},
        {"S1092", "Option type out of range"},
        {"S1093", "Invalid parameter number"},
        {"S1094", "Invalid scale value"},
        {"S1095", "Function type out of range"},
        {"S1096", "Information type out of range"},
        {"S1097", "Column type out of range"},
        {"S1098", "Scope type out of range"},
        {"S1099", "Nullable type out of range"},
        {"S1100", "Uniqueness option type out of range"},
        {"S1101", "Accuracy option type out of range"},
        {"S1103", "Direction option out of range"},
        {"S1104", "Invalid precision value"},
        {"S1105", "Invalid parameter type"},
        {"S1106", "Fetch type out of range"},
        {"S1107", "Row value out of range"},
        {"S1108", "Concurrency option out of range"},
        {"S1109", "Invalid cursor position"},
        {"S1110", "Invalid driver completion"},
        {"S1111", "Invalid bookmark value"},
        {"S1C00", "Driver not capable"},
        {"S1T00", "Timeout expired"},
    };

    private static Map sStateToString = new HashMap();

    static
    {
        for (int i = 0; i < sStateToStringArray.length; i++)
        {
            String[] stateString = sStateToStringArray[i];
            sStateToString.put(stateString[0], stateString[1]);
        }
    }

    private JDBCUtil()
    {

    }

    public static String sqlStateToString(SQLException e)
    {
        return sqlStateToString(e.getSQLState());
    }

    public static String sqlStateToString(String state)
    {
        String message = (String)sStateToString.get(state);
        if (message != null)
        {
            return message;
        }
        return "Unknown state " + state;
    }

    public boolean isNotImplementedError(SQLException e)
    {
        return Arrays.binarySearch(sNotImplementedStates, e.getSQLState()) > 0;
    }
}
