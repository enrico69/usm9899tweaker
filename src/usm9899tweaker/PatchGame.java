/*
 * Class of the patch game section
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import tools.files.BinaryFileHelper;

public class PatchGame implements ActionListener {

    /* Display elements */
    private final JFrame parent;
    private JFrame patchScreen;
    final private JButton patchLoanButton = new JButton("");
    final private JButton saveLoanQtyButton = new JButton("Save");
    final private JTextField maxLoanQtyValue = new JTextField();
    final private JButton saveStartingYearButton = new JButton("Save");
    final private JTextField startingYearValue = new JTextField();
    final private JButton cheatMenuButton = new JButton("");
    final private JButton transfertModeButton = new JButton("");
    
    /* Variables use for processing */
    private boolean firstTime = false;
    private String filename = "";
    private String dir = "";

    private boolean areLongTermLoansEnabled = false;
    private boolean isCheatMenuEnabled = false;
    private boolean transfertStandardMode = true;
      
    /**
     * Constructor
     * @param mainMenu 
     */
    public PatchGame(JFrame mainMenu) {
        this.parent = mainMenu;
    }
    
    /**
     * Log stuff
     * @param message 
     */
    private void log(String message) {
        System.out.println(message);
    }
    
    /**
     * Launch the patch window
     * @throws java.io.IOException
     */
    public void launch() throws IOException, Exception {
        this.parent.setEnabled(false);
        
        // Ask if it is the first time
        String question = "Do you want to check that you have the proper version of USM? Tip: do it the first time only.";
        int result = JOptionPane.showConfirmDialog(this.parent, question, "Question", JOptionPane.YES_NO_OPTION);
        this.firstTime = (result != 1);
        this.log("First time : " + this.firstTime);
        
        // File chooser
        JFileChooser c = new JFileChooser();
        int response = c.showOpenDialog(parent);
        
        boolean validFile = true;
        if (response == JFileChooser.APPROVE_OPTION) {
            this.filename = c.getSelectedFile().getName();
            this.dir = c.getCurrentDirectory().toString();
            this.filename = this.dir + File.separator + this.filename;
            this.log(this.filename);

            if (this.firstTime) {
                validFile = this.testFile(this.filename);
            }
            if (validFile) {
                // Starting reading current configuration...
                BinaryFileHelper.getInstance().setFilePath(this.filename);
                BinaryFileHelper.getInstance().openFile();
                
                // Checking if long-term loans are enabled
                this.checkIfLongTermLoansAreEnabled();
                
                // Getting the max loan qty current settings
                byte[] lFirstQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.MAX_LOANS_QTY_FIRST, 1);
                this.log("First qty loan value----> " + lFirstQtyLoanVal[0]);
                byte[] lSecondQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.MAX_LOANS_QTY_SECOND, 1);
                this.log("Second qty loan value----> " + lSecondQtyLoanVal[0]);
                
                if(lFirstQtyLoanVal[0] != lSecondQtyLoanVal[0]) {
                    throw new Exception("Sorry, but it seems that your file is invalid as there is a difference between the max loan quantity configuration");
                }
                
                // Getting the starting year
                int startingYear = this.getStartingYear();
                
                // Checking if cheat menu is enabled
                this.checkIfCheatMenuIsEnabled();
                
                // Check transfert mode
                this.checkIfTransfertStandardMode();
                
                // Opening the patching panel
                this.openPanel(lFirstQtyLoanVal[0], startingYear);
            }
        } else {
            validFile = false;
        }
        
        if(!validFile) {
            this.parent.setEnabled(true);
            this.parent.requestFocus();
        }
    }
    
    /**
     * Compare the checksum of the selected file with the USM98-99 ENG version
     * @param filename
     * @return boolean
     */
    private boolean testFile(String filename) {
        boolean result = false;
        String errorMsg = "Sorry: impossible to check the checksum";
        
        try {
            BinaryFileHelper.getInstance().setFilePath(filename);
            if(BinaryFileHelper.getInstance().getFileCheckSum().contentEquals(Locations.REFERENCE_HASH)) {
                JOptionPane.showMessageDialog(this.parent, "You have the good version of the game");
                result = true;
            } else {
                JOptionPane.showMessageDialog(this.parent, "The file is different. However you can stil try to patch it");
            }
        } catch (NoSuchAlgorithmException | IOException ex) {
            JOptionPane.showMessageDialog(this.parent, errorMsg);
            Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    /**
     * Open the patching panel
     * @param maxLoanQty is the max qty of players you can loan
     * @param startingYear is the starting year
     */
    private void openPanel(int maxLoanQty, int startingYear) {

        // Screen definition
        this.patchScreen = new JFrame();
        this.patchScreen.setTitle("Patch game");
        this.patchScreen.setSize(700, 230);
        this.patchScreen.setLocation(250, 250);
        this.patchScreen.setResizable(false);
        ImageIcon img = new ImageIcon(MainMenu.iconName);
        this.patchScreen.setIconImage(img.getImage());
        
        Container windowContent = this.patchScreen.getContentPane();
        windowContent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        //--------------------------------
        // Loan duration section
        //--------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        JLabel longTermLoanLabel = new JLabel("Enable / Disable long-term loans (up to 99 weeks)");
        windowContent.add(longTermLoanLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 0;
        
        this.updateLongTermButtonLabel();
        this.patchLoanButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.patchLoanButton, c);
        this.patchLoanButton.addActionListener((ActionListener) this);
        
        //--------------------------------
        // Loan qty section
        //--------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel maxLoanQtyLabel = new JLabel("Max quantity of loan per season (max 9)");
        c.gridx = 0;
        c.gridy = 1;
        windowContent.add(maxLoanQtyLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        this.maxLoanQtyValue.setText(String.valueOf(maxLoanQty));
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        windowContent.add(this.maxLoanQtyValue, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 1;
        
        this.saveLoanQtyButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.saveLoanQtyButton, c);
        this.saveLoanQtyButton.addActionListener((ActionListener) this);
        
        //--------------------------------
        // Start year
        //--------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel startingYearLabel = new JLabel("Starting year (min 1792 max 2047)");
        c.gridx = 0;
        c.gridy = 2;
        windowContent.add(startingYearLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        this.startingYearValue.setText(String.valueOf(startingYear));
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 2;
        windowContent.add(this.startingYearValue, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 2;
        
        this.saveStartingYearButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.saveStartingYearButton, c);
        
        this.saveStartingYearButton.addActionListener((ActionListener) this);
        
        //-----------------------------------------
        // Cheat Menu
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        JLabel cheatMenuLabel = new JLabel("Enable / Disable cheat menu");
        windowContent.add(cheatMenuLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 3;
        
        this.updateCheatButtonLabel();
        this.cheatMenuButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.cheatMenuButton, c);
        this.cheatMenuButton.addActionListener((ActionListener) this);
        
        //-----------------------------------------
        // Transfert mode
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        JLabel transfertMenuLabel = new JLabel("Switch transfert mode");
        windowContent.add(transfertMenuLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 4;
        
        this.updateTransfertButtonLabel();
        this.transfertModeButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.transfertModeButton, c);
        this.transfertModeButton.addActionListener((ActionListener) this);
        
        //-----------------------------------------
        // Display
        //-----------------------------------------
        this.patchScreen.setVisible(true);
        this.patchScreen.addWindowListener( new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent we) 
            {
                try {
                    BinaryFileHelper.getInstance().closeFile();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(parent, "Sorry, impossible to release the file. Please close this application before launching the game", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                parent.setEnabled(true);
                parent.requestFocus();
            }
        } );
        
    }
    
    /**
     * Activate / Desactivate buttons and others forms elements
     * @param status 
     */
    private void changeFormStatus(boolean status) {
        this.patchLoanButton.setEnabled(status);
        this.saveLoanQtyButton.setEnabled(status);
        this.maxLoanQtyValue.setEnabled(status);
    }
    
    /**
     * Handle listeners on buttons
     * @param ev 
     */
    @Override
    public void actionPerformed (ActionEvent ev) {
     
        // Handle of the modification of the quantity of loaned players
        if(ev.getSource() == this.saveLoanQtyButton)
        {
            this.changeFormStatus(false);
            try {
                this.updateLoanQty(Integer.parseInt(this.maxLoanQtyValue.getText()));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }
        
        // Handle of the activation of the long term loans
        if(ev.getSource() == this.patchLoanButton)
        {
            this.changeFormStatus(false);
            try {
                if(this.areLongTermLoansEnabled) {
                    this.disableLongTermLoans();
                } else {
                    this.enableLongTermLoans();
                }
                
                this.checkIfLongTermLoansAreEnabled();
                this.updateLongTermButtonLabel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }
        
        // Handle the starting year
        if(ev.getSource() == this.saveStartingYearButton)
        {   
            this.changeFormStatus(false);
            int date = Integer.parseInt(this.startingYearValue.getText());
            if(this.checkDateInterval(date)) {
                try {
                    this.setStartingDate(date);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, the year must be in the interval 1792 - 2047");
            }
            this.changeFormStatus(true);
        }
        
        // Handle of the activation of the cheat menu
        if(ev.getSource() == this.cheatMenuButton)
        {
            this.changeFormStatus(false);
            try {
                if(this.isCheatMenuEnabled) {
                    this.disableCheatMenu();
                } else {
                    this.enableCheatMenu();
                }
                
                this.checkIfCheatMenuIsEnabled();
                this.updateCheatButtonLabel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }
        
        // Handle of transfert mode switching
        if(ev.getSource() == this.transfertModeButton)
        {
            this.changeFormStatus(false);
            try {
                if(this.transfertStandardMode) {
                    this.disableTransferts();
                } else {
                    this.enableTransferts();
                }
                
                this.checkIfTransfertStandardMode();
                this.updateTransfertButtonLabel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }
    }
    
    /**
     * Update the maximum quantity of player you can loan
     * @param qty
     * @throws IOException 
     */
    private void updateLoanQty(int qty) throws IOException {
        if(qty > 9 || qty < 0) {
            JOptionPane.showMessageDialog(this.patchScreen, "Sorry, the quantity of player you can loan must be in the interval 0 - 9");
        } else {
            BinaryFileHelper.getInstance().goToByte(Locations.MAX_LOANS_QTY_FIRST);
            BinaryFileHelper.getInstance().writeIntValue(qty);
            BinaryFileHelper.getInstance().goToByte(Locations.MAX_LOANS_QTY_SECOND);
            BinaryFileHelper.getInstance().writeIntValue(qty);
            BinaryFileHelper.getInstance().goToByte(Locations.MAX_LOAN_QTY_STRING_FIRST);
            BinaryFileHelper.getInstance().writeHex(BinaryFileHelper.getInstance().integerToHex(qty));
            BinaryFileHelper.getInstance().goToByte(Locations.MAX_LOAN_QTY_STRING_SECOND);
            BinaryFileHelper.getInstance().writeHex(BinaryFileHelper.getInstance().integerToHex(qty));
        }
    }
    
    /**
     * Enable long term loans
     * @throws IOException 
     */
    private void enableLongTermLoans() throws IOException {
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_FIRST);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_FIRST_TARGET_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_SECOND);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_SECOND_TARGET_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_THIRD);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_THIRD_TARGET_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_FOURTH);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_FOURTH_TARGET_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_FIFTH);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_FIFTH_TARGET_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_SIXTH);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_SIXTH_TARGET_VALUE);
    }
    
    /**
     * Disable long term loans
     * @throws IOException 
     */
    private void disableLongTermLoans() throws IOException {
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_FIRST);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_FIRST_REF_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_SECOND);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_SECOND_REF_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_THIRD);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_THIRD_REF_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_FOURTH);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_FOURTH_REF_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_FIFTH);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_FIFTH_REF_VALUE);
        BinaryFileHelper.getInstance().goToByte(Locations.ENABLE_LOAN_SIXTH);
        BinaryFileHelper.getInstance().writeIntValue(Values.ENABLE_LOAN_SIXTH_REF_VALUE);
    }
    
    /**
     * Check if the long terms loans are enabled
     * @throws java.io.IOException
     */
    private void checkIfLongTermLoansAreEnabled() throws IOException {
        boolean areEnabled = true;
        
        byte[] lFirstQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_FIRST, 1);
        if(lFirstQtyLoanVal[0] != Values.ENABLE_LOAN_FIRST_TARGET_VALUE) { areEnabled = false; }
        System.out.println("--> " + lFirstQtyLoanVal[0] +" --> " + areEnabled);
        
        byte[] lSecondQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_SECOND, 1);
        if(lSecondQtyLoanVal[0] != Values.ENABLE_LOAN_SECOND_TARGET_VALUE) { areEnabled = false; }
        System.out.println("--> " + lSecondQtyLoanVal[0] +" --> " + areEnabled);
        
        byte[] lThirdQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_THIRD, 1);
        if(lThirdQtyLoanVal[0] != Values.ENABLE_LOAN_THIRD_TARGET_VALUE) { areEnabled = false; }
        System.out.println("--> " + lThirdQtyLoanVal[0] +" --> " + areEnabled);
        
        byte[] lFourthQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_FOURTH, 1);
        if(lFourthQtyLoanVal[0] != Values.ENABLE_LOAN_FOURTH_TARGET_VALUE) { areEnabled = false; }
        System.out.println("--> " + lFourthQtyLoanVal[0] +" --> " + areEnabled);
        
        byte[] lFifthQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_FIFTH, 1);
        if(lFifthQtyLoanVal[0] != Values.ENABLE_LOAN_FIFTH_TARGET_VALUE) { areEnabled = false; }
        System.out.println("--> " + lFifthQtyLoanVal[0] +" --> " + areEnabled);
        
        byte[] lSixthQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_SIXTH, 1);
        if(lSixthQtyLoanVal[0] != Values.ENABLE_LOAN_SIXTH_TARGET_VALUE) { areEnabled = false; }
        System.out.println("--> " + lSixthQtyLoanVal[0] +" --> " + areEnabled);
        
        this.areLongTermLoansEnabled = areEnabled;
    }
    
    /**
     * Check the status of the cheat menu
     * @throws IOException 
     */
    private void checkIfCheatMenuIsEnabled() throws IOException {
        this.isCheatMenuEnabled = false;
        byte[] cheatVal = BinaryFileHelper.getInstance().getFilePart(Locations.CHEAT_MENU, 1);

        System.out.println("Cheatval read: " + cheatVal[0] +  ", Expected: " + Values.CHEAT_MENU_TARGET_VALUE + ".");      
        
        if(cheatVal[0] == Values.CHEAT_MENU_TARGET_VALUE) {
            System.out.println("Cheat values match");
            this.isCheatMenuEnabled = true;
        }
    }
    
    /**
     * Update the status of the cheat menu status button
     */
    private void updateCheatButtonLabel() {
        if(this.isCheatMenuEnabled) {
            this.cheatMenuButton.setText("Disable");
        } else {
            this.cheatMenuButton.setText("Enable");
        }
    }
    
    /**
     * Update the status of the long-term loans status button
     */
    private void updateLongTermButtonLabel() {
        if(this.areLongTermLoansEnabled) {
            this.patchLoanButton.setText("Disable");
        } else {
            this.patchLoanButton.setText("Enable");
        }
    }
    
    /**
     * Get the starting year
     * @return 
     */
    private int getStartingYear() throws Exception {
        List<Integer> startYearLocations = Locations.getStartingYearPositions();
        int previousValue = -1;
        
        for (Integer location : startYearLocations) {
            BinaryFileHelper.getInstance().goToByte(location);
            byte[] readData = BinaryFileHelper.getInstance().getFilePart(location, 1);
            
            if(previousValue != -1) {
                if(readData[0] != previousValue) {
                    throw new Exception("Sorry, there is a problem with the starting year in you .exe.\n It seems that there is different values.");
                }
            }
            previousValue = readData[0];
        }
        
        String yearValue = BinaryFileHelper.getInstance().integerToHex(previousValue);
        yearValue = yearValue.substring(yearValue.length() - 2, yearValue.length());
        yearValue = "07" + yearValue;
        System.out.println("Starting year HEX value read: " + yearValue);
        previousValue = BinaryFileHelper.getInstance().hexToInt(yearValue);
        
        System.out.println("Starting year read: " + previousValue);
        if(!this.checkDateInterval(previousValue)) {
            throw new Exception("Sorry, there is a problem with the starting year in you .exe. \nIt seems that the year interval doesn't match.");
        }
        
        return previousValue;
    }
    
    /**
     * Check the date interval
     * @param year 
     */
    private boolean checkDateInterval(int year) {
        boolean status = true;
        if(year < 1792 || year > 2047) {
            status = false;
        }
        
        return status;
    }
    
    /**
     * Set the new starting date
     * @param date 
     */
    private void setStartingDate(int date) throws IOException {
        
        // Setting the starting year and the year after
        String newYearHexValue = BinaryFileHelper.getInstance().integerToHex(date);
        String nextYearAfterHexValue = BinaryFileHelper.getInstance().integerToHex(date + 1);
        System.out.println("Setting new date: " +  newYearHexValue);
        System.out.println("Setting new date + 1: " +  nextYearAfterHexValue);
        
        newYearHexValue = newYearHexValue.substring(newYearHexValue.length() - 2, newYearHexValue.length());
        System.out.println("Last two char of the new date in hex: " +  newYearHexValue);
        nextYearAfterHexValue = nextYearAfterHexValue.substring(nextYearAfterHexValue.length() - 2, nextYearAfterHexValue.length());
        System.out.println("Last two char of the new date +1 in hex: " +  nextYearAfterHexValue);
        
        int newYearValueToWrite = BinaryFileHelper.getInstance().hexToInt(newYearHexValue);
        System.out.println("Int value of the data to write " +  newYearValueToWrite);
        int newYearAfterValueToWrite = BinaryFileHelper.getInstance().hexToInt(nextYearAfterHexValue);
        System.out.println("Int value of the data to write " +  newYearAfterValueToWrite);
        
        for (Integer location : Locations.getStartingYearPositions()) {
            BinaryFileHelper.getInstance().goToByte(location);
            BinaryFileHelper.getInstance().writeIntValue(newYearValueToWrite);
        }
        
        for (Integer location : Locations.getStartingYearAfterPositions()) {
            BinaryFileHelper.getInstance().goToByte(location);
            BinaryFileHelper.getInstance().writeIntValue(newYearAfterValueToWrite);
        }
        
        // Setting date of birth reference
        int difference = date - 1998;
        int newReferenceYear = 1900 + difference;
        String hexDifference = BinaryFileHelper.getInstance().integerToHex(newReferenceYear);
        
        hexDifference = hexDifference.substring(hexDifference.length() - 2, hexDifference.length());
        int newAgeYearReference = BinaryFileHelper.getInstance().hexToInt(hexDifference);
        System.out.println("Difference hex value: " + hexDifference);
        
        for (Integer location : Locations.getAgeYearReferencePositions()) {
            BinaryFileHelper.getInstance().goToByte(location);
            BinaryFileHelper.getInstance().writeIntValue(newAgeYearReference);
        }
    }
    
    /**
     * Enable cheat menu
     * @throws IOException 
     */
    private void enableCheatMenu() throws IOException {
        BinaryFileHelper.getInstance().goToByte(Locations.CHEAT_MENU);
        BinaryFileHelper.getInstance().writeIntValue(Values.CHEAT_MENU_TARGET_VALUE);
    }
    
    /**
     * Disable cheat menu
     * @throws IOException 
     */
    private void disableCheatMenu() throws IOException {
        BinaryFileHelper.getInstance().goToByte(Locations.CHEAT_MENU);
        BinaryFileHelper.getInstance().writeIntValue(Values.CHEAT_MENU_ORIGIN_VALUE);
    }
    
    /**
     * Check if the transfert mode
     * @throws java.io.IOException
     */
    private void checkIfTransfertStandardMode() throws IOException {
        int currentIndex = 0;
        
        for (Integer spot : Locations.getTransfertModePositions()) {
            
            byte[] readVal = BinaryFileHelper.getInstance().getFilePart(spot, 1);
            int originVal = Values.getTransfertOriginValues().get(currentIndex);
     
            System.out.println("Index " + currentIndex + " : transfert origin val read: " + readVal[0] +  ", Expected: " + originVal + ".");

            if(readVal[0] == originVal) {
                System.out.println("Transfert values match");
                this.transfertStandardMode = true;
            } else {
                this.transfertStandardMode = false;
                return;
            }
            
            currentIndex++;
        }
    }
    
    /**
     * Update the status of the transfert mode button
     */
    private void updateTransfertButtonLabel() {
        if(this.transfertStandardMode) {
            this.transfertModeButton.setText("Disable transferts");
        } else {
            this.transfertModeButton.setText("Enable transferts");
        }
    }
    
    /**
     * Enable long term loans
     * @throws IOException 
     */
    private void disableTransferts() throws IOException {
        int currentIndex = 0;
        
        for (Integer spot : Locations.getTransfertModePositions()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(Values.getTransfertTargetValues().get(currentIndex));
            currentIndex++;
        }
    }
    
    /**
     * Disable long term loans
     * @throws IOException 
     */
    private void enableTransferts() throws IOException {
        int currentIndex = 0;
        
        for (Integer spot : Locations.getTransfertModePositions()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(Values.getTransfertOriginValues().get(currentIndex));
            currentIndex++;
        }
    }
}