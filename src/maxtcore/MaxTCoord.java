
package maxtcore;

import java.util.*;
import timeperiod.*;
import milkingintervals.*;
import farmingresources.*;

/**
 * Coordinating object for the MaxT core system
 * @author Michael Gallichan
 */
public class MaxTCoord
{
    // link variables
    private Collection<Farm> farms;
    private YieldTable yieldTable;
    private final MaxTTable maxTTable;
    
    // constructor
    /**
     * Creates a new coordinating object
     */
    private MaxTCoord()
    {
        farms = new TreeSet<>();
        yieldTable = new YieldTable();
        maxTTable = MaxTTable.getMaxTTable();
    }
    
    // public protocol
    
    /**
     * Returns the farms in the system
     * @return an unmodifiable collection of all the linked Farm objects
     */
    public Collection<Farm> getFarms()
    {
        return Collections.unmodifiableCollection(farms);
    }
    
    /**
     * Returns the herds on the given farm.
     * @param aFarm a farm
     * @return an unmodifiable collection of all the Herd objects linked to 
     * aFarm
     */
    public Collection<Herd> getHerds(Farm aFarm)
    {
        return Collections.unmodifiableCollection(aFarm.getHerds());
    }
    
    /**
     * Returns the cows in the given herd.
     * @param aHerd a herd
     * @return an unmodifiable collection of all the Cow objects linked to aHerd
     */
    public Collection<Cow> getCows(Herd aHerd)
    {
        return Collections.unmodifiableCollection(aHerd.getCows());
    }
    
    /**
     * Returns the milk takings for the given cow.
     * @param aCow a cow
     * @return a map consisting of milking sessions as keys and the
     * corresponding Milk Takings objects as values.
     */
    public Map<TimePeriod, MilkTakings> getMilkTakings(Cow aCow)
    {
        Map<TimePeriod, MilkTakings> milkTakings = new TreeMap<>();
        
        MilkTakings amMilkTakings = aCow.getAmMilkTakings();
        if (amMilkTakings != null) // add AM milk takings, if exists
        {
            milkTakings.put(TimePeriod.AM, amMilkTakings);
        }
        
        MilkTakings pmMilkTakings = aCow.getPmMilkTakings();
        if (pmMilkTakings != null)  // add PM milk takings, if exists
        {
            milkTakings.put(TimePeriod.PM, pmMilkTakings);
        }
        
        return Collections.unmodifiableMap(milkTakings);
    }
    
    /**
     * Adds a farm to the system with the given attributes.
     * @param anId a unique ID
     * @param aName a name
     * @param aLocation a location
     * 
     * @throws IllegalArgumentException if a farm already exists with the given
     * ID.
     */
    public void addFarm(String anId, String aName, String aLocation) throws IllegalArgumentException
    {
        boolean idExists = false;
        for (Farm eachFarm : farms)
        {
            if (anId.equals(eachFarm.getId()))  // Is farm id unique?
            {
                idExists = true;
                break;
            }
        }
        
        if (!idExists)  // only add new farm if id is unique
        {
            Farm newFarm = new Farm(anId, aName, aLocation);
            farms.add(newFarm);
        }
        else
        {
            throw new IllegalArgumentException("The supplied Farm ID is already in use.");
        }
    }
    
    /**
     * Removes the given farm from the system.
     * @param aFarm a farm
     * 
     * @throws IllegalArgumentException if Herd objects are linked to the given
     * Farm object.
     */
    public void deleteFarm(Farm aFarm) throws IllegalArgumentException
    {
        if (aFarm.getHerds().isEmpty())  // if farm has no linked herds..
        {
            farms.remove(aFarm);  // .. remove all links to aFarm to delete it,
        }
        else
        {
            throw new IllegalArgumentException("A farm cannot be deleted if herds are attached to it.");
        }
    }
    
    /**
     * Adds a herd to the given farm
     * @param anId a unique ID
     * @param aName a name
     * @param aMilkingInterval a milking interval
     * @param aFarm a farm
     * 
     * @throws IllegalArgumentException if a Herd object already linked to the
     * given Farm object has the given ID.
     */
    public void addHerd(String anId, String aName, MilkingIntervals aMilkingInterval, Farm aFarm) throws IllegalArgumentException
    {
        boolean idExists = false;
        Collection<Herd> herdList = aFarm.getHerds();
        for (Herd eachHerd : herdList)
        {
            if (anId.equals(eachHerd.getId()))  // Is id unique in aFarm?
            {
                idExists = true;
                break;
            }
        }
        
        if (!idExists)  // only add new Herd if id is unique in aFarm
        {
            Herd newHerd = new Herd(anId, aName, aMilkingInterval, aFarm);
            aFarm.addHerd(newHerd);
        }
        else
        {
            throw new IllegalArgumentException("The supplied Herd ID is already in use.");
        }
    }
    
    /**
     * Removes the given herd from the system.
     * @param aHerd a herd
     * 
     * @throws IllegalArgumentException if Cow objects are linked to the given
     * Herd object
     */
    public void deleteHerd(Herd aHerd) throws IllegalArgumentException
    {
        if (aHerd.getCows().isEmpty())
        {
            aHerd.getFarm().removeHerd(aHerd);
        }
        else
        {
            throw new IllegalArgumentException("A herd cannot be deleted if cows are associated with it.");
        }
    }
    
