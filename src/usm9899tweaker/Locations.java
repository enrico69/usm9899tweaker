/**
 * Contains all the locations inside the executable file of the game
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.util.ArrayList;
import java.util.List;

public class Locations {
    
    // Reference Hash
    final public static String REFERENCE_HASH = "bf30d9c37e7aa85826ff2c11212704186757f84b";
 
    // Max qty of loans
    final public static long MAX_LOANS_QTY_FIRST = 1005885;
    final public static long MAX_LOANS_QTY_SECOND = 1005932;
    
    final public static long MAX_LOAN_QTY_STRING_FIRST = 1386256;
    final public static long MAX_LOAN_QTY_STRING_SECOND = 1386416;
    
    // Enable long-term loans
    final public static long ENABLE_LOAN_FIRST = 182391;
    final public static long ENABLE_LOAN_SECOND = 1004873;
    final public static long ENABLE_LOAN_THIRD = 1004880;
    final public static long ENABLE_LOAN_FOURTH = 1004887;
    final public static long ENABLE_LOAN_FIFTH = 1004894;
    final public static long ENABLE_LOAN_SIXTH = 1004908;
    
    // Cheat menu
    final public static long CHEAT_MENU = 608711;
    
    // Starting year locations
    private static List<Integer> STARTING_YEAR;
    private static List<Integer> STARTING_YEAR_AFTER;
    private static List<Integer> AGE_YEAR_REFERENCE;

    // Starting year locations
    private static List<Integer> TRANSFERT_MODE;

    /**
     * Return the position of the starting year spots
     * @return ArrayList
     */    
    public static List<Integer> getStartingYearPositions() {
        
       if(Locations.STARTING_YEAR == null) {
            Locations.STARTING_YEAR = new ArrayList<>();

            // Calendar
            Locations.STARTING_YEAR.add(46035);
            Locations.STARTING_YEAR.add(46195);
            Locations.STARTING_YEAR.add(427695);
            
            // Annual Report
            Locations.STARTING_YEAR.add(209796);
            
            // Stadium construction
            Locations.STARTING_YEAR.add(735817);
            
            // Manager joining date
            Locations.STARTING_YEAR.add(917810);
            
            // Club history and current league report
            Locations.STARTING_YEAR.add(890658); 
            
            // Sponsor
            Locations.STARTING_YEAR.add(721116);
        }
        
        return Locations.STARTING_YEAR;
    }
    
    /**
     * Return the position of the starting year +1 spots
     * For example, if you choose to start in 2017, the below
     * spots will contain 2018
     * 
     * @return ArrayList
     */    
    public static List<Integer> getStartingYearAfterPositions() {
        
       if(Locations.STARTING_YEAR_AFTER == null) {
            Locations.STARTING_YEAR_AFTER = new ArrayList<>();

            // Annual report
            Locations.STARTING_YEAR_AFTER.add(209785);
            
            // Club history and current league report
            Locations.STARTING_YEAR_AFTER.add(890690);
        }
        
        return Locations.STARTING_YEAR_AFTER;
    }
    
    /**
     * Return the position of the year of reference (1900 by default)
     * for the young players
     * 
     * @return ArrayList
     */    
    public static List<Integer> getAgeYearReferencePositions() {
        
       if(Locations.AGE_YEAR_REFERENCE == null) {
            Locations.AGE_YEAR_REFERENCE = new ArrayList<>();

            Locations.AGE_YEAR_REFERENCE.add(894399);
            Locations.AGE_YEAR_REFERENCE.add(305436);
        }
        
        return Locations.AGE_YEAR_REFERENCE;
    }
    
    /**
     * Return the position of the transfert mode spots
     * @return ArrayList
     */    
    public static List<Integer> getTransfertModePositions() {
        
        if(Locations.TRANSFERT_MODE == null) {
            Locations.TRANSFERT_MODE = new ArrayList<>();
            
            Locations.TRANSFERT_MODE.add(241121);
            Locations.TRANSFERT_MODE.add(253726);
            Locations.TRANSFERT_MODE.add(265260);
            Locations.TRANSFERT_MODE.add(533109);
            Locations.TRANSFERT_MODE.add(578628);
            Locations.TRANSFERT_MODE.add(954265);
        }
        
        return Locations.TRANSFERT_MODE;
    }
}
