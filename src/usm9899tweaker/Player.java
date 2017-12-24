/**
 * This class represent the player entity
 * @author Eric COURTIAL
 */
package usm9899tweaker;

public class Player {
    
    final private PlayerManagerInterface Helper;
    
    // String attributes
    
    private String firstName = "";
    private String lastName = "";
    
    // Visible attributes
    private int KP = 0; // Keeping
    private int TA = 0; // Tackling
    private int PS = 0; // Pass
    private int SH = 0; // Shoot
    private int PC = 0; // Speed
    private int FT = 0; // Fitness
    private int HE = 0; // Head
    private int ST = 0; // Stamina
    private int SP = 0; // Set play (creativity)
    private int BC = 0; // Ball control (technique)
    
    private int age = 0;
    private int dayOfBirth = 0;
    private int monthOfBirth = 0;
    private int yearOfBirth = 0;
    private String side = "All";
    
    // Hidden attributes
    private int development = 0;
    
    // Hex start position
    private long hexPosition = 0;
    
    // Skill start position
    private long skillStartPosition = 0;

    /**
     * Constructor
     * @param helper
     */
    public Player (PlayerManagerInterface helper) {
        this.Helper = helper;
    }

    //-------------------------------------
    // Getters and Setters
    //-------------------------------------
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    public int getKP() {
        return KP;
    }

    public void setKP(int KP) throws SkillValueException {
        this.Helper.validateSkill(KP, "KP");
        this.KP = KP;
    }

    public int getTA() {
        return TA;
    }

    public void setTA(int TA) throws SkillValueException {
        this.Helper.validateSkill(TA, "TA");
        this.TA = TA;
    }

    public int getPS() {
        return PS;
    }

    public void setPS(int PS) throws SkillValueException {
        this.Helper.validateSkill(PS, "PS");
        this.PS = PS;
    }

    public int getSH() {
        return SH;
    }

    public void setSH(int SH) throws SkillValueException {
        this.Helper.validateSkill(SH, "SH");
        this.SH = SH;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) throws SkillValueException {
        this.Helper.validateSkill(PC, "PC");
        this.PC = PC;
    }

    public int getFT() {
        return FT;
    }

    public void setFT(int FT) throws SkillValueException {
        this.Helper.validateSkill(FT, "FT");
        this.FT = FT;
    }

    public int getHE() {
        return HE;
    }

    public void setHE(int HE) throws SkillValueException {
        this.Helper.validateSkill(HE, "HE");
        this.HE = HE;
    }

    public int getST() {
        return ST;
    }

    public void setST(int ST) throws SkillValueException {
        this.Helper.validateSkill(ST, "ST");
        this.ST = ST;
    }

    public int getSP() {
        return SP;
    }

    public void setSP(int SP) throws SkillValueException {
        this.Helper.validateSkill(SP, "SP");
        this.SP = SP;
    }

    public int getBC() {
        return BC;
    }

    public void setBC(int BC) throws SkillValueException {
        this.Helper.validateSkill(BC, "BC");
        this.BC = BC;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(int dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public int getMonthOfBirth() {
        return monthOfBirth;
    }

    public void setMonthOfBirth(int monthOfBirth) {
        this.monthOfBirth = monthOfBirth;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public int getDevelopment() {
        return development;
    }

    public void setDevelopment(int development) {
        this.development = development;
    }

    public long getHexPosition() {
        return hexPosition;
    }

    public void setHexPosition(long hexPosition) {
        this.hexPosition = hexPosition;
    }

    public long getSkillStartPosition() {
        return skillStartPosition;
    }

    public void setSkillStartPosition(long skillStartPosition) {
        this.skillStartPosition = skillStartPosition;
    }
    
}
