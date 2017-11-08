
package timeperiod;

/**
 * Provides two constants to represent morning (AM) and evening (PM) periods
 * of a day.
 * @author Michael Gallichan
 */
public enum TimePeriod
{
    /**
     * Represents morning period of day.
     */
    AM,
    /**
     * Represents evening period of day.
     */
    PM;
    
    /**
     * Returns the period of day as a String.
     * @return a String representing the period of day
     */
    @Override
    public String toString()
    {
        switch (this)
        {
            case AM: return "AM";
            default: return "PM";
        }
    }
}
