
package maxtgui;

import java.awt.Color;
import maxtcore.*;
import timeperiod.*;
import milkingintervals.*;
import java.util.*;

/**
 * Provides a Graphical User Interface for the maxtcore package.
 * @author MYKAR
 */
public class MaxTGui extends javax.swing.JFrame
{
    // attributes
    private final MaxTCoord maxT;  // coordinating object
    
    private MilkingIntervals milkingInterval = null;  // for radio buttons on 'Add Herd' screen
    private TimePeriod milkingSession = null;  // for radio buttons on 'Add Milk Takings' screen
    
    private final Collection<String> emptyList = new HashSet<>();  // for clearing lists
    
    // constructor
    
    /**
     * Creates new form MaxTGui
     */
    public MaxTGui()
    {
        // get reference to coordinating object
        maxT = MaxTCoord.getMaxT();
        
        initComponents();
    }
    
    // -------------------------------------------------------------------------
    // 'Overall Statistics' screen methods                    OVERALL STATISTICS
    
    private void updateStatDisplay()
    {
        // if a Farm is selected in the list..
        if ((Farm)main_FarmList.getSelectedValue() != null)
        {
            main_statDisplay.setText("");  // clear stat display
            Farm farm = (Farm)main_FarmList.getSelectedValue();
            ArrayList<String> line = new ArrayList<>();
        
            // create headers
            line.add("           AM MaxT   PM MaxT    Cow    Avg per cow");
            line.add("Herd      (min:sec) (min:sec)   qty     (AM, PM)");
            line.add("---------+---------+---------+-------+-------------");

            // generate lines with values
            if (!maxT.isYieldTableComplete())
            {
                line.add(String.format("%n MaxT values cannot be calculated until%n"
                                     + " Yield Table data is complete "));
            }
            else
            {
                Collection<Herd> herds = maxT.getHerds(farm); 
                if (!herds.isEmpty())
                {
                    String herdId;
                    String amMaxT;
                    String pmMaxT;
                    int cowQty;
                    int avgPerCowAm;
                    int avgPerCowPm;

                    for (Herd eachHerd : herds)
                    {
                        herdId = eachHerd.getId();
                        amMaxT = null;
                        pmMaxT = null;
                        cowQty = eachHerd.getNumberOfCows();
                        avgPerCowAm = 0;
                        avgPerCowPm = 0;

                        if (cowQty > 0 && maxT.isHerdMilkingDataComplete(eachHerd))
                        {
                            avgPerCowAm = maxT.getAvgYieldPerCow(eachHerd, TimePeriod.AM);
                            avgPerCowPm = maxT.getAvgYieldPerCow(eachHerd, TimePeriod.PM);
                            
                            amMaxT = maxT.getMaxTValue(avgPerCowAm);
                            if (amMaxT == null)
                            {
                                amMaxT = "XX:XX";
                            }
                            
                            pmMaxT = maxT.getMaxTValue(avgPerCowPm);
                            if (pmMaxT == null)
                            {
                                pmMaxT = "XX:XX";
                            }
                        }

                        if (amMaxT == null)
                        {
                            amMaxT = "";
                        }

                        if (pmMaxT == null)
                        {
                            pmMaxT = "";
                        }

                        if (avgPerCowAm == 0 || avgPerCowPm == 0)
                        {
                            line.add(String.format("%-8s :  %5s  :  %5s  :  %3d  :",
                                                   herdId, amMaxT, pmMaxT,
                                                   cowQty, avgPerCowAm, avgPerCowPm));
                        }
                        else
                        {                  
                            line.add(String.format("%-8s :  %5s  :  %5s  :  %3d  :  %2dL, %2dL",
                                                   herdId, amMaxT, pmMaxT,
                                                   cowQty, avgPerCowAm, avgPerCowPm));
                        }
                    }
                }
            }
            
            for (String eachLine : line)
            {
                main_statDisplay.append(eachLine);
                main_statDisplay.append("\n");
            }
        }
    }
    
    // -------------------------------------------------------------------------
    // 'Add' screen methods                                                  ADD  
    
    // --------------------------------------------------------------------
    // 'Add Farm' screen specific methods                          ADD FARM
    
    private void addFarm()
    {
        // validate entries
        String id = addFarm_IdField.getText();
        if (!validString(id, "ID"))
        {
            selectFieldText(addFarm_IdField);
            return;
        }
        
        Collection<Farm> farms = maxT.getFarms();
        for (Farm eachFarm : farms)
        {
            if (id.equals(eachFarm.getId()))  // Is farm id unique?
            {
                reportError("ID already exists. Enter a unique ID.");
                selectFieldText(addFarm_IdField);
                return;
            }
        }
        
        String name = addFarm_NameField.getText();
        if (!validString(name, "Name"))
        {
            selectFieldText(addFarm_NameField);
            return;
        }
        String location = addFarm_LocationField.getText();
        if (!validString(location, "Location"))
        {
            selectFieldText(addFarm_LocationField);
            return;
        }
        
        // perform add
        maxT.addFarm(id, name, location);
        reportSuccess("Farm '" + id + "' added.");
        clearAddFarm();  // set for next entry
        updateFarmLists();  // update other tabs
    }
    
    private void clearAddFarm()
    {
        clearField(addFarm_IdField);
        clearField(addFarm_NameField);
        clearField(addFarm_LocationField);
        addFarm_IdField.requestFocusInWindow();
    }
    
    // --------------------------------------------------------------------
    // 'Add Herd' specific methods                                 ADD HERD
    
    private void addHerd()
    {
        // validate entries
        if (addHerd_FarmList.isSelectionEmpty())
        {
            reportError("Select farm for herd.");
            return;
        }
        
        String id = addHerd_IdField.getText();
        if (!validString(id, "ID"))
        {
            selectFieldText(addHerd_IdField);
            return;
        }
        
        if (id.length() > 8)
        {
            reportError("Herd ID must be no more than 8 characters.");
            selectFieldText(addHerd_IdField);
            return;
        }
        
        Collection<Herd> herds = maxT.getHerds((Farm)addHerd_FarmList.getSelectedValue());
        for (Herd eachHerd : herds)
        {
            if (id.equals(eachHerd.getId()))  // Is herd id unique?
            {
                reportError("ID already exists. Enter a unique ID.");
                selectFieldText(addHerd_IdField);
                return;
            }
        }
        
        String name = addHerd_NameField.getText();
        if (!validString(name, "Name"))
        {
            selectFieldText(addHerd_NameField);
            return;
        }
        
        if (milkingInterval == null)
        {
            reportError("Select milking intervals for herd.");
            return;
        }
        
        // perform add
        Farm farm = (Farm)addHerd_FarmList.getSelectedValue();
        maxT.addHerd(id, name, milkingInterval, farm);
        reportSuccess("Herd '" + id + "' added.");
        nextAddHerd();  // set for next entry
        updateHerdLists("addHerd");  // update other tabs
    }
    
    private void nextAddHerd()
    {
        clearField(addHerd_IdField);
        clearField(addHerd_NameField);
        
        // reset Milking Interval radio button selection
        addHerd_MilkingIntervalsGroup.clearSelection();
        milkingInterval = null;
        
        addHerd_IdField.requestFocusInWindow();
    }
    
    // --------------------------------------------------------------------
    // 'Add Cow' specific methods                                   ADD COW
    
    private void addCow()
    {
        // validate entries
        if (addCow_FarmList.isSelectionEmpty())
        {
            reportError("Select farm for cow.");
            return;
        }
        
        if (addCow_HerdList.isSelectionEmpty())
        {
            reportError("Select herd for cow.");
            return;
        }
        
        String id = addCow_IdField.getText();
        if (!validString(id, "ID"))
        {
            selectFieldText(addCow_IdField);
            return;
        }
        
        Collection<Cow> cows = maxT.getCows((Herd)addCow_HerdList.getSelectedValue());
        for (Cow eachCow : cows)
        {
            if (id.equals(eachCow.getId()))  // Is cow id unique?
            {
                reportError("ID already exists. Enter a unique ID.");
                selectFieldText(addCow_IdField);
                return;
            }
        }
        
        // perform add
        Herd herd = (Herd)addCow_HerdList.getSelectedValue();
        maxT.addCow(id, herd);
        reportSuccess("Cow '" + id + "' added.");
        nextAddCow();  // set for next entry
        updateCowLists("addCow");  // update other tabs
    }
    
    private void nextAddCow()
    {
        setFieldForNextEntry(addCow_IdField);
    }
    
    // --------------------------------------------------------------------
    // 'Add Milk Takings' specific methods                 ADD MILK TAKINGS
    
    private void addMilkTakings()
    {
        // validate entries
        if (addMilkTakings_FarmList.isSelectionEmpty())
        {
            reportError("Select farm for milk takings.");
            return;
        }
        
        if (addMilkTakings_HerdList.isSelectionEmpty())
        {
            reportError("Select herd for milk takings.");
            return;
        }
        
        if (addMilkTakings_CowList.isSelectionEmpty())
        {
            reportError("Select cow for milk takings.");
            return;
        }
        
        if (milkingSession == null)
        {
            reportError("Select milking session for milk takings.");
            return;
        }
        
        Cow cow = (Cow)addMilkTakings_CowList.getSelectedValue();
        Map<TimePeriod, MilkTakings> milkTakings = maxT.getMilkTakings(cow);
        if (milkTakings.containsKey(milkingSession))  // if milk takings already exist for selected milking session...
        {
            reportError("Milk takings already exist for this milking session. Update milk takings instead.");
            return;
        }
        
        String milkYield = addMilkTakings_YieldField.getText();
        if (!validInt(milkYield, "milk yield"))  // if yield entry not an int..
        {
            selectFieldText(addMilkTakings_YieldField);
            return;
        }
        
        // perform add
        maxT.addMilkTakings(milkingSession,Integer.parseInt(milkYield),cow);
        String session = milkingSession.toString();
        reportSuccess(session + " Milk Takings added.");
        nextAddMilkTakings();  // set for next entry
        updateMilkingSessionLists("addMilkTakings");  // update other tabs
    }
    
    private void nextAddMilkTakings()
    {
        clearField(addMilkTakings_YieldField);
        
        // reset Milking Session radio button selection
        addMilkTakings_MilkingSessionGroup.clearSelection();
        milkingSession = null;
    }
    
    // -------------------------------------------------------------------------
    // 'Update' screen methods                                            UPDATE
    
    // --------------------------------------------------------------------
    // 'Update Milk Takings' specific methods          UPDATE MILK TAKINGS
    
