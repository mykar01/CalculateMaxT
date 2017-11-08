
package milkingintervals;

/**
 * Provides two constants to represent the two milking interval options.
 * @author Michael Gallichan
 */
public enum MilkingIntervals
{
    /**
     * Represents the (8,16) milking interval
     */
    EIGHT,
    /**
     * Represents the (9,15) milking interval
     */
    NINE;
    
    /**
     * Returns the milking intervals as a String.
     * @return a String representation of the milking intervals
     */
    @Override
    public String toString()
    {
        switch (this)
        {
            case EIGHT: return "(8,16)";
            default: return "(9,15)";
        }
    }
    
    /**
     * Returns the first milking interval as an integer.
     * @return an integer representation of the first milking interval
     */
    public int toInt()
    {
        switch (this)
        {
            case EIGHT: return 8;
            default: return 9;
        }
    }
}
