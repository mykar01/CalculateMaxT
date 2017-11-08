
package maxtcore;

import java.util.*;
import milkingintervals.*;

/**
 * Defines objects that represent a herd of cows.
 * @author Michael Gallichan
 */
public class Herd implements Comparable<Herd>
{
    // instance variables
    private final String id;
    private final String name;
    private final MilkingIntervals milkingInterval;
    
    // link variables
    private final Farm farm;
    private Collection<Cow> cows;
    
    // constructor
    /**
     * Creates a new Herd object with the given values.
     * @param anId A unique ID for the herd
     * @param aName A name for the herd
     * @param aMilkingInterval The milking intervals of the herd
     * @param aFarm The farm the herd is on
     */
    Herd (String anId, String aName, MilkingIntervals aMilkingInterval, Farm aFarm)
    {
        id = anId;
        name = aName;
        milkingInterval = aMilkingInterval;
        
        farm = aFarm;
        cows = new TreeSet<>();
    }
    
    // public protocol
    /**
     * Returns the receiver's ID.
     * @return id
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * Returns the receiver's name.
     * @return name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the receiver's milking interval.
     * @return milkingInterval
     */
    public MilkingIntervals getMilkingIntervals()
    {
        return milkingInterval;
    }
    
    /**
     * Returns the number of cows in this herd
     * @return the number of Cow objects linked to this herd
     */
    public int getNumberOfCows()
    {
        return cows.size();
    }
    
    /**
     * Returns the average daily milk yield of the herd
     * @return the average daily milk yield rounded to the nearest litre
     */
    public int getAvgDailyMilkYield()
    {
        int total = 0;
        int count = 0;
        
        for (Cow eachCow : cows)
        {
            total = total + eachCow.getDailyMilkYield();
            count++;
        }
        
        int average;
        if (count == 0)
        {
            average = 0;
        }
        else
        {
            average = total / count;
        }
        
        return (average);
    }
    
    /**
     * Returns a string representation of this herd's ID, name and milking
     * intervals.
     * @return a String object representing the receiver
     */
    @Override
    public String toString()
    {
        return id + "; " + name + "; " + milkingInterval.toString();
    }
    
    /**
     * Returns true if o is a Herd object with id equal to those of the
     * receiver, otherwise return false.
     * @param o the object to be checked for equality with the receiver
     * @return true if the receiver and argument are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof Herd))
        {
            return false;
        }
        Herd f = (Herd) o;
        return (f.id.equals(id));
    }
    
    /**
     * Returns the hashCode of the Herd
     * @return hashCode
     */
    @Override
    public int hashCode()
    {
        int code = 17;
        code = 37*code + id.hashCode();
        return code;
    }
    
    /**
     * Returns a negative integer if the receiver is alphabetically before aHerd,
     * a positive integer if the receiver is alphabetically after aHerd
     * and zero otherwise.
     * @param aHerd the herd to be compared with the receiver
     * @return -1 if receiver is before aHerd, 1 if it is after aHerd, 0 otherwise
     */
    @Override
    public int compareTo(Herd aHerd)
    {
        if (id.compareTo(aHerd.id) < 0)
        {
            return -1;
        }
        if (id.compareTo(aHerd.id) > 0)
        {
            return 1;
        }
        return 0;
    }
    
    // package protocol
    
    /**
     * Returns the farm this herd is on.
     * @return the Farm object linked to the receiver
     */
    Farm getFarm()
    {
        return farm;
    }
    
    /**
     * Returns the cows in this herd.
     * @return a collection of all linked Cow objects
     */
    Collection<Cow> getCows()
    {
        return Collections.unmodifiableCollection(cows);
    }
    
    /**
     * Adds cow to this herd.
     * @param aCow a cow
     */
    void addCow(Cow aCow)
    {
        cows.add(aCow);
    }
    
    /**
     * Removes cow from this herd.
     * @param aCow a cow
     */
    void removeCow(Cow aCow)
    {
        cows.remove(aCow);
    }
    
    /**
     * Confirms whether milk yield data exists for all cows in the given herd.
     * @param aHerd a herd of cows
     * @return false if a null reference is found; true otherwise
     */
    boolean isHerdMilkingDataComplete()
    {
        for (Cow eachCow : cows)
        {
            if (eachCow.getAmMilkTakings() == null)
            {
                return false;
            }
            
            if (eachCow.getPmMilkTakings() == null)
            {
                return false;
            }
        }
        
        return true;
    }
}