    private void updateMilkTakings()
    {
        // validate entries
        if (updateMilkTakings_FarmList.isSelectionEmpty())
        {
            reportError("Select a farm.");
            return;
        }
        
        if (updateMilkTakings_HerdList.isSelectionEmpty())
        {
            reportError("Select a herd.");
            return;
        }
        
        if (updateMilkTakings_CowList.isSelectionEmpty())
        {
            reportError("Select a cow.");
            return;
        }
        
        if (updateMilkTakings_MilkingSessionList.isSelectionEmpty())
        {
            reportError("Select a milking session to update.");
            return;
        }
        
        Cow cow = (Cow)updateMilkTakings_CowList.getSelectedValue();
        Map<TimePeriod, MilkTakings> existingMilkTakings = maxT.getMilkTakings(cow);
        TimePeriod session = (TimePeriod)updateMilkTakings_MilkingSessionList.getSelectedValue();
        MilkTakings theMilkTakings = existingMilkTakings.get(session);  // get the Milk Takings object to be updated
        String milkYield = updateMilkTakings_NewYieldField.getText();
        if (!validInt(milkYield, "milk yield"))  // if yield entry not an int..
        {
            selectFieldText(updateMilkTakings_NewYieldField);
            return;
        }
        
        int newMilkYield = Integer.parseInt(milkYield);
        int existingMilkYield = existingMilkTakings.get((TimePeriod)updateMilkTakings_MilkingSessionList.getSelectedValue()).getMilkYield();
        if (newMilkYield == existingMilkYield)
        {
            reportError("Milk Yield values are the same. No update necessary.");
            selectFieldText(updateMilkTakings_NewYieldField);
            return;
        }
        
        // perform update
        maxT.updateMilkTakings(theMilkTakings, newMilkYield);
        reportSuccess("Milk yield updated.");
        updateYield(updateMilkTakings_ExistingYieldTxt, theMilkTakings.getMilkYield());
        clearField(updateMilkTakings_NewYieldField);
    }
    
    // -------------------------------------------------------------------------
    // 'Delete' screen methods                                            DELETE
    
    // --------------------------------------------------------------------
    // 'Delete Farm' specific methods                           DELETE FARM
    
    private void deleteFarm()
    {
        // validate entries
        if (deleteFarm_FarmList.isSelectionEmpty())
        {
            reportError("Select farm to delete.");
            return;
        }
        
        Farm farm = (Farm)deleteFarm_FarmList.getSelectedValue();
        String farmId = farm.getId();
        if (!maxT.getHerds(farm).isEmpty())  // if farm has linked herds..
        {
            reportError("Farm '" + farmId + "' has herds. Delete herds before deleting farm.");
            return;
        }
        
        // perform delete
        maxT.deleteFarm(farm);
        reportSuccess("Farm '" + farmId + "' deleted.");
        updateFarmLists();  // update other tabs
    }
    
    // --------------------------------------------------------------------
    // 'Delete Herd' specific methods                           DELETE HERD
    
    private void deleteHerd()
    {
        // validate entries
        if (deleteHerd_FarmList.isSelectionEmpty())
        {
            reportError("Select a farm.");
            return;
        }
        
        if (deleteHerd_HerdList.isSelectionEmpty())
        {
            reportError("Select herd to delete.");
            return;
        }
        
        Herd herd = (Herd)deleteHerd_HerdList.getSelectedValue();
        String herdId = herd.getId();
        if (!maxT.getCows(herd).isEmpty())  // if herd has linked cows..
        {
            reportError("Herd '" + herdId + "' has cows. Delete cows before deleting herd.");
            return;
        }
        
        // perform delete
        maxT.deleteHerd(herd);
        reportSuccess("Herd '" + herdId + "' deleted.");
        // update current tab
        updateHerdList(deleteHerd_HerdList, (Farm)deleteHerd_FarmList.getSelectedValue());
        updateHerdLists("deleteHerd");  // update other tabs
    }
    
    // --------------------------------------------------------------------
    // 'Delete Cow' specific methods                             DELETE COW
    
    private void deleteCow()
    {
        // validate entries
        if (deleteCow_FarmList.isSelectionEmpty())
        {
            reportError("Select a farm.");
            return;
        }
        
        if (deleteCow_HerdList.isSelectionEmpty())
        {
            reportError("Select a herd.");
            return;
        }
        
        if (deleteCow_CowList.isSelectionEmpty())
        {
            reportError("Select a cow to delete.");
            return;
        }
        
        // perform delete
        Cow cow = (Cow)deleteCow_CowList.getSelectedValue();
        String id = cow.getId();
        maxT.deleteCow(cow);
        reportSuccess("Cow '" + id + "' deleted.");
        // update current tab
        updateCowList(deleteCow_CowList, (Herd)deleteCow_HerdList.getSelectedValue());
        updateCowLists("deleteCow");  // update other tab
    }
    
    // --------------------------------------------------------------------
    // 'Delete Milk Takings' specific methods           DELETE MILK TAKINGS
    
    private void deleteMilkTakings()
    {
        // validate entries
        if (deleteMilkTakings_FarmList.isSelectionEmpty())
        {
            reportError("Select a farm.");
            return;
        }
        
        if (deleteMilkTakings_HerdList.isSelectionEmpty())
        {
            reportError("Select a herd.");
            return;
        }
        
        if (deleteMilkTakings_CowList.isSelectionEmpty())
        {
            reportError("Select a cow.");
            return;
        }
        
        if (deleteMilkTakings_MilkingSessionList.isSelectionEmpty())
        {
            reportError("Select a milking session to delete.");
            return;
        }
        
        // perform delete
        TimePeriod session = (TimePeriod)deleteMilkTakings_MilkingSessionList.getSelectedValue();
        Cow cow = (Cow)deleteMilkTakings_CowList.getSelectedValue();
        maxT.deleteMilkTakings(session, cow);
        reportSuccess(session + " Milk Takings deleted.");
        // update current tab
        updateMilkingSessionList(deleteMilkTakings_MilkingSessionList, cow);
        clearField(deleteMilkTakings_YieldTxt);
        updateMilkingSessionLists("deleteMilkTakings");  // update other tabs
    }
    
    // -------------------------------------------------------------------------
    // 'Yield Table' screen methods                                  YIELD TABLE
    
    private void updateYieldValue(javax.swing.JTextField aField, String mapKey)
    {
        String yieldValue = aField.getText();
        if (!validInt(yieldValue,"milk yield value"))
        {
            reportError("Invalid milk yield value entered. Updated to 0 instead.");
            aField.setText("0");
            maxT.updateYieldValue(mapKey, 0);
        }
        else
        {
            maxT.updateYieldValue(mapKey, Integer.parseInt(yieldValue));
            reportSuccess("Yield Value updated to '" + yieldValue + "'.");
        }
    }
    
    // -------------------------------------------------------------------------
    // Universal Helper methods                                             MISC
    
    private boolean validString(String theInput, String fieldName)
    {
        if (theInput.equals(""))
        {
            reportError("Enter " + fieldName + ".");
            return false;
        }
        
        return true;
    }
    
    private boolean validInt(String theInput, String fieldName)
    {
        if(theInput.equals(""))
        {
            reportError("Enter " + fieldName + ".");
            return false;
        }
        
        try
        {
            int intCheck = Integer.parseInt(theInput);
            if (intCheck < 0)
            {
                reportError("Milk yield must be a positive value");
                return false;
            }
            return true;
        }
        catch (NumberFormatException e)
        {
            reportError("Milk yield must be numerical and rounded to nearest litre.");
            return false;
        }
    }
    
    private void reportError(String aMessage)
    {
        feedbackTxt.setText("ERROR: " + aMessage);
        feedbackTxt.setForeground(Color.red);
    }
    
    private void reportSuccess(String aMessage)
    {
        feedbackTxt.setText("SUCCESS: " + aMessage);
        feedbackTxt.setForeground(Color.blue);
    }
    
    private void resetFeedback()
    {
        feedbackTxt.setText("Feedback:");
        feedbackTxt.setForeground(Color.black);
    }
    
    private void updateFarmLists()
    {
        updateFarmList(main_FarmList);
        updateFarmList(addHerd_FarmList);
        updateFarmList(addCow_FarmList);
        updateFarmList(addMilkTakings_FarmList);
        updateFarmList(updateMilkTakings_FarmList);
        updateFarmList(deleteFarm_FarmList);
        updateFarmList(deleteHerd_FarmList);
        updateFarmList(deleteCow_FarmList);
        updateFarmList(deleteMilkTakings_FarmList);
        clearHerdLists();
    }
    
    private void updateFarmList(m256gui.M256JList aFarmList)
    {
        aFarmList.setListData(maxT.getFarms());
    }
    
    private void updateHerdLists(String skipThis)
    {
        if (!skipThis.equals("addCow"))
        {
            if ((Farm)addCow_FarmList.getSelectedValue() != null)
            {
                updateHerdList(addCow_HerdList, (Farm)addCow_FarmList.getSelectedValue());
            }
            else
            {
                clearList(addCow_HerdList);
            }
        }
        
        if (!skipThis.equals("addMilkTakings"))
        {
            if ((Farm)addMilkTakings_FarmList.getSelectedValue() != null)
            {
                updateHerdList(addMilkTakings_HerdList, (Farm)addMilkTakings_FarmList.getSelectedValue());
            }
            else
            {
                clearList(addMilkTakings_HerdList);
            }
        }
        
        if (!skipThis.equals("updateMilkTakings"))
        {
            if ((Farm)updateMilkTakings_FarmList.getSelectedValue() != null)
            {
                updateHerdList(updateMilkTakings_HerdList, (Farm)updateMilkTakings_FarmList.getSelectedValue());
            }
            else
            {
                clearList(updateMilkTakings_HerdList);
            }
        }
        
        if (!skipThis.equals("deleteHerd"))
        {
            if ((Farm)deleteHerd_FarmList.getSelectedValue() != null)
            {
                updateHerdList(deleteHerd_HerdList, (Farm)deleteHerd_FarmList.getSelectedValue());
            }
            else
            {
                clearList(deleteHerd_HerdList);
            }
        }
        
        if (!skipThis.equals("deleteCow"))
        {
            if ((Farm)deleteCow_FarmList.getSelectedValue() != null)
            {
                updateHerdList(deleteCow_HerdList, (Farm)deleteCow_FarmList.getSelectedValue());
            }
            else
            {
                clearList(deleteCow_HerdList);
            }
        }
        
        if (!skipThis.equals("deleteMilkTakings"))
        {
            if ((Farm)deleteMilkTakings_FarmList.getSelectedValue() != null)
            {
                updateHerdList(deleteMilkTakings_HerdList, (Farm)deleteMilkTakings_FarmList.getSelectedValue());
            }
            else
            {
                clearList(deleteMilkTakings_HerdList);
            }
        }
                
        updateCowLists(skipThis);
    }
    