    /**
     * Adds a cow to the given herd.
     * @param anId a unique ID
     * @param aHerd a herd
     * 
     * @throws IllegalArgumentException if a Cow object already linked to the 
     * given Herd object has the given ID.
     */
    public void addCow(String anId, Herd aHerd) throws IllegalArgumentException
    {
        boolean idExists = false;
        Collection<Cow> cowList = aHerd.getCows();
        for (Cow eachCow : cowList)
        {
            if (anId.equals(eachCow.getId()))  // Is id unique in aHerd?
            {
                idExists = true;
                break;
            }
        }
        
        if (!idExists)  // only add new Cow if id is unique in aHerd
        {
            Cow newCow = new Cow(anId, aHerd);
            aHerd.addCow(newCow);
        }
        else
        {
            throw new IllegalArgumentException("The supplied Cow ID is already in use.");
        }
    }
    
    /**
     * Removes the given cow from the system.
     * @param aCow a cow
     */
    public void deleteCow(Cow aCow)
    {
        aCow.getHerd().removeCow(aCow);
    }
    
    /**
     * Adds a record of milk takings from the given cow.
     * @param aMilkingSession a milking session
     * @param aMilkYield a milk yield
     * @param aCow a cow
     * 
     * @throws IllegalArgumentException if a MilkTakings object is already linked
     * to aCow for aMilkingSession
     */
    public void addMilkTakings(TimePeriod aMilkingSession, int aMilkYield, Cow aCow) throws IllegalArgumentException
    {
        boolean takingsExist = false;
        if (aMilkingSession == TimePeriod.AM)  // if takings for AM milking session..
        {
            if (aCow.getAmMilkTakings() != null)  // .. and cow already has AM milk takings..
            {
                takingsExist = true;  // .. throw exception,
            }
        }
        else                         // OR, if takings for PM milking session..
        {
            if (aCow.getPmMilkTakings() != null)  // .. and cow already has PM milk takings..
            {
                takingsExist = true;  // .. throw exception.
            }
        }
        
        if (!takingsExist)
        {
            MilkTakings newMilkTakings = new MilkTakings(aMilkingSession, aMilkYield, aCow);
            aCow.addMilkTakings(aMilkingSession, newMilkTakings);
            
        }
        else
        {
            throw new IllegalArgumentException("Milk takings for this cow for this milking session have already been recorded.");
        }
    }
    
    /**
     * Updates the given milk takings with the given milk yield value.
     * @param aMilkTakings a milk takings
     * @param aMilkYield a milk yield
     */
    public void updateMilkTakings(MilkTakings aMilkTakings, int aMilkYield)
    {
        aMilkTakings.setMilkYield(aMilkYield);
    }
    
    /**
     * Removes the milk takings for the given milking session from the given cow.
     * @param aMilkingSession a milking session
     * @param aCow a cow
     */
    public void deleteMilkTakings(TimePeriod aMilkingSession, Cow aCow)
    {
        aCow.removeMilkTakings(aMilkingSession);
    }
    
    /**
     * Returns a coordinating object in the default state.
     * @return a MaxTCoord object
     */
    public static MaxTCoord getMaxT()
    {
        MaxTCoord maxT = new MaxTCoord();
        
        return maxT;
    }
    
    /**
     * Returns a read-only view of the Yield Table.
     * @return an unmodifiable map of the Yield Table data
     */
    public Map<String, Integer> getYieldTable()
    {
        Map<String, Integer> theYieldTable = yieldTable.getYieldTable();
        return Collections.unmodifiableMap(theYieldTable);
    }
    
    /**
     * Returns the Yield Table value for the given getMapKey (made up of the
 average daily milk yield, milking interval and milking session.
     * @param mapKey concatenated data constituting the key of the Yield Table map
     * @return the corresponding yield value as an integer
     */
    public int getYieldTableValue(String mapKey)
    {
        return yieldTable.getValue(mapKey);
    }
    
    /**
     * Replaces the yield value for the given average daily yield, milking
     * interval and milking session values with the given yield value.
     * @param mapKey the concatenated string that constitutes the Yield Table map key
     * @param aYieldValue an expected yield for the milking session
     */
    public void updateYieldValue(String mapKey, int aYieldValue)
    {
        yieldTable.updateYieldValue(mapKey, aYieldValue);
    }
    
    /**
     * Extracts the milk yield value for the given milking session from the
     * Yield Value table according to the given herd's average daily milk yield
     * and milking intervals.
     * @param aHerd a herd of cows
     * @param aMilkingSession a milking session
     * @return the average yield per cow as an integer
     */
    public int getAvgYieldPerCow(Herd aHerd, TimePeriod aMilkingSession)
    {
        int avgDailyMilkYield = aHerd.getAvgDailyMilkYield();
        MilkingIntervals milkingIntervals = aHerd.getMilkingIntervals();
        
        return yieldTable.getValue(yieldTable.getMapKey(avgDailyMilkYield, 
                                                     milkingIntervals, 
                                                     aMilkingSession));
    }
    
    /**
     * Extracts the MaxT value for the given average yield per cow.
     * @param anAvgYieldPerCow an average milk yield per cow for a herd
     * @return the MaxT value
     */
    public String getMaxTValue(int anAvgYieldPerCow)
    {
        return maxTTable.getMaxTValue(anAvgYieldPerCow);
    }
    
    /**
     * Returns a string representation of all farms.
     * @return a String object representing the receiver
     */
    @Override
    public String toString()
    {
        return farms.toString();
    }
    
    /**
     * Confirms whether milk yield data exists for all cows in the given herd.
     * @param aHerd a herd of cows
     * @return false if a null reference is found; true otherwise
     */
    public boolean isHerdMilkingDataComplete(Herd aHerd)
    {
        return aHerd.isHerdMilkingDataComplete();
    }
    
    /**
     * Checks if the yield table data is completely filled out by checking for
     * a zero value.
     * @return false if a zero value is found, true otherwise
     */
    public boolean isYieldTableComplete()
    {
        return yieldTable.isYieldTableComplete();
    }
}
