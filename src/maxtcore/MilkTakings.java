
package maxtcore;

import timeperiod.*;

/**
 * Defines objects that represent the milk takings from a single cow in a
 * single milking session.
 * @author Michael Gallichan
 */
public class MilkTakings
{
    // instance variables
    private final TimePeriod milkingSession;
    private int milkYield;
    
    // link variables
    private final Cow cow;
    
    // constructor
    /**
     * Creates a new MilkTakings object with the given values.
     * @param aMilkingSession The milking session
     * @param aMilkYield The amount of milk obtained (rounded to the nearest 
     *        litre.
     */
    MilkTakings(TimePeriod aMilkingSession, int aMilkYield, Cow aCow)
    {
        milkingSession = aMilkingSession;
        milkYield = aMilkYield;
        
        cow = aCow;
    }
    
    // public protocol
    
    /**
     * Returns the receiver's milking session.
     * @return milkingSession
     */
    public TimePeriod getMilkingSession()
    {
        return milkingSession;
    }
    
    /**
     * Returns the receiver's milk yield.
     * @return milkYield
     */
    public int getMilkYield()
    {
        return milkYield;
    }
    
    /**
     * A string representation of this milk yield.
     * @return a String representing this Milk Takings object
     */
    @Override
    public String toString()
    {
        return milkingSession.toString() + " milk yield: " + milkYield;
    }
    
    // package protocol
    
    /**
     * Updates a milk yield to the given value.
     * @param aMilkYield a milk yield
     */
    void setMilkYield(int aMilkYield)
    {
        milkYield = aMilkYield;
    }
}