    private void updateHerdList(m256gui.M256JList aHerdList, Farm aFarm)
    {
        aHerdList.setListData(maxT.getHerds(aFarm));
    }
    
    private void updateCowLists(String skipThis)
    {
        if (!skipThis.equals("addMilkTakings"))
        {
            if ((Herd)addMilkTakings_HerdList.getSelectedValue() != null)
            {
                updateCowList(addMilkTakings_CowList, (Herd)addMilkTakings_HerdList.getSelectedValue());
            }
            else
            {
                clearList(addMilkTakings_CowList);
            }
        }
                
        if (!skipThis.equals("updateMilkTakings"))
        {
            if ((Herd)updateMilkTakings_HerdList.getSelectedValue() != null)
            {
                updateCowList(updateMilkTakings_CowList, (Herd)updateMilkTakings_HerdList.getSelectedValue());
            }
            else
            {
                clearList(updateMilkTakings_CowList);
            }
        }
        
        if (!skipThis.equals("deleteCow"))
        {
            if ((Herd)deleteCow_HerdList.getSelectedValue() != null)
            {
                updateCowList(deleteCow_CowList, (Herd)deleteCow_HerdList.getSelectedValue());
            }
            else
            {
                clearList(deleteCow_CowList);
            }
        }
        
        if (!skipThis.equals("deleteMilkTakings"))
        {
            if ((Herd)deleteMilkTakings_HerdList.getSelectedValue() != null)
            {
                updateCowList(deleteMilkTakings_CowList, (Herd)deleteMilkTakings_HerdList.getSelectedValue());
            }
            else
            {
                clearList(deleteMilkTakings_CowList);
            }
        }
            
        updateMilkingSessionLists(skipThis);
    }
    
    private void updateCowList(m256gui.M256JList aCowList, Herd aHerd)
    {
        aCowList.setListData(maxT.getCows(aHerd));
    }
    
    private void updateMilkingSessionLists(String skipThis)
    {
        if (!skipThis.equals("updateMilkTakings"))
        {
            if ((Cow)updateMilkTakings_CowList.getSelectedValue() != null)
            {
                updateMilkingSessionList(updateMilkTakings_MilkingSessionList, (Cow)updateMilkTakings_CowList.getSelectedValue());
            }
            else
            {
                clearList(updateMilkTakings_MilkingSessionList);
            }
        }
        
        if (!skipThis.equals("deleteMilkTakings"))
        {        
            if ((Cow)deleteMilkTakings_CowList.getSelectedValue() != null)
            {
                updateMilkingSessionList(deleteMilkTakings_MilkingSessionList, (Cow)deleteMilkTakings_CowList.getSelectedValue());
            }
            else
            {
                clearList(deleteMilkTakings_MilkingSessionList);
            }
        }
    }
    
    private void updateMilkingSessionList(m256gui.M256JList aMilkingSessionList, Cow aCow)
    {
        aMilkingSessionList.setListData(maxT.getMilkTakings(aCow).keySet());
    }
    
    private void updateYield(javax.swing.JTextField aField, int aYield)
    {
        aField.setText(aYield + "L");
    }
    
    private void clearHerdLists()
    {
        clearList(addCow_HerdList);
        clearList(addMilkTakings_HerdList);
        clearList(updateMilkTakings_HerdList);
        clearList(deleteHerd_HerdList);
        clearList(deleteCow_HerdList);
        clearList(deleteMilkTakings_HerdList);
        clearCowLists();
    }
    
    private void clearCowLists()
    {
        clearList(addMilkTakings_CowList);
        clearList(updateMilkTakings_CowList);
        clearList(deleteCow_CowList);
        clearList(deleteMilkTakings_CowList);
        clearMilkingSessionLists();
    }
    
    private void clearMilkingSessionLists()
    {
        clearList(updateMilkTakings_MilkingSessionList);
        clearList(deleteMilkTakings_MilkingSessionList);
    }
    
    private void clearList(m256gui.M256JList aList)
    {
        aList.setListData(emptyList);
    }
    
    private void clearField(javax.swing.JTextField aField)
    {
        aField.setText("");
    }
    
    private void setFieldForNextEntry(javax.swing.JTextField aField)
    {
        clearField(aField);
        aField.requestFocusInWindow();
    }
    
    private void selectFieldText(javax.swing.JTextField aField)
    {
        aField.requestFocusInWindow();
        aField.selectAll();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        addHerd_MilkingIntervalsGroup = new javax.swing.ButtonGroup();
        addMilkTakings_MilkingSessionGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainScreen = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        main_FarmList = new m256gui.M256JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        main_statDisplay = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        addScreen = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        addFarmScreen = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        addFarm_IdField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        addFarm_NameField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        addFarm_LocationField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        addFarmButton = new javax.swing.JButton();
        addHerdScreen = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        addHerd_FarmList = new m256gui.M256JList();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        addHerd_IdField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        addHerd_NameField = new javax.swing.JTextField();
        addHerd_MilkIntervalRadio1 = new javax.swing.JRadioButton();
        addHerd_MilkIntervalRadio2 = new javax.swing.JRadioButton();
        jLabel21 = new javax.swing.JLabel();
        addHerdButton = new javax.swing.JButton();
        jLabel55 = new javax.swing.JLabel();
        addCowScreen = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        addCow_FarmList = new m256gui.M256JList();
        addCowButton = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        addCow_HerdList = new m256gui.M256JList();
        jLabel25 = new javax.swing.JLabel();
        addCow_IdField = new javax.swing.JTextField();
        addMilkTakingsScreen = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        addMilkTakings_FarmList = new m256gui.M256JList();
        jLabel27 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        addMilkTakings_HerdList = new m256gui.M256JList();
        jScrollPane8 = new javax.swing.JScrollPane();
        addMilkTakings_CowList = new m256gui.M256JList();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        addMilkTakingsButton = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        addMilkTakings_MilkSessionRadio1 = new javax.swing.JRadioButton();
        addMilkTakings_MilkSessionRadio2 = new javax.swing.JRadioButton();
        jLabel37 = new javax.swing.JLabel();
        addMilkTakings_YieldField = new javax.swing.JTextField();
        updateScreen = new javax.swing.JPanel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        updateMilkTakingsScreen = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        updateMilkTakings_FarmList = new m256gui.M256JList();
        jLabel33 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        updateMilkTakings_HerdList = new m256gui.M256JList();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        updateMilkTakings_CowList = new m256gui.M256JList();
        updateMilkTakingsButton = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        updateMilkTakings_ExistingYieldTxt = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        updateMilkTakings_NewYieldField = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jScrollPane22 = new javax.swing.JScrollPane();
        updateMilkTakings_MilkingSessionList = new m256gui.M256JList();
        deleteScreen = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        deleteFarmScreen = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        deleteFarm_FarmList = new m256gui.M256JList();
        deleteFarmButton = new javax.swing.JButton();
        deleteHerdScreen = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        deleteHerd_FarmList = new m256gui.M256JList();
        jLabel41 = new javax.swing.JLabel();
        jScrollPane14 = new javax.swing.JScrollPane();
        deleteHerd_HerdList = new m256gui.M256JList();
        deleteHerdButton = new javax.swing.JButton();
        deleteCowScreen = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jScrollPane15 = new javax.swing.JScrollPane();
        deleteCow_FarmList = new m256gui.M256JList();
        jLabel43 = new javax.swing.JLabel();
        jScrollPane16 = new javax.swing.JScrollPane();
        deleteCow_HerdList = new m256gui.M256JList();
        jLabel44 = new javax.swing.JLabel();
        jScrollPane17 = new javax.swing.JScrollPane();
        deleteCow_CowList = new m256gui.M256JList();
        deleteCowButton = new javax.swing.JButton();
        deleteMilkTakingsScreen = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jScrollPane18 = new javax.swing.JScrollPane();
        deleteMilkTakings_FarmList = new m256gui.M256JList();
        jLabel46 = new javax.swing.JLabel();
        jScrollPane19 = new javax.swing.JScrollPane();
        deleteMilkTakings_HerdList = new m256gui.M256JList();
        jLabel47 = new javax.swing.JLabel();
        jScrollPane20 = new javax.swing.JScrollPane();
        deleteMilkTakings_CowList = new m256gui.M256JList();
        jLabel48 = new javax.swing.JLabel();
        jScrollPane21 = new javax.swing.JScrollPane();
        deleteMilkTakings_MilkingSessionList = new m256gui.M256JList();
        deleteMilkTakingsButton = new javax.swing.JButton();
        jLabel51 = new javax.swing.JLabel();
        deleteMilkTakings_YieldTxt = new javax.swing.JTextField();
        yieldTableScreen = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        yieldTable_20_8_PM = new javax.swing.JTextField();
        yieldTable_20_8_AM = new javax.swing.JTextField();
        yieldTable_20_9_PM = new javax.swing.JTextField();
        yieldTable_20_9_AM = new javax.swing.JTextField();
        yieldTable_21_8_PM = new javax.swing.JTextField();
        yieldTable_21_8_AM = new javax.swing.JTextField();
        yieldTable_21_9_PM = new javax.swing.JTextField();
        yieldTable_21_9_AM = new javax.swing.JTextField();
        yieldTable_22_8_PM = new javax.swing.JTextField();
        yieldTable_22_8_AM = new javax.swing.JTextField();
        yieldTable_22_9_PM = new javax.swing.JTextField();
        yieldTable_22_9_AM = new javax.swing.JTextField();
        yieldTable_23_8_PM = new javax.swing.JTextField();
        yieldTable_23_8_AM = new javax.swing.JTextField();
        yieldTable_23_9_PM = new javax.swing.JTextField();
        yieldTable_23_9_AM = new javax.swing.JTextField();
        yieldTable_24_8_PM = new javax.swing.JTextField();
        yieldTable_24_8_AM = new javax.swing.JTextField();
        yieldTable_24_9_PM = new javax.swing.JTextField();
        yieldTable_24_9_AM = new javax.swing.JTextField();
        yieldTable_25_8_PM = new javax.swing.JTextField();
        yieldTable_25_8_AM = new javax.swing.JTextField();
        yieldTable_25_9_PM = new javax.swing.JTextField();
        yieldTable_25_9_AM = new javax.swing.JTextField();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        exitButton = new javax.swing.JButton();
        feedbackTxt = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Calculate MaxT");

        jPanel1.setPreferredSize(new java.awt.Dimension(600, 460));

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(600, 420));

        mainScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                mainScreenComponentShown(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 102));
        jLabel1.setText("Overall Statistics");

        jLabel2.setText("Select Farm:");

        main_FarmList.setMaximumSize(new java.awt.Dimension(33, 64));
        main_FarmList.setMinimumSize(new java.awt.Dimension(33, 64));
        main_FarmList.setVisibleRowCount(10);
        main_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                main_FarmListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(main_FarmList);

        main_statDisplay.setEditable(false);
        main_statDisplay.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.disabledBackground"));
        main_statDisplay.setColumns(20);
        main_statDisplay.setFont(new java.awt.Font("Courier New", 0, 13)); // NOI18N
        main_statDisplay.setRows(5);
        main_statDisplay.setFocusable(false);
        jScrollPane2.setViewportView(main_statDisplay);

        jLabel3.setText("Statistics:");

        jLabel69.setText("A herd will display only Cow Qty until all Milk Taking data is entered for that herd.");

        jLabel70.setText("A MaxT value of 'XX:XX' indicates that the Avg Per Cow is out of MaxT range (7L-16L).");

        jLabel71.setText("Note:");

        javax.swing.GroupLayout mainScreenLayout = new javax.swing.GroupLayout(mainScreen);
        mainScreen.setLayout(mainScreenLayout);
        mainScreenLayout.setHorizontalGroup(
            mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainScreenLayout.createSequentialGroup()
                        .addGroup(mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(mainScreenLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(71, 71, 71)
                                .addComponent(jLabel3)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(mainScreenLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                            .addGroup(mainScreenLayout.createSequentialGroup()
                                .addGroup(mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel70)
                                    .addComponent(jLabel69)
                                    .addComponent(jLabel71))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        mainScreenLayout.setVerticalGroup(
            mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel71)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel69)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel70)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Overall Statistics", mainScreen);

        addScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                addScreenComponentShown(evt);
            }
        });

        addFarmScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                addFarmScreenComponentShown(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 102));
        jLabel4.setText("Add Farm");

        jLabel13.setText("Farm ID:");

        jLabel14.setText("Farm Name:");

        jLabel15.setText("Farm Location:");

        jLabel16.setText("Enter details of new farm:");

        addFarmButton.setText("Add Farm");
        addFarmButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addFarmButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addFarmScreenLayout = new javax.swing.GroupLayout(addFarmScreen);
        addFarmScreen.setLayout(addFarmScreenLayout);
        addFarmScreenLayout.setHorizontalGroup(
            addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addFarmScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel16)
                    .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addFarmButton)
                        .addGroup(addFarmScreenLayout.createSequentialGroup()
                            .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel15)
                                .addComponent(jLabel14)
                                .addComponent(jLabel13))
                            .addGap(18, 18, 18)
                            .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(addFarm_IdField)
                                .addComponent(addFarm_NameField)
                                .addComponent(addFarm_LocationField, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)))))
                .addContainerGap(291, Short.MAX_VALUE))
        );
        addFarmScreenLayout.setVerticalGroup(
            addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addFarmScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(addFarm_IdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(addFarm_NameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(addFarm_LocationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(addFarmButton)
                .addContainerGap(168, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Farm", addFarmScreen);

        addHerdScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                addHerdScreenComponentShown(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 102));
        jLabel5.setText("Add Herd");

        addHerd_FarmList.setVisibleRowCount(6);
        jScrollPane3.setViewportView(addHerd_FarmList);

        jLabel17.setText("Select farm for new herd:");

        jLabel18.setText("Enter details of new herd:");

        jLabel19.setText("Herd ID:");

        jLabel20.setText("Herd Name:");

        addHerd_MilkingIntervalsGroup.add(addHerd_MilkIntervalRadio1);
        addHerd_MilkIntervalRadio1.setText("8, 16");
        addHerd_MilkIntervalRadio1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addHerd_MilkIntervalRadio1ActionPerformed(evt);
            }
        });

        addHerd_MilkingIntervalsGroup.add(addHerd_MilkIntervalRadio2);
        addHerd_MilkIntervalRadio2.setText("9, 15");
        addHerd_MilkIntervalRadio2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addHerd_MilkIntervalRadio2ActionPerformed(evt);
            }
        });

        jLabel21.setText("Milking Intervals:");

        addHerdButton.setText("Add Herd");
        addHerdButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addHerdButtonActionPerformed(evt);
            }
        });

        jLabel55.setText("(8 character limit)");

        javax.swing.GroupLayout addHerdScreenLayout = new javax.swing.GroupLayout(addHerdScreen);
        addHerdScreen.setLayout(addHerdScreenLayout);
        addHerdScreenLayout.setHorizontalGroup(
            addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addHerdScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addGroup(addHerdScreenLayout.createSequentialGroup()
                        .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(addHerdScreenLayout.createSequentialGroup()
                                    .addComponent(jLabel20)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(addHerd_IdField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addHerd_NameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addHerdButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addHerdScreenLayout.createSequentialGroup()
                                    .addComponent(jLabel21)
                                    .addGap(18, 18, 18)
                                    .addComponent(addHerd_MilkIntervalRadio1)
                                    .addGap(18, 18, 18)
                                    .addComponent(addHerd_MilkIntervalRadio2)))
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel55)))
                .addContainerGap(219, Short.MAX_VALUE))
        );
        addHerdScreenLayout.setVerticalGroup(
            addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addHerdScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addHerd_IdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel55))
                .addGap(18, 18, 18)
                .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addHerd_NameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addGap(18, 18, 18)
                .addGroup(addHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addHerd_MilkIntervalRadio1)
                    .addComponent(addHerd_MilkIntervalRadio2)
                    .addComponent(jLabel21))
                .addGap(9, 9, 9)
                .addComponent(addHerdButton)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Herd", addHerdScreen);

        addCowScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                addCowScreenComponentShown(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 102));
        jLabel6.setText("Add Cow");

        jLabel22.setText("Enter details of new cow:");

        jLabel23.setText("Select farm for new cow:");

        addCow_FarmList.setVisibleRowCount(6);
        addCow_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                addCow_FarmListValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(addCow_FarmList);

        addCowButton.setText("Add Cow");
        addCowButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addCowButtonActionPerformed(evt);
            }
        });

        jLabel24.setText("Select herd for new cow:");

        addCow_HerdList.setVisibleRowCount(6);
        jScrollPane5.setViewportView(addCow_HerdList);

        jLabel25.setText("Cow ID:");

        javax.swing.GroupLayout addCowScreenLayout = new javax.swing.GroupLayout(addCowScreen);
        addCowScreen.setLayout(addCowScreenLayout);
        addCowScreenLayout.setHorizontalGroup(
            addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addCowScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addCowScreenLayout.createSequentialGroup()
                        .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addGap(41, 41, 41)
                        .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addCowButton)
                        .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel22)
                            .addGroup(addCowScreenLayout.createSequentialGroup()
                                .addComponent(jLabel25)
                                .addGap(18, 18, 18)
                                .addComponent(addCow_IdField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(299, Short.MAX_VALUE))
        );
        addCowScreenLayout.setVerticalGroup(
            addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addCowScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addCowScreenLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addCowScreenLayout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addCow_IdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addGap(18, 18, 18)
                .addComponent(addCowButton)
                .addContainerGap(113, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Cow", addCowScreen);

        addMilkTakingsScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                addMilkTakingsScreenComponentShown(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 102));
        jLabel7.setText("Add Milk Takings");

        jLabel26.setText("Select farm for milk takings:");

        addMilkTakings_FarmList.setVisibleRowCount(6);
        addMilkTakings_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                addMilkTakings_FarmListValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(addMilkTakings_FarmList);

        jLabel27.setText("Select herd for milk takings:");

        addMilkTakings_HerdList.setVisibleRowCount(6);
        addMilkTakings_HerdList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                addMilkTakings_HerdListValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(addMilkTakings_HerdList);

        addMilkTakings_CowList.setVisibleRowCount(6);
        jScrollPane8.setViewportView(addMilkTakings_CowList);

        jLabel28.setText("Select cow for milk takings:");

        jLabel29.setText("Enter details of milk takings:");

        addMilkTakingsButton.setText("Add Milk Takings");
        addMilkTakingsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addMilkTakingsButtonActionPerformed(evt);
            }
        });

        jLabel31.setText("Milking session:");

        addMilkTakings_MilkingSessionGroup.add(addMilkTakings_MilkSessionRadio1);
        addMilkTakings_MilkSessionRadio1.setText("AM");
        addMilkTakings_MilkSessionRadio1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addMilkTakings_MilkSessionRadio1ActionPerformed(evt);
            }
        });

        addMilkTakings_MilkingSessionGroup.add(addMilkTakings_MilkSessionRadio2);
        addMilkTakings_MilkSessionRadio2.setText("PM");
        addMilkTakings_MilkSessionRadio2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addMilkTakings_MilkSessionRadio2ActionPerformed(evt);
            }
        });

        jLabel37.setText("Milk yield  (rounded to nearest L):");

        javax.swing.GroupLayout addMilkTakingsScreenLayout = new javax.swing.GroupLayout(addMilkTakingsScreen);
        addMilkTakingsScreen.setLayout(addMilkTakingsScreenLayout);
        addMilkTakingsScreenLayout.setHorizontalGroup(
            addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                        .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addGap(41, 41, 41)
                        .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel27)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addMilkTakingsButton)
                        .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel29)
                            .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                                .addComponent(jLabel31)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(addMilkTakings_MilkSessionRadio1)
                                .addGap(18, 18, 18)
                                .addComponent(addMilkTakings_MilkSessionRadio2))
                            .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                                .addComponent(jLabel37)
                                .addGap(18, 18, 18)
                                .addComponent(addMilkTakings_YieldField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(108, Short.MAX_VALUE))
        );
        addMilkTakingsScreenLayout.setVerticalGroup(
            addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(addMilkTakings_MilkSessionRadio1)
                    .addComponent(addMilkTakings_MilkSessionRadio2))
                .addGap(18, 18, 18)
                .addGroup(addMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(addMilkTakings_YieldField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(addMilkTakingsButton)
                .addContainerGap(74, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Milk Takings", addMilkTakingsScreen);

        javax.swing.GroupLayout addScreenLayout = new javax.swing.GroupLayout(addScreen);
        addScreen.setLayout(addScreenLayout);
        addScreenLayout.setHorizontalGroup(
            addScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        addScreenLayout.setVerticalGroup(
            addScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );

        jTabbedPane1.addTab("Add", addScreen);

        updateScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                updateScreenComponentShown(evt);
            }
        });

        updateMilkTakingsScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                updateMilkTakingsScreenComponentShown(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 102));
        jLabel12.setText("Update Milk Takings");

        jLabel32.setText("Select farm for milk takings:");

        updateMilkTakings_FarmList.setVisibleRowCount(6);
        updateMilkTakings_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                updateMilkTakings_FarmListValueChanged(evt);
            }
        });
        jScrollPane9.setViewportView(updateMilkTakings_FarmList);

        jLabel33.setText("Select herd for milk takings:");

        updateMilkTakings_HerdList.setVisibleRowCount(6);
        updateMilkTakings_HerdList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                updateMilkTakings_HerdListValueChanged(evt);
            }
        });
        jScrollPane10.setViewportView(updateMilkTakings_HerdList);

        jLabel34.setText("Select cow for milk takings:");

        updateMilkTakings_CowList.setVisibleRowCount(6);
        updateMilkTakings_CowList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                updateMilkTakings_CowListValueChanged(evt);
            }
        });
        jScrollPane11.setViewportView(updateMilkTakings_CowList);

        updateMilkTakingsButton.setText("Update Milk Takings");
        updateMilkTakingsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                updateMilkTakingsButtonActionPerformed(evt);
            }
        });

        jLabel30.setText("Existing milk yield:");

        updateMilkTakings_ExistingYieldTxt.setEditable(false);
        updateMilkTakings_ExistingYieldTxt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        jLabel38.setText("New milk yield  (rounded to nearest L):");

        jLabel50.setText("Select milking session:");

        updateMilkTakings_MilkingSessionList.setVisibleRowCount(2);
        updateMilkTakings_MilkingSessionList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                updateMilkTakings_MilkingSessionListValueChanged(evt);
            }
        });
        jScrollPane22.setViewportView(updateMilkTakings_MilkingSessionList);

        javax.swing.GroupLayout updateMilkTakingsScreenLayout = new javax.swing.GroupLayout(updateMilkTakingsScreen);
        updateMilkTakingsScreen.setLayout(updateMilkTakingsScreenLayout);
        updateMilkTakingsScreenLayout.setHorizontalGroup(
            updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(updateMilkTakingsButton)
                        .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(updateMilkTakings_ExistingYieldTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                                .addComponent(jLabel38)
                                .addGap(28, 28, 28)
                                .addComponent(updateMilkTakings_NewYieldField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel50)
                    .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                        .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel32))
                        .addGap(41, 41, 41)
                        .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33)
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel34))))
                .addContainerGap(108, Short.MAX_VALUE))
        );
        updateMilkTakingsScreenLayout.setVerticalGroup(
            updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(updateMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel50)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateMilkTakings_ExistingYieldTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30))
                .addGap(18, 18, 18)
                .addGroup(updateMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(updateMilkTakings_NewYieldField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(updateMilkTakingsButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane4.addTab("Milk Takings", updateMilkTakingsScreen);

        javax.swing.GroupLayout updateScreenLayout = new javax.swing.GroupLayout(updateScreen);
        updateScreen.setLayout(updateScreenLayout);
        updateScreenLayout.setHorizontalGroup(
            updateScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 595, Short.MAX_VALUE)
            .addGroup(updateScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane4))
        );
        updateScreenLayout.setVerticalGroup(
            updateScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
            .addGroup(updateScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane4))
        );

        jTabbedPane1.addTab("Update", updateScreen);

        deleteScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                deleteScreenComponentShown(evt);
            }
        });

        deleteFarmScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                deleteFarmScreenComponentShown(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 102));
        jLabel9.setText("Delete Farm");

        jLabel39.setText("Select farm to delete:");

        jScrollPane12.setViewportView(deleteFarm_FarmList);

        deleteFarmButton.setText("Delete Farm");
        deleteFarmButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteFarmButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout deleteFarmScreenLayout = new javax.swing.GroupLayout(deleteFarmScreen);
        deleteFarmScreen.setLayout(deleteFarmScreenLayout);
        deleteFarmScreenLayout.setHorizontalGroup(
            deleteFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteFarmScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(deleteFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel39)
                    .addGroup(deleteFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(deleteFarmButton)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(476, Short.MAX_VALUE))
        );
        deleteFarmScreenLayout.setVerticalGroup(
            deleteFarmScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteFarmScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel39)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(deleteFarmButton)
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Farm", deleteFarmScreen);

        deleteHerdScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                deleteHerdScreenComponentShown(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 102));
        jLabel10.setText("Delete Herd");

        jLabel40.setText("Select farm:");

        deleteHerd_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteHerd_FarmListValueChanged(evt);
            }
        });
        jScrollPane13.setViewportView(deleteHerd_FarmList);

        jLabel41.setText("Select herd to delete:");

        jScrollPane14.setViewportView(deleteHerd_HerdList);

        deleteHerdButton.setText("Delete Herd");
        deleteHerdButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteHerdButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout deleteHerdScreenLayout = new javax.swing.GroupLayout(deleteHerdScreen);
        deleteHerdScreen.setLayout(deleteHerdScreenLayout);
        deleteHerdScreenLayout.setHorizontalGroup(
            deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteHerdScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addGroup(deleteHerdScreenLayout.createSequentialGroup()
                        .addGroup(deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel40))
                        .addGap(41, 41, 41)
                        .addGroup(deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel41)
                            .addGroup(deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(deleteHerdButton)
                                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(335, Short.MAX_VALUE))
        );
        deleteHerdScreenLayout.setVerticalGroup(
            deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteHerdScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deleteHerdScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(deleteHerdScreenLayout.createSequentialGroup()
                        .addComponent(jLabel40)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(deleteHerdScreenLayout.createSequentialGroup()
                        .addComponent(jLabel41)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(deleteHerdButton)
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Herd", deleteHerdScreen);

        deleteCowScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                deleteCowScreenComponentShown(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 102));
        jLabel11.setText("Delete Cow");

        jLabel42.setText("Select farm:");

        deleteCow_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteCow_FarmListValueChanged(evt);
            }
        });
        jScrollPane15.setViewportView(deleteCow_FarmList);

        jLabel43.setText("Select herd:");

        deleteCow_HerdList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteCow_HerdListValueChanged(evt);
            }
        });
        jScrollPane16.setViewportView(deleteCow_HerdList);

        jLabel44.setText("Select cow to delete:");

        jScrollPane17.setViewportView(deleteCow_CowList);

        deleteCowButton.setText("Delete Cow");
        deleteCowButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteCowButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout deleteCowScreenLayout = new javax.swing.GroupLayout(deleteCowScreen);
        deleteCowScreen.setLayout(deleteCowScreenLayout);
        deleteCowScreenLayout.setHorizontalGroup(
            deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteCowScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(deleteCowScreenLayout.createSequentialGroup()
                        .addGroup(deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel42))
                        .addGap(41, 41, 41)
                        .addGroup(deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel43)
                            .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44)
                            .addGroup(deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(deleteCowButton)
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(200, Short.MAX_VALUE))
        );
        deleteCowScreenLayout.setVerticalGroup(
            deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteCowScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deleteCowScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(deleteCowScreenLayout.createSequentialGroup()
                        .addComponent(jLabel42)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(deleteCowScreenLayout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(deleteCowScreenLayout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(deleteCowButton)
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Cow", deleteCowScreen);

        deleteMilkTakingsScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                deleteMilkTakingsScreenComponentShown(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 102));
        jLabel8.setText("Delete Milk Takings");

        jLabel45.setText("Select farm:");

        deleteMilkTakings_FarmList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteMilkTakings_FarmListValueChanged(evt);
            }
        });
        jScrollPane18.setViewportView(deleteMilkTakings_FarmList);

        jLabel46.setText("Select herd:");

        deleteMilkTakings_HerdList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteMilkTakings_HerdListValueChanged(evt);
            }
        });
        jScrollPane19.setViewportView(deleteMilkTakings_HerdList);

        jLabel47.setText("Select cow:");

        deleteMilkTakings_CowList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteMilkTakings_CowListValueChanged(evt);
            }
        });
        jScrollPane20.setViewportView(deleteMilkTakings_CowList);

        jLabel48.setText("Select milking session:");

        deleteMilkTakings_MilkingSessionList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                deleteMilkTakings_MilkingSessionListValueChanged(evt);
            }
        });
        jScrollPane21.setViewportView(deleteMilkTakings_MilkingSessionList);

        deleteMilkTakingsButton.setText("Delete Milk Takings");
        deleteMilkTakingsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteMilkTakingsButtonActionPerformed(evt);
            }
        });

        jLabel51.setText("Milk yield to be deleted:");

        deleteMilkTakings_YieldTxt.setEditable(false);
        deleteMilkTakings_YieldTxt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        javax.swing.GroupLayout deleteMilkTakingsScreenLayout = new javax.swing.GroupLayout(deleteMilkTakingsScreen);
        deleteMilkTakingsScreen.setLayout(deleteMilkTakingsScreenLayout);
        deleteMilkTakingsScreenLayout.setHorizontalGroup(
            deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                        .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel48)
                                    .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                                    .addComponent(jLabel51)
                                    .addGap(18, 18, 18)
                                    .addComponent(deleteMilkTakings_YieldTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(deleteMilkTakingsButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                        .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel45))
                                .addGap(41, 41, 41)
                                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel46)
                                    .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(38, 38, 38)
                                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel47)
                                    .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel8))
                        .addContainerGap(201, Short.MAX_VALUE))))
        );
        deleteMilkTakingsScreenLayout.setVerticalGroup(
            deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel46)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(deleteMilkTakingsScreenLayout.createSequentialGroup()
                        .addComponent(jLabel47)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(deleteMilkTakingsScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteMilkTakings_YieldTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel51))
                .addGap(18, 18, 18)
                .addComponent(deleteMilkTakingsButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Milk Takings", deleteMilkTakingsScreen);

        javax.swing.GroupLayout deleteScreenLayout = new javax.swing.GroupLayout(deleteScreen);
        deleteScreen.setLayout(deleteScreenLayout);
        deleteScreenLayout.setHorizontalGroup(
            deleteScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3)
        );
        deleteScreenLayout.setVerticalGroup(
            deleteScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3)
        );

        jTabbedPane1.addTab("Delete", deleteScreen);

        yieldTableScreen.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                yieldTableScreenComponentShown(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(0, 0, 102));
        jLabel35.setText("Yield Table");

        yieldTable_20_8_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_20_8_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_20_8_PMFocusLost(evt);
            }
        });

        yieldTable_20_8_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_20_8_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_20_8_AMFocusLost(evt);
            }
        });

        yieldTable_20_9_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_20_9_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_20_9_PMFocusLost(evt);
            }
        });

        yieldTable_20_9_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_20_9_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_20_9_AMFocusLost(evt);
            }
        });

        yieldTable_21_8_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_21_8_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_21_8_PMFocusLost(evt);
            }
        });

        yieldTable_21_8_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_21_8_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_21_8_AMFocusLost(evt);
            }
        });

        yieldTable_21_9_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_21_9_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_21_9_PMFocusLost(evt);
            }
        });

        yieldTable_21_9_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_21_9_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_21_9_AMFocusLost(evt);
            }
        });

        yieldTable_22_8_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_22_8_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_22_8_PMFocusLost(evt);
            }
        });

        yieldTable_22_8_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_22_8_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_22_8_AMFocusLost(evt);
            }
        });

        yieldTable_22_9_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_22_9_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_22_9_PMFocusLost(evt);
            }
        });

        yieldTable_22_9_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_22_9_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_22_9_AMFocusLost(evt);
            }
        });

        yieldTable_23_8_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_23_8_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_23_8_PMFocusLost(evt);
            }
        });

        yieldTable_23_8_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_23_8_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_23_8_AMFocusLost(evt);
            }
        });

        yieldTable_23_9_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_23_9_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_23_9_PMFocusLost(evt);
            }
        });

        yieldTable_23_9_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_23_9_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_23_9_AMFocusLost(evt);
            }
        });

        yieldTable_24_8_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_24_8_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_24_8_PMFocusLost(evt);
            }
        });

        yieldTable_24_8_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_24_8_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_24_8_AMFocusLost(evt);
            }
        });

        yieldTable_24_9_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_24_9_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_24_9_PMFocusLost(evt);
            }
        });

        yieldTable_24_9_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_24_9_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_24_9_AMFocusLost(evt);
            }
        });

        yieldTable_25_8_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_25_8_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_25_8_PMFocusLost(evt);
            }
        });

        yieldTable_25_8_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_25_8_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_25_8_AMFocusLost(evt);
            }
        });

        yieldTable_25_9_PM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_25_9_PM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_25_9_PMFocusLost(evt);
            }
        });

        yieldTable_25_9_AM.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        yieldTable_25_9_AM.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                yieldTable_25_9_AMFocusLost(evt);
            }
        });

        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel56.setText("AM");
        jLabel56.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel57.setText("AM");
        jLabel57.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setText("PM");
        jLabel58.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel59.setText("PM");
        jLabel59.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel60.setText("(8,16)");

        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel61.setText("(9,15)");

        jLabel62.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel62.setText("Milking Intervals");

        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Daily");

        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel49.setText("Milk");

        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText("Yield");

        jLabel53.setText("20");

        jLabel54.setText("21");

        jLabel63.setText("22");

        jLabel64.setText("23");

        jLabel65.setText("24");

        jLabel66.setText("25");

        jLabel67.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel67.setText("(L)");

        jLabel68.setText("Enter yield table values:");

        javax.swing.GroupLayout yieldTableScreenLayout = new javax.swing.GroupLayout(yieldTableScreen);
        yieldTableScreen.setLayout(yieldTableScreenLayout);
        yieldTableScreenLayout.setHorizontalGroup(
            yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yieldTableScreenLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel68)))
                    .addGroup(yieldTableScreenLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel53)
                            .addComponent(jLabel65)
                            .addComponent(jLabel66)
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel67, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel64, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel54, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel63, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(6, 6, 6)
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addComponent(yieldTable_25_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_25_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_25_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_25_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addComponent(yieldTable_24_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_24_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_24_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_24_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addComponent(yieldTable_23_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_23_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_23_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_23_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addComponent(yieldTable_22_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_22_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_22_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_22_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addComponent(yieldTable_21_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_21_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_21_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yieldTable_21_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(yieldTable_20_8_PM)
                                            .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(yieldTable_20_8_AM)
                                            .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(yieldTableScreenLayout.createSequentialGroup()
                                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(yieldTable_20_9_PM)
                                            .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(yieldTable_20_9_AM)
                                            .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 327, Short.MAX_VALUE))
        );
        yieldTableScreenLayout.setVerticalGroup(
            yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yieldTableScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel68)
                .addGap(18, 18, 18)
                .addComponent(jLabel62)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel60)
                    .addComponent(jLabel61))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(jLabel57)
                    .addComponent(jLabel58)
                    .addComponent(jLabel59))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yieldTable_20_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yieldTable_20_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yieldTable_20_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yieldTable_20_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel53))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yieldTableScreenLayout.createSequentialGroup()
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yieldTable_21_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_21_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_21_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_21_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel54))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yieldTable_22_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_22_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_22_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_22_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel63))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yieldTable_23_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_23_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_23_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_23_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel64))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yieldTable_24_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_24_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_24_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_24_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel65))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(yieldTableScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(yieldTable_25_8_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_25_8_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_25_9_PM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yieldTable_25_9_AM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel66)))
                    .addGroup(yieldTableScreenLayout.createSequentialGroup()
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel49)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel52)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel67)))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Yield Table", yieldTableScreen);

        bottomPanel.setPreferredSize(new java.awt.Dimension(600, 30));

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitButtonActionPerformed(evt);
            }
        });

        feedbackTxt.setEditable(false);
        feedbackTxt.setText("Feedback:  ");
        feedbackTxt.setFocusable(false);

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(exitButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(feedbackTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(180, 180, 180))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitButton)
                    .addComponent(feedbackTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(100, 100, 100))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(616, 499));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void addFarmButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addFarmButtonActionPerformed
    {//GEN-HEADEREND:event_addFarmButtonActionPerformed
        addFarm();
    }//GEN-LAST:event_addFarmButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitButtonActionPerformed
    {//GEN-HEADEREND:event_exitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void deleteFarmButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteFarmButtonActionPerformed
    {//GEN-HEADEREND:event_deleteFarmButtonActionPerformed
        deleteFarm();
    }//GEN-LAST:event_deleteFarmButtonActionPerformed

    private void addHerdButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addHerdButtonActionPerformed
    {//GEN-HEADEREND:event_addHerdButtonActionPerformed
        addHerd();
    }//GEN-LAST:event_addHerdButtonActionPerformed

    private void deleteHerdButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteHerdButtonActionPerformed
    {//GEN-HEADEREND:event_deleteHerdButtonActionPerformed
        deleteHerd();
    }//GEN-LAST:event_deleteHerdButtonActionPerformed

    private void deleteHerd_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteHerd_FarmListValueChanged
    {//GEN-HEADEREND:event_deleteHerd_FarmListValueChanged
        // if a farm is selected..
        if ((Farm)deleteHerd_FarmList.getSelectedValue() != null)
        {
            Farm farm = (Farm)deleteHerd_FarmList.getSelectedValue();
            updateHerdList(deleteHerd_HerdList, farm);
        }
    }//GEN-LAST:event_deleteHerd_FarmListValueChanged

    private void addHerd_MilkIntervalRadio1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addHerd_MilkIntervalRadio1ActionPerformed
    {//GEN-HEADEREND:event_addHerd_MilkIntervalRadio1ActionPerformed
        milkingInterval = MilkingIntervals.EIGHT;
    }//GEN-LAST:event_addHerd_MilkIntervalRadio1ActionPerformed

    private void addHerd_MilkIntervalRadio2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addHerd_MilkIntervalRadio2ActionPerformed
    {//GEN-HEADEREND:event_addHerd_MilkIntervalRadio2ActionPerformed
        milkingInterval = MilkingIntervals.NINE;
    }//GEN-LAST:event_addHerd_MilkIntervalRadio2ActionPerformed

    private void addCowButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addCowButtonActionPerformed
    {//GEN-HEADEREND:event_addCowButtonActionPerformed
        addCow();
    }//GEN-LAST:event_addCowButtonActionPerformed

    private void addCow_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_addCow_FarmListValueChanged
    {//GEN-HEADEREND:event_addCow_FarmListValueChanged
        // if a farm is selected..
        if ((Farm)addCow_FarmList.getSelectedValue() != null)
        {
            Farm farm = (Farm)addCow_FarmList.getSelectedValue();
            updateHerdList(addCow_HerdList, farm);
        }
    }//GEN-LAST:event_addCow_FarmListValueChanged

    private void deleteCow_HerdListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteCow_HerdListValueChanged
    {//GEN-HEADEREND:event_deleteCow_HerdListValueChanged
        // if a herd is selected..
        if ((Herd)deleteCow_HerdList.getSelectedValue() != null)
        {
            Herd herd = (Herd)deleteCow_HerdList.getSelectedValue();
            updateCowList(deleteCow_CowList, herd);
        }
    }//GEN-LAST:event_deleteCow_HerdListValueChanged

    private void deleteCow_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteCow_FarmListValueChanged
    {//GEN-HEADEREND:event_deleteCow_FarmListValueChanged
        // if a farm is selected..
        if ((Farm)deleteCow_FarmList.getSelectedValue() != null)
        {
            Farm farm = (Farm)deleteCow_FarmList.getSelectedValue();
            updateHerdList(deleteCow_HerdList, farm);
            clearList(deleteCow_CowList);
        }
    }//GEN-LAST:event_deleteCow_FarmListValueChanged

    private void deleteCowButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteCowButtonActionPerformed
    {//GEN-HEADEREND:event_deleteCowButtonActionPerformed
        deleteCow();
    }//GEN-LAST:event_deleteCowButtonActionPerformed

    private void addMilkTakingsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addMilkTakingsButtonActionPerformed
    {//GEN-HEADEREND:event_addMilkTakingsButtonActionPerformed
        addMilkTakings();
    }//GEN-LAST:event_addMilkTakingsButtonActionPerformed

    private void addMilkTakings_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_addMilkTakings_FarmListValueChanged
    {//GEN-HEADEREND:event_addMilkTakings_FarmListValueChanged
        // if a farm is selected..
        if ((Farm)addMilkTakings_FarmList.getSelectedValue() != null)
        {
            Farm farm = (Farm)addMilkTakings_FarmList.getSelectedValue();
            updateHerdList(addMilkTakings_HerdList, farm);
            clearList(addMilkTakings_CowList);
        }
    }//GEN-LAST:event_addMilkTakings_FarmListValueChanged

    private void addMilkTakings_HerdListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_addMilkTakings_HerdListValueChanged
    {//GEN-HEADEREND:event_addMilkTakings_HerdListValueChanged
        // if a herd is selected..
        if ((Herd)addMilkTakings_HerdList.getSelectedValue() != null)
        {
            Herd herd = (Herd)addMilkTakings_HerdList.getSelectedValue();
            updateCowList(addMilkTakings_CowList, herd);
        }
    }//GEN-LAST:event_addMilkTakings_HerdListValueChanged

    private void deleteMilkTakings_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteMilkTakings_FarmListValueChanged
    {//GEN-HEADEREND:event_deleteMilkTakings_FarmListValueChanged
        // if a farm is selected..
        if ((Farm)deleteMilkTakings_FarmList.getSelectedValue() != null)
        {
            Farm farm = (Farm)deleteMilkTakings_FarmList.getSelectedValue();
            updateHerdList(deleteMilkTakings_HerdList, farm);
            clearList(deleteMilkTakings_CowList);
            clearList(deleteMilkTakings_MilkingSessionList);
            clearField(deleteMilkTakings_YieldTxt);
        }
    }//GEN-LAST:event_deleteMilkTakings_FarmListValueChanged

    private void deleteMilkTakings_HerdListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteMilkTakings_HerdListValueChanged
    {//GEN-HEADEREND:event_deleteMilkTakings_HerdListValueChanged
        // if a herd is selected..
        if ((Herd)deleteMilkTakings_HerdList.getSelectedValue() != null)
        {
            Herd herd = (Herd)deleteMilkTakings_HerdList.getSelectedValue();
            updateCowList(deleteMilkTakings_CowList, herd);
            clearList(deleteMilkTakings_MilkingSessionList);
            clearField(deleteMilkTakings_YieldTxt);
        }
    }//GEN-LAST:event_deleteMilkTakings_HerdListValueChanged

    private void deleteMilkTakings_CowListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteMilkTakings_CowListValueChanged
    {//GEN-HEADEREND:event_deleteMilkTakings_CowListValueChanged
        // if a cow selected..
        if ((Cow)deleteMilkTakings_CowList.getSelectedValue() != null)
        {
            // .. update appropriate milking session list
            Cow cow = (Cow)deleteMilkTakings_CowList.getSelectedValue();
            updateMilkingSessionList(deleteMilkTakings_MilkingSessionList, cow);
            clearField(deleteMilkTakings_YieldTxt);
        }
    }//GEN-LAST:event_deleteMilkTakings_CowListValueChanged

    private void addMilkTakings_MilkSessionRadio1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addMilkTakings_MilkSessionRadio1ActionPerformed
    {//GEN-HEADEREND:event_addMilkTakings_MilkSessionRadio1ActionPerformed
        milkingSession = TimePeriod.AM;
        selectFieldText(addMilkTakings_YieldField);
    }//GEN-LAST:event_addMilkTakings_MilkSessionRadio1ActionPerformed

    private void addMilkTakings_MilkSessionRadio2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addMilkTakings_MilkSessionRadio2ActionPerformed
    {//GEN-HEADEREND:event_addMilkTakings_MilkSessionRadio2ActionPerformed
        milkingSession = TimePeriod.PM;
        selectFieldText(addMilkTakings_YieldField);
    }//GEN-LAST:event_addMilkTakings_MilkSessionRadio2ActionPerformed

    private void deleteMilkTakingsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteMilkTakingsButtonActionPerformed
    {//GEN-HEADEREND:event_deleteMilkTakingsButtonActionPerformed
        deleteMilkTakings();
    }//GEN-LAST:event_deleteMilkTakingsButtonActionPerformed

    private void deleteMilkTakings_MilkingSessionListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_deleteMilkTakings_MilkingSessionListValueChanged
    {//GEN-HEADEREND:event_deleteMilkTakings_MilkingSessionListValueChanged
        // if a milking session is selected..
        if ((TimePeriod)deleteMilkTakings_MilkingSessionList.getSelectedValue() != null)
        {
            // .. get the associated milk yield and display it
            Cow cow = (Cow)deleteMilkTakings_CowList.getSelectedValue();
            Map<TimePeriod, MilkTakings> milkTakings = maxT.getMilkTakings(cow);
            TimePeriod session = (TimePeriod)deleteMilkTakings_MilkingSessionList.getSelectedValue();
            int milkYield = milkTakings.get(session).getMilkYield();
            updateYield(deleteMilkTakings_YieldTxt, milkYield);
        }
    }//GEN-LAST:event_deleteMilkTakings_MilkingSessionListValueChanged

    private void updateMilkTakings_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_updateMilkTakings_FarmListValueChanged
    {//GEN-HEADEREND:event_updateMilkTakings_FarmListValueChanged
        // if a farm is selected..
        if ((Farm)updateMilkTakings_FarmList.getSelectedValue() != null)
        {
            Farm farm = (Farm)updateMilkTakings_FarmList.getSelectedValue();
            updateHerdList(updateMilkTakings_HerdList, farm);
            clearList(updateMilkTakings_CowList);
            clearList(updateMilkTakings_MilkingSessionList);
            clearField(updateMilkTakings_ExistingYieldTxt);
        }
    }//GEN-LAST:event_updateMilkTakings_FarmListValueChanged

    private void updateMilkTakings_HerdListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_updateMilkTakings_HerdListValueChanged
    {//GEN-HEADEREND:event_updateMilkTakings_HerdListValueChanged
        // if a herd is selected..
        if ((Herd)updateMilkTakings_HerdList.getSelectedValue() != null)
        {
            Herd herd = (Herd)updateMilkTakings_HerdList.getSelectedValue();
            updateCowList(updateMilkTakings_CowList, herd);
            clearList(updateMilkTakings_MilkingSessionList);
            clearField(updateMilkTakings_ExistingYieldTxt);
        }
    }//GEN-LAST:event_updateMilkTakings_HerdListValueChanged

    private void updateMilkTakings_CowListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_updateMilkTakings_CowListValueChanged
    {//GEN-HEADEREND:event_updateMilkTakings_CowListValueChanged
        // if a cow is selected..
        if ((Cow)updateMilkTakings_CowList.getSelectedValue() != null)
        {
            Cow cow = (Cow)updateMilkTakings_CowList.getSelectedValue();
            updateMilkingSessionList(updateMilkTakings_MilkingSessionList, cow);
            clearField(updateMilkTakings_ExistingYieldTxt);
        }
    }//GEN-LAST:event_updateMilkTakings_CowListValueChanged

    private void updateMilkTakings_MilkingSessionListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_updateMilkTakings_MilkingSessionListValueChanged
    {//GEN-HEADEREND:event_updateMilkTakings_MilkingSessionListValueChanged
        // if a milking session is selected..
        if ((TimePeriod)updateMilkTakings_MilkingSessionList.getSelectedValue() != null)
        {
            // .. get the associated milk yield and display it
            Cow cow = (Cow)updateMilkTakings_CowList.getSelectedValue();
            Map<TimePeriod, MilkTakings> milkTakings = maxT.getMilkTakings(cow);
            TimePeriod session = (TimePeriod)updateMilkTakings_MilkingSessionList.getSelectedValue();
            int milkYield = milkTakings.get(session).getMilkYield();
            updateYield(updateMilkTakings_ExistingYieldTxt, milkYield);
        }
    }//GEN-LAST:event_updateMilkTakings_MilkingSessionListValueChanged

    private void updateMilkTakingsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_updateMilkTakingsButtonActionPerformed
    {//GEN-HEADEREND:event_updateMilkTakingsButtonActionPerformed
        updateMilkTakings();
    }//GEN-LAST:event_updateMilkTakingsButtonActionPerformed

    private void mainScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_mainScreenComponentShown
    {//GEN-HEADEREND:event_mainScreenComponentShown
        resetFeedback();
        updateStatDisplay();
    }//GEN-LAST:event_mainScreenComponentShown

    private void addFarmScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_addFarmScreenComponentShown
    {//GEN-HEADEREND:event_addFarmScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_addFarmScreenComponentShown

    private void addHerdScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_addHerdScreenComponentShown
    {//GEN-HEADEREND:event_addHerdScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_addHerdScreenComponentShown

    private void addCowScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_addCowScreenComponentShown
    {//GEN-HEADEREND:event_addCowScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_addCowScreenComponentShown

    private void addMilkTakingsScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_addMilkTakingsScreenComponentShown
    {//GEN-HEADEREND:event_addMilkTakingsScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_addMilkTakingsScreenComponentShown

    private void updateMilkTakingsScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_updateMilkTakingsScreenComponentShown
    {//GEN-HEADEREND:event_updateMilkTakingsScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_updateMilkTakingsScreenComponentShown

    private void deleteFarmScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_deleteFarmScreenComponentShown
    {//GEN-HEADEREND:event_deleteFarmScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_deleteFarmScreenComponentShown

    private void deleteHerdScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_deleteHerdScreenComponentShown
    {//GEN-HEADEREND:event_deleteHerdScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_deleteHerdScreenComponentShown

    private void deleteCowScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_deleteCowScreenComponentShown
    {//GEN-HEADEREND:event_deleteCowScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_deleteCowScreenComponentShown

    private void deleteMilkTakingsScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_deleteMilkTakingsScreenComponentShown
    {//GEN-HEADEREND:event_deleteMilkTakingsScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_deleteMilkTakingsScreenComponentShown

    private void addScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_addScreenComponentShown
    {//GEN-HEADEREND:event_addScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_addScreenComponentShown

    private void updateScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_updateScreenComponentShown
    {//GEN-HEADEREND:event_updateScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_updateScreenComponentShown

    private void deleteScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_deleteScreenComponentShown
    {//GEN-HEADEREND:event_deleteScreenComponentShown
        resetFeedback();
    }//GEN-LAST:event_deleteScreenComponentShown

    private void yieldTableScreenComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_yieldTableScreenComponentShown
    {//GEN-HEADEREND:event_yieldTableScreenComponentShown
        // do nothing
    }//GEN-LAST:event_yieldTableScreenComponentShown

    private void main_FarmListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_main_FarmListValueChanged
    {//GEN-HEADEREND:event_main_FarmListValueChanged
        updateStatDisplay();
    }//GEN-LAST:event_main_FarmListValueChanged

    private void yieldTable_20_8_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_20_8_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_20_8_PMFocusLost
        updateYieldValue(yieldTable_20_8_PM, "20,(8,16),PM");
    }//GEN-LAST:event_yieldTable_20_8_PMFocusLost

    private void yieldTable_20_8_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_20_8_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_20_8_AMFocusLost
        updateYieldValue(yieldTable_20_8_AM, "20,(8,16),AM");
    }//GEN-LAST:event_yieldTable_20_8_AMFocusLost

    private void yieldTable_20_9_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_20_9_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_20_9_PMFocusLost
        updateYieldValue(yieldTable_20_9_PM, "20,(9,15),PM");
    }//GEN-LAST:event_yieldTable_20_9_PMFocusLost

    private void yieldTable_20_9_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_20_9_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_20_9_AMFocusLost
        updateYieldValue(yieldTable_20_9_AM, "20,(9,15),AM");
    }//GEN-LAST:event_yieldTable_20_9_AMFocusLost

    private void yieldTable_21_8_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_21_8_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_21_8_PMFocusLost
        updateYieldValue(yieldTable_21_8_PM, "21,(8,16),PM");
    }//GEN-LAST:event_yieldTable_21_8_PMFocusLost

    private void yieldTable_21_8_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_21_8_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_21_8_AMFocusLost
        updateYieldValue(yieldTable_21_8_AM, "21,(8,16),AM");
    }//GEN-LAST:event_yieldTable_21_8_AMFocusLost

    private void yieldTable_21_9_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_21_9_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_21_9_PMFocusLost
        updateYieldValue(yieldTable_21_9_PM, "21,(9,15),PM");
    }//GEN-LAST:event_yieldTable_21_9_PMFocusLost

    private void yieldTable_21_9_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_21_9_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_21_9_AMFocusLost
        updateYieldValue(yieldTable_21_9_AM, "21,(9,15),AM");
    }//GEN-LAST:event_yieldTable_21_9_AMFocusLost

    private void yieldTable_22_8_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_22_8_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_22_8_PMFocusLost
        updateYieldValue(yieldTable_22_8_PM, "22,(8,16),PM");
    }//GEN-LAST:event_yieldTable_22_8_PMFocusLost

    private void yieldTable_22_8_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_22_8_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_22_8_AMFocusLost
        updateYieldValue(yieldTable_22_8_AM, "22,(8,16),AM");
    }//GEN-LAST:event_yieldTable_22_8_AMFocusLost

    private void yieldTable_22_9_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_22_9_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_22_9_PMFocusLost
        updateYieldValue(yieldTable_22_9_PM, "22,(9,15),PM");
    }//GEN-LAST:event_yieldTable_22_9_PMFocusLost

    private void yieldTable_22_9_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_22_9_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_22_9_AMFocusLost
        updateYieldValue(yieldTable_22_9_AM, "22,(9,15),AM");
    }//GEN-LAST:event_yieldTable_22_9_AMFocusLost

    private void yieldTable_23_8_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_23_8_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_23_8_PMFocusLost
        updateYieldValue(yieldTable_23_8_PM, "23,(8,16),PM");
    }//GEN-LAST:event_yieldTable_23_8_PMFocusLost

    private void yieldTable_23_8_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_23_8_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_23_8_AMFocusLost
        updateYieldValue(yieldTable_23_8_AM, "23,(8,16),AM");
    }//GEN-LAST:event_yieldTable_23_8_AMFocusLost

    private void yieldTable_23_9_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_23_9_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_23_9_PMFocusLost
        updateYieldValue(yieldTable_23_9_PM, "23,(9,15),PM");
    }//GEN-LAST:event_yieldTable_23_9_PMFocusLost

    private void yieldTable_23_9_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_23_9_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_23_9_AMFocusLost
        updateYieldValue(yieldTable_23_9_AM, "23,(9,15),AM");
    }//GEN-LAST:event_yieldTable_23_9_AMFocusLost

    private void yieldTable_24_8_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_24_8_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_24_8_PMFocusLost
        updateYieldValue(yieldTable_24_8_PM, "24,(8,16),PM");
    }//GEN-LAST:event_yieldTable_24_8_PMFocusLost

    private void yieldTable_24_8_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_24_8_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_24_8_AMFocusLost
        updateYieldValue(yieldTable_24_8_AM, "24,(8,16),AM");
    }//GEN-LAST:event_yieldTable_24_8_AMFocusLost

    private void yieldTable_24_9_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_24_9_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_24_9_PMFocusLost
        updateYieldValue(yieldTable_24_9_PM, "24,(9,15),PM");
    }//GEN-LAST:event_yieldTable_24_9_PMFocusLost

    private void yieldTable_24_9_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_24_9_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_24_9_AMFocusLost
        updateYieldValue(yieldTable_24_9_AM, "24,(9,15),AM");
    }//GEN-LAST:event_yieldTable_24_9_AMFocusLost

    private void yieldTable_25_8_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_25_8_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_25_8_PMFocusLost
        updateYieldValue(yieldTable_25_8_PM, "25,(8,16),PM");
    }//GEN-LAST:event_yieldTable_25_8_PMFocusLost

    private void yieldTable_25_8_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_25_8_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_25_8_AMFocusLost
        updateYieldValue(yieldTable_25_8_AM, "25,(8,16),AM");
    }//GEN-LAST:event_yieldTable_25_8_AMFocusLost

    private void yieldTable_25_9_PMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_25_9_PMFocusLost
    {//GEN-HEADEREND:event_yieldTable_25_9_PMFocusLost
        updateYieldValue(yieldTable_25_9_PM, "25,(9,15),PM");
    }//GEN-LAST:event_yieldTable_25_9_PMFocusLost

    private void yieldTable_25_9_AMFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_yieldTable_25_9_AMFocusLost
    {//GEN-HEADEREND:event_yieldTable_25_9_AMFocusLost
        updateYieldValue(yieldTable_25_9_AM, "25,(9,15),AM");
    }//GEN-LAST:event_yieldTable_25_9_AMFocusLost

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Windows".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(MaxTGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(MaxTGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(MaxTGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(MaxTGui.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new MaxTGui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCowButton;
    private javax.swing.JPanel addCowScreen;
    private m256gui.M256JList addCow_FarmList;
    private m256gui.M256JList addCow_HerdList;
    private javax.swing.JTextField addCow_IdField;
    private javax.swing.JButton addFarmButton;
    private javax.swing.JPanel addFarmScreen;
    private javax.swing.JTextField addFarm_IdField;
    private javax.swing.JTextField addFarm_LocationField;
    private javax.swing.JTextField addFarm_NameField;
    private javax.swing.JButton addHerdButton;
    private javax.swing.JPanel addHerdScreen;
    private m256gui.M256JList addHerd_FarmList;
    private javax.swing.JTextField addHerd_IdField;
    private javax.swing.JRadioButton addHerd_MilkIntervalRadio1;
    private javax.swing.JRadioButton addHerd_MilkIntervalRadio2;
    private javax.swing.ButtonGroup addHerd_MilkingIntervalsGroup;
    private javax.swing.JTextField addHerd_NameField;
    private javax.swing.JButton addMilkTakingsButton;
    private javax.swing.JPanel addMilkTakingsScreen;
    private m256gui.M256JList addMilkTakings_CowList;
    private m256gui.M256JList addMilkTakings_FarmList;
    private m256gui.M256JList addMilkTakings_HerdList;
    private javax.swing.JRadioButton addMilkTakings_MilkSessionRadio1;
    private javax.swing.JRadioButton addMilkTakings_MilkSessionRadio2;
    private javax.swing.ButtonGroup addMilkTakings_MilkingSessionGroup;
    private javax.swing.JTextField addMilkTakings_YieldField;
    private javax.swing.JPanel addScreen;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton deleteCowButton;
    private javax.swing.JPanel deleteCowScreen;
    private m256gui.M256JList deleteCow_CowList;
    private m256gui.M256JList deleteCow_FarmList;
    private m256gui.M256JList deleteCow_HerdList;
    private javax.swing.JButton deleteFarmButton;
    private javax.swing.JPanel deleteFarmScreen;
    private m256gui.M256JList deleteFarm_FarmList;
    private javax.swing.JButton deleteHerdButton;
    private javax.swing.JPanel deleteHerdScreen;
    private m256gui.M256JList deleteHerd_FarmList;
    private m256gui.M256JList deleteHerd_HerdList;
    private javax.swing.JButton deleteMilkTakingsButton;
    private javax.swing.JPanel deleteMilkTakingsScreen;
    private m256gui.M256JList deleteMilkTakings_CowList;
    private m256gui.M256JList deleteMilkTakings_FarmList;
    private m256gui.M256JList deleteMilkTakings_HerdList;
    private m256gui.M256JList deleteMilkTakings_MilkingSessionList;
    private javax.swing.JTextField deleteMilkTakings_YieldTxt;
    private javax.swing.JPanel deleteScreen;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField feedbackTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JPanel mainScreen;
    private m256gui.M256JList main_FarmList;
    private javax.swing.JTextArea main_statDisplay;
    private javax.swing.JButton updateMilkTakingsButton;
    private javax.swing.JPanel updateMilkTakingsScreen;
    private m256gui.M256JList updateMilkTakings_CowList;
    private javax.swing.JTextField updateMilkTakings_ExistingYieldTxt;
    private m256gui.M256JList updateMilkTakings_FarmList;
    private m256gui.M256JList updateMilkTakings_HerdList;
    private m256gui.M256JList updateMilkTakings_MilkingSessionList;
    private javax.swing.JTextField updateMilkTakings_NewYieldField;
    private javax.swing.JPanel updateScreen;
    private javax.swing.JPanel yieldTableScreen;
    private javax.swing.JTextField yieldTable_20_8_AM;
    private javax.swing.JTextField yieldTable_20_8_PM;
    private javax.swing.JTextField yieldTable_20_9_AM;
    private javax.swing.JTextField yieldTable_20_9_PM;
    private javax.swing.JTextField yieldTable_21_8_AM;
    private javax.swing.JTextField yieldTable_21_8_PM;
    private javax.swing.JTextField yieldTable_21_9_AM;
    private javax.swing.JTextField yieldTable_21_9_PM;
    private javax.swing.JTextField yieldTable_22_8_AM;
    private javax.swing.JTextField yieldTable_22_8_PM;
    private javax.swing.JTextField yieldTable_22_9_AM;
    private javax.swing.JTextField yieldTable_22_9_PM;
    private javax.swing.JTextField yieldTable_23_8_AM;
    private javax.swing.JTextField yieldTable_23_8_PM;
    private javax.swing.JTextField yieldTable_23_9_AM;
    private javax.swing.JTextField yieldTable_23_9_PM;
    private javax.swing.JTextField yieldTable_24_8_AM;
    private javax.swing.JTextField yieldTable_24_8_PM;
    private javax.swing.JTextField yieldTable_24_9_AM;
    private javax.swing.JTextField yieldTable_24_9_PM;
    private javax.swing.JTextField yieldTable_25_8_AM;
    private javax.swing.JTextField yieldTable_25_8_PM;
    private javax.swing.JTextField yieldTable_25_9_AM;
    private javax.swing.JTextField yieldTable_25_9_PM;
    // End of variables declaration//GEN-END:variables
}
