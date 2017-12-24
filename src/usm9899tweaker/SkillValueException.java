/**
 * Exception when a player skill value is less than 1 and more than 99
 * (100 for fitness).
 * @author Eric COURTIAL
 */
package usm9899tweaker;

public class SkillValueException extends Exception {
    
    /**
     * Constructor
     * @param message 
     */
    public SkillValueException(String message) {
        super(message);
    }
    
    /**
     * Generate the message
     * @param value
     * @param message
     * @return 
     */
    public static String generateMessage(int value, String message) {
        System.out.println("For the attribute " + message + " you set" +
            " a value of " + value + ". Remember that for all skills" +
            " the min value is 1 and the max value is 99 (100 for the fitness)");
        
        return "For the attribute " + message + " you set" +
            " a value of " + value + ". Remember that for all skills" +
            " the min value is 1 and the max value is 99 (100 for the fitness)";
    }
}