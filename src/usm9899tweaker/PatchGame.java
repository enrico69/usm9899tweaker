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
    final private JButton firingModeButton = new JButton("");
    final private JTextField maxPromRelegFrQtyValue = new JTextField();
    final private JButton savemaxPromRelegFrQtyButton = new JButton("Save");
    final private JTextField maxPromRelegItQtyValue = new JTextField();
    final private JButton savemaxPromRelegItQtyButton = new JButton("Save");
    final private JButton renameCLButton = new JButton("");
    final private JButton renameELButton = new JButton("");
    final private JButton renameLeagueButton = new JButton("");
    final private JButton renameVariousButton = new JButton("");
    final private JButton looserButton = new JButton("");

    /* Variables use for processing */
    private boolean firstTime = false;
    private String filename = "";
    private String dir = "";

    private boolean areLongTermLoansEnabled = false;
    private boolean isCheatMenuEnabled = false;
    private boolean transfertStandardMode = true;
    private boolean firingStandardMode = true;

    /**
     * Constructor
     *
     * @param mainMenu
     */
    public PatchGame(JFrame mainMenu) {
        this.parent = mainMenu;
    }

    /**
     * Log stuff
     *
     * @param message
     */
    private void log(String message) {
        System.out.println(message);
    }

    /**
     * Launch the patch window
     *
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

                if (lFirstQtyLoanVal[0] != lSecondQtyLoanVal[0]) {
                    throw new Exception("Sorry, but it seems that your file is invalid as there is a difference between the max loan quantity configuration");
                }

                // Getting the fr qty of prom/releg team
                // This we don't make any control of the returned value
                // because at the end the user can overwrite it easily
                // So no corruption control
                int promRelegFr = this.getPromRelelegFr();
                int promRelegIt = this.getPromRelelegIt();

                // Getting the starting year
                int startingYear = this.getStartingYear();

                // Checking if cheat menu is enabled
                this.checkIfCheatMenuIsEnabled();

                // Check transfert mode
                this.checkIfTransfertStandardMode();

                // Check firing mode
                this.checkIfFiringStandardMode();
                
                // Check looser manager
                this.updateLooserButton();

                // Opening the patching panel
                this.openPanel(lFirstQtyLoanVal[0], startingYear, promRelegFr, promRelegIt);
            }
        } else {
            validFile = false;
        }

        if (!validFile) {
            this.parent.setEnabled(true);
            this.parent.requestFocus();
        }
    }

    /**
     * Compare the checksum of the selected file with the USM98-99 ENG version
     *
     * @param filename
     * @return boolean
     */
    private boolean testFile(String filename) {
        boolean result = false;
        String errorMsg = "Sorry: impossible to check the checksum";

        try {
            BinaryFileHelper.getInstance().setFilePath(filename);
            if (BinaryFileHelper.getInstance().getFileCheckSum().contentEquals(Locations.REFERENCE_HASH)) {
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
     *
     * @param maxLoanQty is the max qty of players you can loan
     * @param startingYear is the starting year
     */
    private void openPanel(int maxLoanQty, int startingYear, int promRelegFr, int promRelegIt) {

        // Screen definition
        this.patchScreen = new JFrame();
        this.patchScreen.setTitle("Patch game");
        this.patchScreen.setSize(800, 350);
        this.patchScreen.setLocation(150, 150);
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
        // No Firing
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        JLabel firingMenuLabel = new JLabel("Enable / Disable the fact that you can be fired (see doc.)");
        windowContent.add(firingMenuLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 5;

        this.updateFiringButtonLabel();
        this.firingModeButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.firingModeButton, c);
        this.firingModeButton.addActionListener((ActionListener) this);

        //--------------------------------
        // Promoted / Relegated FR qty section
        //--------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel promRelegQtyFrLabel = new JLabel("Quantity of promoted and relegated teams in the french league");
        c.gridx = 0;
        c.gridy = 6;
        windowContent.add(promRelegQtyFrLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        this.maxPromRelegFrQtyValue.setText(String.valueOf(promRelegFr));
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 6;
        windowContent.add(this.maxPromRelegFrQtyValue, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 6;

        this.savemaxPromRelegFrQtyButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.savemaxPromRelegFrQtyButton, c);
        this.savemaxPromRelegFrQtyButton.addActionListener((ActionListener) this);

        //--------------------------------
        // Promoted / Relegated IT qty section
        //--------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel promRelegQtyItLabel = new JLabel("Quantity of promoted and relegated teams in the italian league");
        c.gridx = 0;
        c.gridy = 7;
        windowContent.add(promRelegQtyItLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        this.maxPromRelegItQtyValue.setText(String.valueOf(promRelegIt));
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 7;
        windowContent.add(this.maxPromRelegItQtyValue, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 7;

        this.savemaxPromRelegItQtyButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.savemaxPromRelegItQtyButton, c);
        this.savemaxPromRelegItQtyButton.addActionListener((ActionListener) this);

        //-----------------------------------------
        // Rename Champions L
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 8;
        JLabel renameChampionsLabel = new JLabel("Rename Champions League");
        windowContent.add(renameChampionsLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 8;

        this.renameCLButton.setText("Apply / Re-apply");
        this.renameCLButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.renameCLButton, c);
        this.renameCLButton.addActionListener((ActionListener) this);

        //-----------------------------------------
        // Rename Europa L
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 9;
        JLabel renameEuropaLabel = new JLabel("Rename Europa League");
        windowContent.add(renameEuropaLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 9;

        this.renameELButton.setText("Apply / Re-apply");
        this.renameELButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.renameELButton, c);
        this.renameELButton.addActionListener((ActionListener) this);

        //-----------------------------------------
        // Rename Leagues
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 10;
        JLabel renameLeagueLabel = new JLabel("Rename Leagues (see doc.)");
        windowContent.add(renameLeagueLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 10;

        this.renameLeagueButton.setText("Apply / Re-apply");
        this.renameLeagueButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.renameLeagueButton, c);
        this.renameLeagueButton.addActionListener((ActionListener) this);
        
        //-----------------------------------------
        // Rename various strings
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 11;
        JLabel renameVariousLabel = new JLabel("Rename various strings (see doc.)");
        windowContent.add(renameVariousLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 11;

        this.renameVariousButton.setText("Apply / Re-apply");
        this.renameVariousButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.renameVariousButton, c);
        this.renameVariousButton.addActionListener((ActionListener) this);

        //-----------------------------------------
        // Looser Manager
        //-----------------------------------------
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 12;
        JLabel looserLabel = new JLabel("Everybody wants a looser (see doc.)");
        windowContent.add(looserLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 12;

        this.looserButton.setPreferredSize(new Dimension(120, 20));
        windowContent.add(this.looserButton, c);
        this.looserButton.addActionListener((ActionListener) this);

        //-----------------------------------------
        // Display
        //-----------------------------------------
        this.patchScreen.setVisible(true);
        this.patchScreen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                try {
                    BinaryFileHelper.getInstance().closeFile();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(parent, "Sorry, impossible to release the file. Please close this application before launching the game", "Error", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                parent.setEnabled(true);
                parent.requestFocus();
            }
        });

    }

    /**
     * Activate / Desactivate buttons and others forms elements
     *
     * @param status
     */
    private void changeFormStatus(boolean status) {
        this.patchLoanButton.setEnabled(status);
        this.saveLoanQtyButton.setEnabled(status);
        this.maxLoanQtyValue.setEnabled(status);
    }

    /**
     * Handle listeners on buttons
     *
     * @param ev
     */
    @Override
    public void actionPerformed(ActionEvent ev) {

        // Handle of the modification of the quantity of loaned players
        if (ev.getSource() == this.saveLoanQtyButton) {
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
        if (ev.getSource() == this.patchLoanButton) {
            this.changeFormStatus(false);
            try {
                if (this.areLongTermLoansEnabled) {
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
        if (ev.getSource() == this.saveStartingYearButton) {
            this.changeFormStatus(false);
            int date = Integer.parseInt(this.startingYearValue.getText());
            if (this.checkDateInterval(date)) {
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

        // Handle the french qty of team promoted and relegated
        if (ev.getSource() == this.savemaxPromRelegFrQtyButton) {
            this.changeFormStatus(false);

            try {
                this.setFrPromRelegated(Integer.parseInt(this.maxPromRelegFrQtyValue.getText()));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.changeFormStatus(true);
        }

        // Handle the italian qty of team promoted and relegated
        if (ev.getSource() == this.savemaxPromRelegItQtyButton) {
            this.changeFormStatus(false);

            try {
                this.setItPromRelegated(Integer.parseInt(this.maxPromRelegItQtyValue.getText()));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.changeFormStatus(true);
        }

        // Handle of the activation of the cheat menu
        if (ev.getSource() == this.cheatMenuButton) {
            this.changeFormStatus(false);
            try {
                if (this.isCheatMenuEnabled) {
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
        if (ev.getSource() == this.transfertModeButton) {
            this.changeFormStatus(false);
            try {
                if (this.transfertStandardMode) {
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

        // Handle of the firing mode
        if (ev.getSource() == this.firingModeButton) {
            this.changeFormStatus(false);
            try {
                System.out.println("Switching firing mode...");
                this.switchFiringMode();
                this.checkIfFiringStandardMode();
                this.updateFiringButtonLabel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }

        // Handle of the activation of the Champions L
        if (ev.getSource() == this.renameCLButton) {
            this.changeFormStatus(false);
            try {
                this.renameChampionsLeague();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }

        // Handle of the activation of the Europa L
        if (ev.getSource() == this.renameELButton) {
            this.changeFormStatus(false);
            try {
                this.renameEuropaLeague();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }

        // Handle of the renaming of the french league
        if (ev.getSource() == this.renameLeagueButton) {
            this.changeFormStatus(false);
            try {
                this.renameLigue1();
                this.renameLigue2();
                this.renameLiga1();
                this.renameLiga2();
                this.renameFrEsShortNames();
                this.renameEndOfSeason();
                this.renameEnglishScotish();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }

        // Rename various string
        if (ev.getSource() == this.renameVariousButton) {
            this.changeFormStatus(false);
            try {
                this.renameVarious();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }
        
        // Handle of the looser manager
        if (ev.getSource() == this.looserButton) {
            this.changeFormStatus(false);
            try {
                System.out.println("Switching looser mode...");
                this.switchLooserMode();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.patchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.changeFormStatus(true);
        }
    }

    /**
     * Update the maximum quantity of player you can loan
     *
     * @param qty
     * @throws IOException
     */
    private void updateLoanQty(int qty) throws IOException {
        if (qty > 9 || qty < 0) {
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
     *
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
     *
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
     *
     * @throws java.io.IOException
     */
    private void checkIfLongTermLoansAreEnabled() throws IOException {
        boolean areEnabled = true;

        byte[] lFirstQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_FIRST, 1);
        if (lFirstQtyLoanVal[0] != Values.ENABLE_LOAN_FIRST_TARGET_VALUE) {
            areEnabled = false;
        }
        System.out.println("--> " + lFirstQtyLoanVal[0] + " --> " + areEnabled);

        byte[] lSecondQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_SECOND, 1);
        if (lSecondQtyLoanVal[0] != Values.ENABLE_LOAN_SECOND_TARGET_VALUE) {
            areEnabled = false;
        }
        System.out.println("--> " + lSecondQtyLoanVal[0] + " --> " + areEnabled);

        byte[] lThirdQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_THIRD, 1);
        if (lThirdQtyLoanVal[0] != Values.ENABLE_LOAN_THIRD_TARGET_VALUE) {
            areEnabled = false;
        }
        System.out.println("--> " + lThirdQtyLoanVal[0] + " --> " + areEnabled);

        byte[] lFourthQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_FOURTH, 1);
        if (lFourthQtyLoanVal[0] != Values.ENABLE_LOAN_FOURTH_TARGET_VALUE) {
            areEnabled = false;
        }
        System.out.println("--> " + lFourthQtyLoanVal[0] + " --> " + areEnabled);

        byte[] lFifthQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_FIFTH, 1);
        if (lFifthQtyLoanVal[0] != Values.ENABLE_LOAN_FIFTH_TARGET_VALUE) {
            areEnabled = false;
        }
        System.out.println("--> " + lFifthQtyLoanVal[0] + " --> " + areEnabled);

        byte[] lSixthQtyLoanVal = BinaryFileHelper.getInstance().getFilePart(Locations.ENABLE_LOAN_SIXTH, 1);
        if (lSixthQtyLoanVal[0] != Values.ENABLE_LOAN_SIXTH_TARGET_VALUE) {
            areEnabled = false;
        }
        System.out.println("--> " + lSixthQtyLoanVal[0] + " --> " + areEnabled);

        this.areLongTermLoansEnabled = areEnabled;
    }

    /**
     * Check the status of the cheat menu
     *
     * @throws IOException
     */
    private void checkIfCheatMenuIsEnabled() throws IOException {
        this.isCheatMenuEnabled = false;
        byte[] cheatVal = BinaryFileHelper.getInstance().getFilePart(Locations.CHEAT_MENU, 1);

        System.out.println("Cheatval read: " + cheatVal[0] + ", Expected: " + Values.CHEAT_MENU_TARGET_VALUE + ".");

        if (cheatVal[0] == Values.CHEAT_MENU_TARGET_VALUE) {
            System.out.println("Cheat values match");
            this.isCheatMenuEnabled = true;
        }
    }

    /**
     * Update the status of the cheat menu status button
     */
    private void updateCheatButtonLabel() {
        if (this.isCheatMenuEnabled) {
            this.cheatMenuButton.setText("Disable");
        } else {
            this.cheatMenuButton.setText("Enable");
        }
    }

    /**
     * Update the status of the long-term loans status button
     */
    private void updateLongTermButtonLabel() {
        if (this.areLongTermLoansEnabled) {
            this.patchLoanButton.setText("Disable");
        } else {
            this.patchLoanButton.setText("Enable");
        }
    }

    /**
     * Get the starting year
     *
     * @return
     */
    private int getStartingYear() throws Exception {
        List<Integer> startYearLocations = Locations.getStartingYearPositions();
        int previousValue = -1;

        for (Integer location : startYearLocations) {
            BinaryFileHelper.getInstance().goToByte(location);
            byte[] readData = BinaryFileHelper.getInstance().getFilePart(location, 1);

            if (previousValue != -1) {
                if (readData[0] != previousValue) {
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
        if (!this.checkDateInterval(previousValue)) {
            throw new Exception("Sorry, there is a problem with the starting year in you .exe. \nIt seems that the year interval doesn't match.");
        }

        return previousValue;
    }

    /**
     * Check the date interval
     *
     * @param year
     */
    private boolean checkDateInterval(int year) {
        boolean status = true;
        if (year < 1792 || year > 2047) {
            status = false;
        }

        return status;
    }

    /**
     * Set the new starting date
     *
     * @param date
     */
    private void setStartingDate(int date) throws IOException {

        // Setting the starting year and the year after
        String newYearHexValue = BinaryFileHelper.getInstance().integerToHex(date);
        String nextYearAfterHexValue = BinaryFileHelper.getInstance().integerToHex(date + 1);
        System.out.println("Setting new date: " + newYearHexValue);
        System.out.println("Setting new date + 1: " + nextYearAfterHexValue);

        newYearHexValue = newYearHexValue.substring(newYearHexValue.length() - 2, newYearHexValue.length());
        System.out.println("Last two char of the new date in hex: " + newYearHexValue);
        nextYearAfterHexValue = nextYearAfterHexValue.substring(nextYearAfterHexValue.length() - 2, nextYearAfterHexValue.length());
        System.out.println("Last two char of the new date +1 in hex: " + nextYearAfterHexValue);

        int newYearValueToWrite = BinaryFileHelper.getInstance().hexToInt(newYearHexValue);
        System.out.println("Int value of the data to write " + newYearValueToWrite);
        int newYearAfterValueToWrite = BinaryFileHelper.getInstance().hexToInt(nextYearAfterHexValue);
        System.out.println("Int value of the data to write " + newYearAfterValueToWrite);

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
     *
     * @throws IOException
     */
    private void enableCheatMenu() throws IOException {
        BinaryFileHelper.getInstance().goToByte(Locations.CHEAT_MENU);
        BinaryFileHelper.getInstance().writeIntValue(Values.CHEAT_MENU_TARGET_VALUE);
    }

    private void switchFiringMode() throws IOException {
        long target = Locations.NO_FIRING;
        int value = Values.NO_FIRING_TARGET_VALUE;

        if (!this.firingStandardMode) {
            value = Values.NO_FIRING_ORIGIN_VALUE;
        }

        System.out.println("Firing value: " + value);

        BinaryFileHelper.getInstance().goToByte(target);
        BinaryFileHelper.getInstance().writeIntValue(value);
    }

    /**
     * Disable cheat menu
     *
     * @throws IOException
     */
    private void disableCheatMenu() throws IOException {
        BinaryFileHelper.getInstance().goToByte(Locations.CHEAT_MENU);
        BinaryFileHelper.getInstance().writeIntValue(Values.CHEAT_MENU_ORIGIN_VALUE);
    }

    /**
     * Check if the transfert mode
     *
     * @throws java.io.IOException
     */
    private void checkIfTransfertStandardMode() throws IOException {
        int currentIndex = 0;

        for (Integer spot : Locations.getTransfertModePositions()) {

            byte[] readVal = BinaryFileHelper.getInstance().getFilePart(spot, 1);
            int originVal = Values.getTransfertOriginValues().get(currentIndex);

            System.out.println("Index " + currentIndex + " : transfert origin val read: " + readVal[0] + ", Expected: " + originVal + ".");

            if (readVal[0] == originVal) {
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
     * Check if the looser mode is enabled
     * 
     * @return
     * @throws IOException 
     */
    private boolean checkLooserMode() throws IOException
    {
        byte[] readVal = BinaryFileHelper.getInstance().getFilePart(667421, 1);
        System.out.println("Looser mode: " + readVal[0] );
        if (readVal[0] == -116) { // @TODO also check the second part (see below)
            return true;
        }
        return false;
    }
    
    /**
     * Swtich the looser mode
     * @throws IOException 
     */
    private void switchLooserMode() throws IOException
    {
        if (this.checkLooserMode()) {
            BinaryFileHelper.getInstance().goToByte(667421);
            BinaryFileHelper.getInstance().writeIntValue(142);
        
            BinaryFileHelper.getInstance().goToByte(667436);
            BinaryFileHelper.getInstance().writeIntValue(59);
            BinaryFileHelper.getInstance().writeIntValue(198);
            BinaryFileHelper.getInstance().writeIntValue(15);
            BinaryFileHelper.getInstance().writeIntValue(143);
            BinaryFileHelper.getInstance().writeIntValue(224);
        } else {
            BinaryFileHelper.getInstance().goToByte(667421);
            BinaryFileHelper.getInstance().writeIntValue(140);
        
            BinaryFileHelper.getInstance().goToByte(667436);
            BinaryFileHelper.getInstance().writeIntValue(133);
            BinaryFileHelper.getInstance().writeIntValue(246);
            BinaryFileHelper.getInstance().writeIntValue(15);
            BinaryFileHelper.getInstance().writeIntValue(142);
            BinaryFileHelper.getInstance().writeIntValue(242);
        }
        
        
        updateLooserButton();
    }
    
    /**
     * Set the looser button label
     * 
     * @throws IOException 
     */
    private void updateLooserButton() throws IOException
    {
        if(this.checkLooserMode()) {
            this.looserButton.setText("Disable");
        }  else {
            this.looserButton.setText("Enable");
        }
    }

    /**
     * Check if the firing mode
     *
     * @throws java.io.IOException
     */
    private void checkIfFiringStandardMode() throws IOException {
        byte[] readVal = BinaryFileHelper.getInstance().getFilePart(Locations.NO_FIRING, 1);
        System.out.println("No firing read value: " + readVal[0]);

        if (readVal[0] == Values.NO_FIRING_ORIGIN_VALUE) {
            System.out.println("firing values match");
            this.firingStandardMode = true;
        } else {
            this.firingStandardMode = false;
        }
    }

    /**
     * Update the status of the transfert mode button
     */
    private void updateTransfertButtonLabel() {
        if (this.transfertStandardMode) {
            this.transfertModeButton.setText("Disable transferts");
        } else {
            this.transfertModeButton.setText("Enable transferts");
        }
    }

    /**
     * Update the status of the firing mode button
     */
    private void updateFiringButtonLabel() {
        if (this.firingStandardMode) {
            this.firingModeButton.setText("Disable");
        } else {
            this.firingModeButton.setText("Enable");
        }
    }

    /**
     * Enable long term loans
     *
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
     * Return the first offset value for the qty of french promoted/regated
     * teams
     *
     * @return int
     */
    private int getPromRelelegFr() throws IOException {
        byte[] readData = BinaryFileHelper.getInstance().getFilePart(Locations.QTY_PROM_RELEG_FR_1, 1);

        return readData[0];
    }

    /**
     * Return the first offset value for the qty of french promoted/regated
     * teams
     *
     * @return int
     */
    private int getPromRelelegIt() throws IOException {
        byte[] readData = BinaryFileHelper.getInstance().getFilePart(Locations.QTY_PROM_RELEG_IT_1, 1);

        return readData[0];
    }

    /**
     * Change the value of qty of french and relegated teams
     *
     * @param int value
     * @throws IOException
     */
    private void setFrPromRelegated(int value) throws IOException {
        value = value > 9 ? 9 : value;
        value = value < 0 ? 0 : value;

        BinaryFileHelper.getInstance().goToByte(Locations.QTY_PROM_RELEG_FR_1);
        BinaryFileHelper.getInstance().writeIntValue(value);
        BinaryFileHelper.getInstance().goToByte(Locations.QTY_PROM_RELEG_FR_2);
        BinaryFileHelper.getInstance().writeIntValue(value);
    }

    /**
     * Change the value of qty of italian and relegated teams
     *
     * @param int value
     * @throws IOException
     */
    private void setItPromRelegated(int value) throws IOException {
        value = value > 9 ? 9 : value;
        value = value < 0 ? 0 : value;

        BinaryFileHelper.getInstance().goToByte(Locations.QTY_PROM_RELEG_IT_1);
        BinaryFileHelper.getInstance().writeIntValue(value);
        BinaryFileHelper.getInstance().goToByte(Locations.QTY_PROM_RELEG_IT_2);
        BinaryFileHelper.getInstance().writeIntValue(value);
    }

    /**
     * Disable long term loans
     *
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

    /**
     * Rename the European Cup
     *
     * @throws IOException
     */
    private void renameChampionsLeague() throws IOException {

        for (Integer spot : Locations.getChampionsLeaguePositions()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Champion's L");
        }

        BinaryFileHelper.getInstance().goToByte(Locations.CL_SHORTNAME_SPOT);
        BinaryFileHelper.getInstance().writeHex("CL");

    }

    /**
     * Rename the UEFA Cup
     *
     * @throws IOException
     */
    private void renameEuropaLeague() throws IOException {

        for (Integer spot : Locations.getEuropaLeaguePositions()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Europa L");
        }

        BinaryFileHelper.getInstance().goToByte(Locations.EL_SHORTNAME_SPOT);
        BinaryFileHelper.getInstance().writeHex(" EL ");
    }

    /**
     * Rename the French Division 1 to Ligue 1
     *
     * @throws IOException
     */
    private void renameLigue1() throws IOException {

        for (Integer spot : Locations.getSerieC1ALocations()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Ligue 1  ");
        }

        // Ligues summary
        BinaryFileHelper.getInstance().goToByte(358278);
        BinaryFileHelper.getInstance().writeIntValue(100);
        BinaryFileHelper.getInstance().goToByte(358279);
        BinaryFileHelper.getInstance().writeIntValue(133);
        
        // Ligues summary
        int[] factorial = {358278, 361280, 364714, 367686, 368070, 368454,
        368838, 369222, 369606, 370698, 371098, 371498, 1142376, 1142384, 1256056};

        for (Integer spot : factorial) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(100);
            spot++;
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(133);
        }

        // Current form
        BinaryFileHelper.getInstance().goToByte(1169336);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1169337);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169338);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top scorer
        BinaryFileHelper.getInstance().goToByte(1169352);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1169353);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169354);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Tables
        BinaryFileHelper.getInstance().goToByte(1169184);
        BinaryFileHelper.getInstance().writeIntValue(88);
        BinaryFileHelper.getInstance().goToByte(1169185);
        BinaryFileHelper.getInstance().writeIntValue(184);
        BinaryFileHelper.getInstance().goToByte(1169186);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Tables Home 
        BinaryFileHelper.getInstance().goToByte(1169188);
        BinaryFileHelper.getInstance().writeIntValue(64);
        BinaryFileHelper.getInstance().goToByte(1169189);
        BinaryFileHelper.getInstance().writeIntValue(184);
        BinaryFileHelper.getInstance().goToByte(1169190);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Tables Away 
        BinaryFileHelper.getInstance().goToByte(1169192);
        BinaryFileHelper.getInstance().writeIntValue(40);
        BinaryFileHelper.getInstance().goToByte(1169193);
        BinaryFileHelper.getInstance().writeIntValue(184);
        BinaryFileHelper.getInstance().goToByte(1169194);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Fixtures
        BinaryFileHelper.getInstance().goToByte(1169272);
        BinaryFileHelper.getInstance().writeIntValue(168);
        BinaryFileHelper.getInstance().goToByte(1169273);
        BinaryFileHelper.getInstance().writeIntValue(183);
        BinaryFileHelper.getInstance().goToByte(1169274);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Results
        BinaryFileHelper.getInstance().goToByte(1169320);
        BinaryFileHelper.getInstance().writeIntValue(72);
        BinaryFileHelper.getInstance().goToByte(1169321);
        BinaryFileHelper.getInstance().writeIntValue(183);
        BinaryFileHelper.getInstance().goToByte(1169322);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top scorers 
        BinaryFileHelper.getInstance().goToByte(1169352);
        BinaryFileHelper.getInstance().writeIntValue(136);
        BinaryFileHelper.getInstance().goToByte(1169353);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169354);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top players
        BinaryFileHelper.getInstance().goToByte(1169368);
        BinaryFileHelper.getInstance().writeIntValue(32);
        BinaryFileHelper.getInstance().goToByte(1169369);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169370);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top managers of the month 
        BinaryFileHelper.getInstance().goToByte(1169384);
        BinaryFileHelper.getInstance().writeIntValue(148);
        BinaryFileHelper.getInstance().goToByte(1169385);
        BinaryFileHelper.getInstance().writeIntValue(181);
        BinaryFileHelper.getInstance().goToByte(1169386);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top managers of the year
        BinaryFileHelper.getInstance().goToByte(1169400);
        BinaryFileHelper.getInstance().writeIntValue(252);
        BinaryFileHelper.getInstance().goToByte(1169401);
        BinaryFileHelper.getInstance().writeIntValue(180);
        BinaryFileHelper.getInstance().goToByte(1169402);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Most assist 
        BinaryFileHelper.getInstance().goToByte(1169416);
        BinaryFileHelper.getInstance().writeIntValue(136);
        BinaryFileHelper.getInstance().goToByte(1169417);
        BinaryFileHelper.getInstance().writeIntValue(180);
        BinaryFileHelper.getInstance().goToByte(1169418);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Man of the match
        BinaryFileHelper.getInstance().goToByte(1169432);
        BinaryFileHelper.getInstance().writeIntValue(240);
        BinaryFileHelper.getInstance().goToByte(1169433);
        BinaryFileHelper.getInstance().writeIntValue(179);
        BinaryFileHelper.getInstance().goToByte(1169434);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Worst discipline
        BinaryFileHelper.getInstance().goToByte(1169448);
        BinaryFileHelper.getInstance().writeIntValue(108);
        BinaryFileHelper.getInstance().goToByte(1169449);
        BinaryFileHelper.getInstance().writeIntValue(179);
        BinaryFileHelper.getInstance().goToByte(1169450);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Average Attendance
        BinaryFileHelper.getInstance().goToByte(1169464);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1169465);
        BinaryFileHelper.getInstance().writeIntValue(178);
        BinaryFileHelper.getInstance().goToByte(1169466);
        BinaryFileHelper.getInstance().writeIntValue(83);
        
         // Short name
        BinaryFileHelper.getInstance().goToByte(1273704);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1273705);
        BinaryFileHelper.getInstance().writeIntValue(184);
        
        BinaryFileHelper.getInstance().goToByte(1345488);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1345489);
        BinaryFileHelper.getInstance().writeIntValue(184);
        
        // History
        BinaryFileHelper.getInstance().goToByte(1273352);
        BinaryFileHelper.getInstance().writeIntValue(208);
        BinaryFileHelper.getInstance().goToByte(1273353);
        BinaryFileHelper.getInstance().writeIntValue(141);
    }
    
    /**
     * Rename the string from the end of the season
     */
    private void renameEndOfSeason() throws IOException
    {
        int begin = 1283884;
         BinaryFileHelper.getInstance().goToByte(begin);
         int end = begin + 12;
         BinaryFileHelper.getInstance().writeHex("Rel. from L1");
         for (int i = end; i<=end +3; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
         
         int[] factorial = {1242642, 1242656, 1242564, 1242536, 1105784};

        for (Integer spot : factorial) {
            begin = spot;
            BinaryFileHelper.getInstance().goToByte(begin);
            end = begin + 2;
            BinaryFileHelper.getInstance().writeHex("L2");
            for (int i = end; i<=end +9; i++) {
               BinaryFileHelper.getInstance().writeHex(" ");
           }
        }
        
        BinaryFileHelper.getInstance().goToByte(659522);
        BinaryFileHelper.getInstance().writeIntValue(185);

        BinaryFileHelper.getInstance().goToByte(648539);
        BinaryFileHelper.getInstance().writeIntValue(32);
        BinaryFileHelper.getInstance().writeIntValue(24);

        BinaryFileHelper.getInstance().goToByte(648666);
        BinaryFileHelper.getInstance().writeIntValue(04);
        BinaryFileHelper.getInstance().writeIntValue(24);

        BinaryFileHelper.getInstance().goToByte(649070);
        BinaryFileHelper.getInstance().writeIntValue(196);
        BinaryFileHelper.getInstance().writeIntValue(23);

        BinaryFileHelper.getInstance().goToByte(649127);
        BinaryFileHelper.getInstance().writeIntValue(168);
        BinaryFileHelper.getInstance().writeIntValue(23);

        BinaryFileHelper.getInstance().goToByte(649466);
        BinaryFileHelper.getInstance().writeIntValue(44);
        BinaryFileHelper.getInstance().writeIntValue(185);

        BinaryFileHelper.getInstance().goToByte(649327);
        BinaryFileHelper.getInstance().writeIntValue(120);
        BinaryFileHelper.getInstance().writeIntValue(01);
        BinaryFileHelper.getInstance().writeIntValue(81);
        
 
         // So those two strings are available
//         begin = 1283900;
//         BinaryFileHelper.getInstance().goToByte(begin);
//         end = begin + 12;
//         BinaryFileHelper.getInstance().writeHex("L2 Champions");
//         for (int i = end; i<=end +16; i++) {
//            BinaryFileHelper.getInstance().writeIntValue(0);
//        }
//         
//         begin = 1283948;
//         BinaryFileHelper.getInstance().goToByte(begin);
//         end = begin + 13;
//         BinaryFileHelper.getInstance().writeHex("Prom. from L2");
//         for (int i = end; i<=end +30; i++) {
//            BinaryFileHelper.getInstance().writeIntValue(0);
//        }
    }        
    
    /**
     * Rename the short name of the French and Spanish leagues
     */
    private void renameFrEsShortNames() throws IOException
    {
        int L1Start = 1283820;
        BinaryFileHelper.getInstance().goToByte(L1Start);
        BinaryFileHelper.getInstance().writeHex("L1");
        
        int L1End = L1Start + 28; // 26 empty chars
        int L1EndString = L1Start + 2;
        BinaryFileHelper.getInstance().goToByte(L1EndString);
        for (int i = L1EndString; i<=L1End; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        
       
        int L2Start = 1283852;
        BinaryFileHelper.getInstance().goToByte(L2Start);
        BinaryFileHelper.getInstance().writeHex("L2");
        
        int L2End = L2Start + 28; // 26 empty chars
        int L2EndString = L2Start + 2;
        BinaryFileHelper.getInstance().goToByte(L2EndString);
        for (int i = L2EndString; i<=L2End; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        
    }

    /**
     * Rename the French Division 2 to Ligue 2
     *
     * @throws IOException
     */
    private void renameLigue2() throws IOException {

        for (Integer spot : Locations.getSerieC1BLocations()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Ligue 2  ");
        }

        // Ligues summary
        int[] factorial = {358301, 361303, 364737, 367709, 368093, 368477,
        368861, 369245, 369629, 370721, 371121, 371521, 1142380, 1142388, 1256061};

        for (Integer spot : factorial) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(88);
            spot++;
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(133);
        }

        // Current form
        BinaryFileHelper.getInstance().goToByte(1169340);
        BinaryFileHelper.getInstance().writeIntValue(212);
        BinaryFileHelper.getInstance().goToByte(1169341);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169342);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top scorer
        BinaryFileHelper.getInstance().goToByte(1169356);
        BinaryFileHelper.getInstance().writeIntValue(108);
        BinaryFileHelper.getInstance().goToByte(1169357);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169358);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Tables
        BinaryFileHelper.getInstance().goToByte(1169196);
        BinaryFileHelper.getInstance().writeIntValue(20);
        BinaryFileHelper.getInstance().goToByte(1169197);
        BinaryFileHelper.getInstance().writeIntValue(184);
        BinaryFileHelper.getInstance().goToByte(1169198);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Tables Home
        BinaryFileHelper.getInstance().goToByte(1169200);
        BinaryFileHelper.getInstance().writeIntValue(252);
        BinaryFileHelper.getInstance().goToByte(1169201);
        BinaryFileHelper.getInstance().writeIntValue(183);
        BinaryFileHelper.getInstance().goToByte(1169202);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Tables Away
        BinaryFileHelper.getInstance().goToByte(1169204);
        BinaryFileHelper.getInstance().writeIntValue(228);
        BinaryFileHelper.getInstance().goToByte(1169205);
        BinaryFileHelper.getInstance().writeIntValue(183);
        BinaryFileHelper.getInstance().goToByte(1169206);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Fixtures
        BinaryFileHelper.getInstance().goToByte(1169276);
        BinaryFileHelper.getInstance().writeIntValue(148);
        BinaryFileHelper.getInstance().goToByte(1169277);
        BinaryFileHelper.getInstance().writeIntValue(183);
        BinaryFileHelper.getInstance().goToByte(1169278);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Results
        BinaryFileHelper.getInstance().goToByte(1169324);
        BinaryFileHelper.getInstance().writeIntValue(52);
        BinaryFileHelper.getInstance().goToByte(1169325);
        BinaryFileHelper.getInstance().writeIntValue(183);
        BinaryFileHelper.getInstance().goToByte(1169326);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top scorers 
        BinaryFileHelper.getInstance().goToByte(1169356);
        BinaryFileHelper.getInstance().writeIntValue(108);
        BinaryFileHelper.getInstance().goToByte(1169357);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169358);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top players 
        BinaryFileHelper.getInstance().goToByte(1169372);
        BinaryFileHelper.getInstance().writeIntValue(4);
        BinaryFileHelper.getInstance().goToByte(1169373);
        BinaryFileHelper.getInstance().writeIntValue(182);
        BinaryFileHelper.getInstance().goToByte(1169374);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top managers of the month 
        BinaryFileHelper.getInstance().goToByte(1169388);
        BinaryFileHelper.getInstance().writeIntValue(108);
        BinaryFileHelper.getInstance().goToByte(1169389);
        BinaryFileHelper.getInstance().writeIntValue(181);
        BinaryFileHelper.getInstance().goToByte(1169390);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Top managers of the year
        BinaryFileHelper.getInstance().goToByte(1169404);
        BinaryFileHelper.getInstance().writeIntValue(212);
        BinaryFileHelper.getInstance().goToByte(1169405);
        BinaryFileHelper.getInstance().writeIntValue(180);
        BinaryFileHelper.getInstance().goToByte(1169406);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Most assist
        BinaryFileHelper.getInstance().goToByte(1169420);
        BinaryFileHelper.getInstance().writeIntValue(108);
        BinaryFileHelper.getInstance().goToByte(1169421);
        BinaryFileHelper.getInstance().writeIntValue(180);
        BinaryFileHelper.getInstance().goToByte(1169422);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Man of the match
        BinaryFileHelper.getInstance().goToByte(1169436);
        BinaryFileHelper.getInstance().writeIntValue(196);
        BinaryFileHelper.getInstance().goToByte(1169437);
        BinaryFileHelper.getInstance().writeIntValue(179);
        BinaryFileHelper.getInstance().goToByte(1169438);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Worst discipline
        BinaryFileHelper.getInstance().goToByte(1169452);
        BinaryFileHelper.getInstance().writeIntValue(76);
        BinaryFileHelper.getInstance().goToByte(1169453);
        BinaryFileHelper.getInstance().writeIntValue(179);
        BinaryFileHelper.getInstance().goToByte(1169454);
        BinaryFileHelper.getInstance().writeIntValue(83);

        // Average Attendance
        BinaryFileHelper.getInstance().goToByte(1169468);
        BinaryFileHelper.getInstance().writeIntValue(204);
        BinaryFileHelper.getInstance().goToByte(1169469);
        BinaryFileHelper.getInstance().writeIntValue(178);
        BinaryFileHelper.getInstance().goToByte(1169470);
        BinaryFileHelper.getInstance().writeIntValue(83);
        
        // Short name
        BinaryFileHelper.getInstance().goToByte(1273708);
        BinaryFileHelper.getInstance().writeIntValue(12);
        BinaryFileHelper.getInstance().goToByte(1273709);
        BinaryFileHelper.getInstance().writeIntValue(185);
        
        BinaryFileHelper.getInstance().goToByte(1345492);
        BinaryFileHelper.getInstance().writeIntValue(12);
        BinaryFileHelper.getInstance().goToByte(1345493);
        BinaryFileHelper.getInstance().writeIntValue(185);
        
        BinaryFileHelper.getInstance().goToByte(1345492);
        BinaryFileHelper.getInstance().writeIntValue(12);
        BinaryFileHelper.getInstance().goToByte(1345493);
        BinaryFileHelper.getInstance().writeIntValue(185);
        
        // History
        BinaryFileHelper.getInstance().goToByte(1273356);
        BinaryFileHelper.getInstance().writeIntValue(188);
        BinaryFileHelper.getInstance().goToByte(1273357);
        BinaryFileHelper.getInstance().writeIntValue(141);
    }

    /**
     * Rename the Spanish Division 1 to La Liga1
     *
     * @throws IOException
     */
    private void renameLiga1() throws IOException {
        String ligaName = "La Liga 1";

        for (Integer spot : Locations.getNationalLocations()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex(ligaName);
        }

        // Use the C1A Final slot to add the missing slot
        BinaryFileHelper.getInstance().goToByte(1283932);
        BinaryFileHelper.getInstance().writeHex(ligaName + "      ");

        // Ligues summary
        int[] factorial = {1110, 4093, 7818, 11430, 11814, 12198, 12582,
            12966, 13350, 13754, 14154, 14554, 1142624, 1142632, 1256160};

        for (Integer spot : factorial) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(92);
            spot++;
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(185);
            spot++;
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeIntValue(83);
        }
        
        // Short name
        BinaryFileHelper.getInstance().goToByte(1283820);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1283820);
        BinaryFileHelper.getInstance().writeIntValue(184);
        
        BinaryFileHelper.getInstance().goToByte(1274776);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1274777);
        BinaryFileHelper.getInstance().writeIntValue(184);
        
        BinaryFileHelper.getInstance().goToByte(1347440);
        BinaryFileHelper.getInstance().writeIntValue(236);
        BinaryFileHelper.getInstance().goToByte(1347441);
        BinaryFileHelper.getInstance().writeIntValue(184);
        
        // History
        BinaryFileHelper.getInstance().goToByte(1274736);
        BinaryFileHelper.getInstance().writeIntValue(240);
        BinaryFileHelper.getInstance().goToByte(1274737);
        BinaryFileHelper.getInstance().writeIntValue(142);
        
        //Results
        BinaryFileHelper.getInstance().goToByte(1106712);
        BinaryFileHelper.getInstance().writeIntValue(96);
        BinaryFileHelper.getInstance().writeIntValue(248);
        
        //Current form
        BinaryFileHelper.getInstance().goToByte(1106720);
        BinaryFileHelper.getInstance().writeIntValue(48);
        BinaryFileHelper.getInstance().writeIntValue(248);
        
        //Top scorer
        BinaryFileHelper.getInstance().goToByte(1106728);
        BinaryFileHelper.getInstance().writeIntValue(248);
        BinaryFileHelper.getInstance().writeIntValue(247);
        
         //Top players
        BinaryFileHelper.getInstance().goToByte(1106736);
        BinaryFileHelper.getInstance().writeIntValue(192);
        BinaryFileHelper.getInstance().writeIntValue(247);
        
        //Manager of the month
        BinaryFileHelper.getInstance().goToByte(1106744);
        BinaryFileHelper.getInstance().writeIntValue(112);
        BinaryFileHelper.getInstance().writeIntValue(247);
        
        //Manager of the year
        BinaryFileHelper.getInstance().goToByte(1106752);
        BinaryFileHelper.getInstance().writeIntValue(32);
        BinaryFileHelper.getInstance().writeIntValue(247);
        
        //Most assists
        BinaryFileHelper.getInstance().goToByte(1106760);
        BinaryFileHelper.getInstance().writeIntValue(232);
        BinaryFileHelper.getInstance().writeIntValue(246);
        
        //Man of the match
        BinaryFileHelper.getInstance().goToByte(1106768);
        BinaryFileHelper.getInstance().writeIntValue(144);
        BinaryFileHelper.getInstance().writeIntValue(246);
        
        //Worst discipline
        BinaryFileHelper.getInstance().goToByte(1106776);
        BinaryFileHelper.getInstance().writeIntValue(80);
        BinaryFileHelper.getInstance().writeIntValue(246);
        
        //Average attendance (Buggy in the original game)
//        BinaryFileHelper.getInstance().goToByte(1106784);
//        BinaryFileHelper.getInstance().writeIntValue(12);
//        BinaryFileHelper.getInstance().writeIntValue(246);

        //Fixtures
        BinaryFileHelper.getInstance().goToByte(1106664);
        BinaryFileHelper.getInstance().writeIntValue(92);
        BinaryFileHelper.getInstance().writeIntValue(3);
        
        // Table
        BinaryFileHelper.getInstance().goToByte(1106576);
        BinaryFileHelper.getInstance().writeIntValue(228);
        BinaryFileHelper.getInstance().writeIntValue(3);
        
        // Tables - Away
        BinaryFileHelper.getInstance().goToByte(1106580);
        BinaryFileHelper.getInstance().writeIntValue(204);
        BinaryFileHelper.getInstance().writeIntValue(3);        
        
        // Table-Home
        BinaryFileHelper.getInstance().goToByte(1106584);
        BinaryFileHelper.getInstance().writeIntValue(180);
        BinaryFileHelper.getInstance().writeIntValue(3);
    }

    /**
     * Rename the Spanish Division 2 to La Liga2
     *
     * @throws IOException
     */
    private void renameLiga2() throws IOException {
        for (Integer spot : Locations.getDiv2ALocations()) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("La Liga 2  ");
        }
        
        // Short name
        BinaryFileHelper.getInstance().goToByte(1274780);
        BinaryFileHelper.getInstance().writeIntValue(12);
        BinaryFileHelper.getInstance().goToByte(1274781);
        BinaryFileHelper.getInstance().writeIntValue(185);
        
        BinaryFileHelper.getInstance().goToByte(1347444);
        BinaryFileHelper.getInstance().writeIntValue(12);
        BinaryFileHelper.getInstance().goToByte(1347445);
        BinaryFileHelper.getInstance().writeIntValue(185);
    }
    
    /**
     * Rename various strings
     */
    private void renameVarious() throws IOException {
        
        // STD
        BinaryFileHelper.getInstance().goToByte(1175920);
        BinaryFileHelper.getInstance().writeHex("STD");
        for (int i = 0; i<=5; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        
        BinaryFileHelper.getInstance().goToByte(1176358);
        BinaryFileHelper.getInstance().writeHex("STD");
        for (int i = 0; i<=5; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        
        // Alan Agent
        BinaryFileHelper.getInstance().goToByte(1368368);
        BinaryFileHelper.getInstance().writeHex("Mino Raiola");
        
        // Sierratext
        int[] factorial = {1112340, 1112364, 1112396, 1141152};
        
         for (Integer spot : factorial) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Smart TV");
             for (int i = 0; i<=2; i++) {
                BinaryFileHelper.getInstance().writeIntValue(0);
            }
         }
         
        BinaryFileHelper.getInstance().goToByte(1255052);
        BinaryFileHelper.getInstance().writeHex("Smart TV (Dutch)");
        for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        BinaryFileHelper.getInstance().goToByte(1255072);
        BinaryFileHelper.getInstance().writeHex("Smart TV (Spain)");
        for (int i = 0; i<=4; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        BinaryFileHelper.getInstance().goToByte(1255096);
        BinaryFileHelper.getInstance().writeHex("Smart TV (Scottish)");
        for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        BinaryFileHelper.getInstance().goToByte(1255120);
        BinaryFileHelper.getInstance().writeHex("Smart TV (Italian)");
        for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        BinaryFileHelper.getInstance().goToByte(1255144);
        BinaryFileHelper.getInstance().writeHex("Smart TV (German)");
        for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        BinaryFileHelper.getInstance().goToByte(1255164);
        BinaryFileHelper.getInstance().writeHex("Smart TV (French)");
        for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        BinaryFileHelper.getInstance().goToByte(1255184);
        BinaryFileHelper.getInstance().writeHex("Smart TV (English)");
        for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
        
         // Email
        BinaryFileHelper.getInstance().goToByte(1141176);
        BinaryFileHelper.getInstance().writeHex("Macbook");
         for (int i = 0; i<=2; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
         
         // Cell phone
        BinaryFileHelper.getInstance().goToByte(1141136);
        BinaryFileHelper.getInstance().writeHex("iPhone");
         for (int i = 0; i<=6; i++) {
            BinaryFileHelper.getInstance().writeIntValue(0);
        }
         
        BinaryFileHelper.getInstance().goToByte(1228256);
        BinaryFileHelper.getInstance().writeHex("iPhone");
        
        // Video highlights
        BinaryFileHelper.getInstance().goToByte(1140632);
        BinaryFileHelper.getInstance().writeHex("Apple TV");
        for (int i = 0; i<=7; i++) {
        BinaryFileHelper.getInstance().writeIntValue(0);
        }
    }
    
    private void renameEnglishScotish() throws IOException {
        
        // Premiership
        // Premier L. +1 space
        int[] factorial = {1139764,1139824,1218994,1219060,1219148,1219204,1219256,1219336,1219419,1219475,1219512,
            1219560,1219640,1219740,1219768,1219796,1219816,1240031,1273288,1288854,1288928,1289024,1289088,1289231,1289295,1302584};
        for (Integer spot : factorial) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Premier L. ");
         }
        
        // Division 1
        // Championship
        int[] factorialDiv1 = {1105562,1105987,1106043,1240951,1242427};
         for (Integer spot : factorialDiv1) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Championship");
         }
         
         int[] factorialDiv1b = {1105628,1105716,1105772,1105824,1105904,1106084,1106128,1106312,1106500,
             1106528,1106556,1110764,1219830,1240172,1240436,1240464};
         for (Integer spot : factorialDiv1b) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Champions.");
         }
         
         int[] factorialDiv1c = { 1240003,1240746,1240760,1273264,1289624,1289768,1289788};
         for (Integer spot : factorialDiv1c) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("Championship");
         }
         BinaryFileHelper.getInstance().goToByte(1272468);
         BinaryFileHelper.getInstance().writeHex("Cham");
                
        // Division 2
        // League One
        int[] factorialDiv2 = { 1139812,1168450,1168516,1168604,1168660,1168712,1168792,1168875,1168931,
            1168968,1169012,1169084,1169104,1169132,1169160,1240140,1240376,1240404,1241070,1241084};
        
        for (Integer spot : factorialDiv2) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("League One");
         }
        
        int[] factorialDiv2b = { 1239975,1240694,1240708,1273240,1289592,1289716,1289736 };
         for (Integer spot : factorialDiv2b) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("League One  ");
         }
         
         BinaryFileHelper.getInstance().goToByte(1272460);
         BinaryFileHelper.getInstance().writeHex("One ");
        
        // Division 3
        // League Two
        int[] factorialDiv3 = {  1139800,1218958,1219028,1219104,1219176,1219216,1219296,1219391,1219447,
            1219488,1219540,1219620,1219664,1219692,1219720,1240108,1240316,1240344};
        
        for (Integer spot : factorialDiv3) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("League Two");
         }
        
        int[] factorialDiv3b = { 1239943,1240634,1240652,1273216,1289556,1289656,1289680 };
         for (Integer spot : factorialDiv3b) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("League Two    ");
         }
         
         BinaryFileHelper.getInstance().goToByte(1272452);
         BinaryFileHelper.getInstance().writeHex("Two ");
        
        // Conference
        // National
         int[] factorialConference = { 1139748,1139788,1240076,1240256,1240284,1240596,1273196,1288814,1288892,1288976,1289056,
        1289100,1289140,1289199,1289263,1289308,1289400,1289436,1289480,1289508,1289536};
        
         for (Integer spot : factorialConference) {
            BinaryFileHelper.getInstance().goToByte(spot);
            BinaryFileHelper.getInstance().writeHex("National L");
         }
         
         BinaryFileHelper.getInstance().goToByte(1272444);
         BinaryFileHelper.getInstance().writeHex("Nat.");
        
    }
}
