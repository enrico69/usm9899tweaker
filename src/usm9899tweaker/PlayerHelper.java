/**
 * A class containing tools for players.
 * Is a singleton
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.io.IOException;
import tools.files.BinaryFileHelper;

public class PlayerHelper implements PlayerManagerInterface {
    
    private static PlayerHelper instance = null;
    
    /**
     * Constructor
     * Exists only to defeat instantiation.
     */
    protected void PlayerHelper() {
    }
    
    /**
     * Get instance of this class
     * @return PlayerHelper
     */
    public static PlayerHelper getInstance() {
        if(instance == null) {
            instance = new PlayerHelper();
        }
        
        return instance;
    }
    
    /**
     * Instanciate a player
     * @return a Player
     */
    @Override
    public Player createPlayer() {
        Player thePlayer = new Player(this);
        
        return thePlayer;
    }
    
    /**
     * Validate set value on player skills
     * @param value
     * @param skillName
     * @throws SkillValueException 
     */
    @Override
    public void validateSkill(int value, String skillName) throws SkillValueException {
        int maxValue = 99;
        
        if(skillName.contentEquals("FT")) {
            maxValue = 100;
        }
        
        if(value < 1 || value > maxValue) {
            throw new SkillValueException(SkillValueException.generateMessage(value, skillName));
        }
    }
    
    /**
     * Instanciate a player form its Hex value
     * 
     * @param startPosition
     * @param lastNameLength
     * @param hexValue
     * @param maxLength
     * @return
     * @throws SkillValueException 
     */
    @Override
    public Player fillPlayer(long startPosition, int lastNameLength, byte[] hexValue, int maxLength) throws SkillValueException {
        Player thePlayer = this.createPlayer();
        thePlayer.setHexPosition(startPosition);
        
        //-----------
        // Names
        //-----------
        
        // First name
        int position = 0;
        int length = 12;
        if(position < length) {
            byte[] byteFirstName = new byte[length];
            while(position < length) {
                byteFirstName[position] = hexValue[position];
                position++;
            }
            thePlayer.setFirstName(BinaryFileHelper.getInstance().binaryToText(byteFirstName));
        }

        // Last name
        position += 2;
        int lastNameStart = position;
        length += 2;
        length = length + lastNameLength;
        if(position < length) {
            byte[] byteLastName = new byte[length];
            while(position < length) {
                byteLastName[position] = hexValue[position];
                position++;
            }
            thePlayer.setLastName(BinaryFileHelper.getInstance().binaryToText(byteLastName));
        }

        //-----------
        // Skills
        //-----------
        position = lastNameStart + 20;
        int maxPosition = position + maxLength;
        boolean skillsLocationFound = false;
        
        while(position < maxPosition && !skillsLocationFound) {
            try {
                if(hexValue.length - position >= 10) { 
                    this.setSkills(thePlayer, position, hexValue);
                    skillsLocationFound = true;
                    thePlayer.setSkillStartPosition(startPosition + position);
                } else {
                    throw new SkillValueException("End of chunck");
                }
            } catch(SkillValueException ex) {
                if(position >= maxPosition) {
                    throw ex;
                }
            }
            
            position++;
        }
        
        if(!skillsLocationFound) {
            throw new SkillValueException("Impossible to find compatible skills data sequence");
        }
        
            
        return thePlayer;
    }
    
    /**
     * Set the player skills
     * @param thePlayer
     * @param position
     * @param hexValue
     * @throws SkillValueException 
     */
    private void setSkills(Player thePlayer, int position, byte[] hexValue) throws SkillValueException {
        thePlayer.setKP(hexValue[position]); position++;
        thePlayer.setTA(hexValue[position]); position++;
        thePlayer.setPS(hexValue[position]); position++;
        thePlayer.setSH(hexValue[position]); position++;
        thePlayer.setPC(hexValue[position]); position++;
        thePlayer.setFT(hexValue[position]); position++;
        thePlayer.setHE(hexValue[position]); position++;
        thePlayer.setST(hexValue[position]); position++;
        thePlayer.setSP(hexValue[position]); position++;
        thePlayer.setBC(hexValue[position]); position++;
    }
    
    /**
     * Save player
     * @param thePlayer
     * @throws IOException 
     */
    @Override
    public void savePlayer(Player thePlayer) throws IOException {
        long skillsStartPosition = thePlayer.getSkillStartPosition();
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getKP()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getTA()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getPS()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getSH()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getPC()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getFT()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getHE()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getST()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getSP()); skillsStartPosition++;
        
        BinaryFileHelper.getInstance().goToByte(skillsStartPosition);
        BinaryFileHelper.getInstance().writeIntValue(thePlayer.getBC()); skillsStartPosition++;
    }
}
