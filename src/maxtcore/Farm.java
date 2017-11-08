
package maxtcore;

import java.util.*;

/**
 * Defines objects that represent a physical farm.
 * @author Michael Gallichan
 */
public class Farm implements Comparable<Farm>
{
    // instance variables
    private final String id;
    private final String name;
    private final String location;
    
    // link variables
    private Collection<Herd> herds;
    
    // constructor
    /**
     * Creates a new Farm object with the given values.
     * @param anId A unique ID for the farm
     * @param aName A name for the farm
     * @param aLocation The location of the farm
     */
    Farm (String anId, String aName, String aLocation)
    {
        id = anId;
        name = aName;
        location = aLocation;
        
        herds = new TreeSet<>();
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
     * Returns the receiver's location.
     * @return location
     */
    public String getLocation()
    {
        return location;
    }
    
    /**
     * Returns a string representation of this farm's ID, name and location.
     * @return a String object representing the receiver
     */
    @Override
    public String toString()
    {
        return id + "; " + name + "; " + location;
    }
    
    // package protocol
    
    /**
     * Returns the herds on this farm.
     * @return a collection of all linked Herd objects
     */
    Collection<Herd> getHerds()
    {
        return herds;
    }
    
    /**
     * Adds the herd to this farm.
     * @param aHerd a herd
     */
    void addHerd(Herd aHerd)
    {
        herds.add(aHerd);
    }
    
    /**
     * Removes the herd from this farm.
     * @param aHerd a herd
     */
    void removeHerd(Herd aHerd)
    {
        herds.remove(aHerd);
    }
    
    /**
     * Returns true if o is a Farm object with id equal to those of the
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
        if (!(o instanceof Farm))
        {
            return false;
        }
        Farm f = (Farm) o;
        return (f.id.equals(id));
    }
    
    /**
     * Returns the hashCode of the Farm
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
     * Returns a negative integer if the receiver is alphabetically before aFarm,
     * a positive integer if the receiver is alphabetically after aFarm
     * and zero otherwise.
     * @param aFarm the farm to be compared with the receiver
     * @return -1 if receiver is before aFarm, 1 if it is after aFarm, 0 otherwise
     */
    @Override
    public int compareTo(Farm aFarm)
    {
        if (id.compareTo(aFarm.id) < 0)
        {
            return -1;
        }
        if (id.compareTo(aFarm.id) > 0)
        {
            return 1;
        }
        return 0;
    }
}
