package maxtcore;

import java.util.*;
import milkingintervals.*;
import timeperiod.*;

/**
 * Stores expected milk yield values according to milking session, milking
 * intervals and average daily milk yield
 * @author Michael Gallichan
 */
public class YieldTable
{
    // instance variables
    private Map<String, Integer> yieldTable;
    
    // constructor
    
    /**
     * Creates a new yield table object and sets up the default keys with 0
     * values.
     */
    YieldTable()
    {
        yieldTable = new HashMap<>();
        
        // array of required values to be used as keys
        int yields[] = {20,21,22,23,24,25};
        
        // create keys for all required values
        for (int i=0; i<yields.length; i++)
        {
            for (MilkingIntervals interval : MilkingIntervals.values())
            {
                for (TimePeriod session : TimePeriod.values())
                {
                    yieldTable.put(yields[i] + "," 
                                 + interval.toString() + "," 
                                 + session.toString(),
                                   0);
                }
            }
        }
    }
    
    // public protocol
    
    /**
     * Returns the Yield Table value corresponding to the given map key.
     * @param mapKey the concatenated string that constitutes the Yield Table map key
     * @return the appropriate expected milk yield value as an integer
     */
    public int getValue(String mapKey)
    {
        if (yieldTable.containsKey(mapKey))
        {
            return yieldTable.get(mapKey);
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Generates a string for use as a map key in yieldTable.
     * @param anAvgDailyYield an average daily yield value
     * @param aMilkingInterval a milking interval
     * @param aMilkingSession a milking session
     * @return a String object concatenating the given arguments as 
     * comma-separated values
     */
    public String getMapKey(int anAvgDailyYield, MilkingIntervals aMilkingInterval,
                          TimePeriod aMilkingSession)
    {
        return anAvgDailyYield + ","
             + aMilkingInterval.toString() + ","
             + aMilkingSession.toString();
    }
    
    /**
     * A string representing the yield table
     * @return A String object that constructs a yield table in a fixed-width
     * font
     */
    @Override
    public String toString()
    {
        return yieldTable.toString();
    }
    
    // package protocol
    
    /**
     * Returns the Yield Table data.
     * @return a map of the Yield Table data
     */
    Map<String, Integer> getYieldTable()
    {
        return yieldTable;
    }
    
    /**
     * Puts the given yield value (aYieldValue) into the map for the key made
     * up of the first three arguments.
     * @param mapKey the concatenated string that constitutes the Yield Table map key
     * @param aYieldValue the yield expected for the other given criteria
     */
    void updateYieldValue(String mapKey, int aYieldValue)
    {
        yieldTable.put(mapKey, aYieldValue);
    }
    
    /**
     * Checks if the yield table data is completely filled out by checking for
     * a zero value.
     * @return false if a zero value is found, true otherwise
     */
    public boolean isYieldTableComplete()
    {
        Collection<Integer> values = yieldTable.values();
        for (int eachValue : values)
        {
            if (eachValue == 0)
            {
                return false;
            }
        }
        
        return true;
    }
}
