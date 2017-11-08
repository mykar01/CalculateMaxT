
package maxtcore;

import timeperiod.*;

/**
 * Defines objects that represent a cow.
 * @author "Michael Gallichan"
 */
public class Cow implements Comparable<Cow>
{
    // instance variables
    private final String id;
    
    // link variables
    private final Herd herd;
    private MilkTakings amMilkTakings;
    private MilkTakings pmMilkTakings;
    
    // constructor
    /**
     * Creates a new Cow object with the given value.
     * @param anId A unique ID for the cow
     * @param aHerd The herd the cow is part of
     */
    Cow (String anId, Herd aHerd)
    {
        id = anId;
        
        herd = aHerd;
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
     * Returns the daily milk yield of this cow.
     * @return the daily milk yield of the receiver
     */
    public int getDailyMilkYield()
    {
        return amMilkTakings.getMilkYield() + pmMilkTakings.getMilkYield();
    }
    
    /**
     * Returns a string representing this cow's ID.
     * @return a String object representing this Cow object
     */
    @Override
    public String toString()
    {
        return id;
    }
    
    /**
     * Returns true if o is a Cow object with id equal to those of the
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
        if (!(o instanceof Cow))
        {
            return false;
        }
        Cow f = (Cow) o;
        return (f.id.equals(id));
    }
    
    /**
     * Returns the hashCode of the Cow
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
     * Returns a negative integer if the receiver is alphabetically before aCow,
     * a positive integer if the receiver is alphabetically after aCow
     * and zero otherwise.
     * @param aCow the cow to be compared with the receiver
     * @return -1 if receiver is before aCow, 1 if it is after aCow, 0 otherwise
     */
    @Override
    public int compareTo(Cow aCow)
    {
        if (id.compareTo(aCow.id) < 0)
        {
            return -1;
        }
        if (id.compareTo(aCow.id) > 0)
        {
            return 1;
        }
        return 0;
    }
    
    // package protocol
    
    /**
     * Returns the herd this cow is part of.
     * @return the linked Cow object
     */
    Herd getHerd()
    {
        return herd;
    }
    
    /**
     * Returns the morning milk takings for this cow.
     * @return the linked AM Milk Taking object
     */
    MilkTakings getAmMilkTakings()
    {
        return amMilkTakings;
    }
    
    /**
     * Returns the evening milk takings for this cow.
     * @return the linked PM Milk Taking object
     */
    MilkTakings getPmMilkTakings()
    {
        return pmMilkTakings;
    }
    
    /**
     * Adds the milk takings for the appropriate milking session
     * @param aMilkingSession a milking session
     * @param aMilkTakings a Milk Takings object
     */
    void addMilkTakings(TimePeriod aMilkingSession, MilkTakings aMilkTakings)
    {
        if (aMilkingSession == TimePeriod.AM)  // if it's for an AM milking session...
        {
            amMilkTakings = aMilkTakings;  // .. add as AM milk takings,
        }
        else
        {
            pmMilkTakings = aMilkTakings;  // else, add as PM milk takings.
        }
    }
    
    /**
     * Removes the milk takings currently recorded for the appropriate
     * milking session.
     * @param aMilkingSession a milking session
     */
    void removeMilkTakings(TimePeriod aMilkingSession)
    {
        if (aMilkingSession == TimePeriod.AM)  // if AM selected..
        {
            amMilkTakings = null;  // .. remove AM MilkTakings,
        }
        else
        {
            pmMilkTakings = null;  // else, remove PM MilkTakings.
        }
    }
}
