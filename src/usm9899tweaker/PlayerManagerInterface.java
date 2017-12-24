/*
 * Interface for the players manager/factory
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.io.IOException;

public interface PlayerManagerInterface {
    
    /**
     * 
     * @param value
     * @param skillName
     * @throws SkillValueException 
     */
    public void validateSkill(int value, String skillName) throws SkillValueException;
    
    /**
     * 
     * @param startPosition
     * @param lastNameLength
     * @param hexValue
     * @param maxLength
     * @return
     * @throws SkillValueException 
     */
    public Player fillPlayer(long startPosition, int lastNameLength, byte[] hexValue, int maxLength) throws SkillValueException;
    
    /**
     * 
     * @param thePlayer
     * @throws IOException 
     */
    public void savePlayer(Player thePlayer) throws IOException;
    
    /**
     * 
     * @return 
     */
    public Player createPlayer();
    
}
