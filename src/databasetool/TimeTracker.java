package databasetool;

public class TimeTracker
{
    private long mStartTime;
    private String mDescription;

    public TimeTracker(String description)
    {
        mStartTime = System.currentTimeMillis();
        mDescription = description;
    }

    public String toString()
    {
        long timeInMillis = System.currentTimeMillis() - mStartTime;
        if (timeInMillis > 800)
        {
            return (timeInMillis / 1000) + "." + (timeInMillis % 1000) / 100 +
                    " s for " + mDescription;
        }

        return timeInMillis + " ms for " + mDescription;
    }
}
