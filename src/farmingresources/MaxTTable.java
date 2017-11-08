package farmingresources;

import java.util.*;

/**
 * Stores MaxT time values (min:sec) according to yield per milking (in litres)
 * @author Michael Gallichan
 */
public class MaxTTable
{
    // class variables
    private final Map<Integer, String> maxTValues;
    
    // constructor
    
    /**
     * Creates a new MaxTTable object with the preset values.
     */
    MaxTTable()
    {
        maxTValues = new TreeMap<>();
        
        maxTValues.put(7, "04:51");
        maxTValues.put(8, "05:20");
        maxTValues.put(9, "05:48");
        maxTValues.put(10, "06:15");
        maxTValues.put(11, "06:42");
        maxTValues.put(12, "07:07");
        maxTValues.put(13, "07:32");
        maxTValues.put(14, "07:57");
        maxTValues.put(15, "08:21");
        maxTValues.put(16, "08:44");
    }
    
    // public protocol

    /**
     * Returns a MaxTTable in the default state
     * @return a MaxT Table
     */
    public static MaxTTable getMaxTTable()
    {
        MaxTTable maxTTable = new MaxTTable();
        
        return maxTTable;
    }
    
    public String getMaxTValue(int aYieldPerMilking)
    {
        return maxTValues.get(aYieldPerMilking);
    }
}
