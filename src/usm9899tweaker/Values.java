/**
 * References values in the original .exe of the game
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.util.ArrayList;
import java.util.List;

public class Values {
    
    /* LONG TERM LOANS */
    final public static int ENABLE_LOAN_FIRST_REF_VALUE = 1;
    final public static int ENABLE_LOAN_SECOND_REF_VALUE = 1;
    final public static int ENABLE_LOAN_THIRD_REF_VALUE = 2;
    final public static int ENABLE_LOAN_FOURTH_REF_VALUE = 3;
    final public static int ENABLE_LOAN_FIFTH_REF_VALUE = 4;
    final public static int ENABLE_LOAN_SIXTH_REF_VALUE = 6;
    
    final public static int ENABLE_LOAN_FIRST_TARGET_VALUE = 0;
    final public static int ENABLE_LOAN_SECOND_TARGET_VALUE = 5;
    final public static int ENABLE_LOAN_THIRD_TARGET_VALUE = 5;
    final public static int ENABLE_LOAN_FOURTH_TARGET_VALUE = 5;
    final public static int ENABLE_LOAN_FIFTH_TARGET_VALUE = 5;
    final public static int ENABLE_LOAN_SIXTH_TARGET_VALUE = 5;
    
    /* CHEAT MENU */
    final public static int CHEAT_MENU_ORIGIN_VALUE = -123;
    final public static int CHEAT_MENU_TARGET_VALUE = -124;
    
    // Transfert locations
    private static List<Integer> TRANSFERT_MODE_ORIGIN;
    private static List<Integer> TRANSFERT_MODE_TARGET;
    
    /**
     * Return the original values of the transfert mode spots
     * @return ArrayList
     */    
    public static List<Integer> getTransfertOriginValues() {
        
        if(Values.TRANSFERT_MODE_ORIGIN == null) {
            Values.TRANSFERT_MODE_ORIGIN = new ArrayList<>();
            
            Values.TRANSFERT_MODE_ORIGIN.add(116);
            Values.TRANSFERT_MODE_ORIGIN.add(116);
            Values.TRANSFERT_MODE_ORIGIN.add(116);
            Values.TRANSFERT_MODE_ORIGIN.add(-123);
            Values.TRANSFERT_MODE_ORIGIN.add(116);
            Values.TRANSFERT_MODE_ORIGIN.add(116);
        }
        
        return Values.TRANSFERT_MODE_ORIGIN;
    }
    
    /**
     * Return the target values of the transfert mode spots
     * @return ArrayList
     */    
    public static List<Integer> getTransfertTargetValues() {
        
        if(Values.TRANSFERT_MODE_TARGET == null) {
            Values.TRANSFERT_MODE_TARGET = new ArrayList<>();
            
            Values.TRANSFERT_MODE_TARGET.add(117);
            Values.TRANSFERT_MODE_TARGET.add(117);
            Values.TRANSFERT_MODE_TARGET.add(117);
            Values.TRANSFERT_MODE_TARGET.add(-124);
            Values.TRANSFERT_MODE_TARGET.add(117);
            Values.TRANSFERT_MODE_TARGET.add(117);
        }
        
        return Values.TRANSFERT_MODE_TARGET;
    }
    
}
