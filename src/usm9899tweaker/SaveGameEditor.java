/*
 * Class of the save game editor section
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
import java.util.ArrayList;
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

public class SaveGameEditor  implements ActionListener {
    
    /* Display elements */
    private final JFrame parent;
    private JFrame searchScreen;
    final private JTextField playerLastName = new JTextField("");
    final private JButton searchButton = new JButton("Search");

    /* Variables use for processing */
    private String filename = "";
    private String dir = "";
    
    /**
     * Constructor
     * @param mainMenu 
     */
    public SaveGameEditor(JFrame mainMenu) {
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
        
        // File chooser
        JFileChooser c = new JFileChooser();
        int response = c.showOpenDialog(parent);
        
        if (response == JFileChooser.APPROVE_OPTION) {
            this.filename = c.getSelectedFile().getName();
            this.dir = c.getCurrentDirectory().toString();
            this.filename = this.dir + File.separator + this.filename;
            this.log(this.filename);
            
            BinaryFileHelper.getInstance().setFilePath(this.filename);
            BinaryFileHelper.getInstance().openFile();
            this.openSearchWindow();
        } else {
            this.parent.setEnabled(true);
            this.parent.requestFocus();
        }
    }
    
    public void openSearchWindow() {
        // Screen definition
        this.searchScreen = new JFrame();
        this.searchScreen.setTitle("Edit Savegame: search players");
        this.searchScreen.setSize(700, 100);
        this.searchScreen.setLocation(250, 250);
        this.searchScreen.setResizable(false);
        ImageIcon img = new ImageIcon(MainMenu.iconName);
        this.searchScreen.setIconImage(img.getImage());
        
        Container windowContent = this.searchScreen.getContentPane();
        windowContent.setLayout(new GridBagLayout());
        
        //--------------------------------
        // Player search section
        //--------------------------------
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel playerLastNameLabel = new JLabel("Enter player Last Name: ");
        c.gridx = 0;
        c.gridy = 1;
        windowContent.add(playerLastNameLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        windowContent.add(playerLastName, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 1;
              
        this.searchButton.setPreferredSize(new Dimension(40, 20));
        windowContent.add(this.searchButton, c);
        this.searchButton.addActionListener((ActionListener) this);

        //-----------------------------------------
        // Display
        //-----------------------------------------
        this.searchScreen.setVisible(true);
        this.searchScreen.addWindowListener( new WindowAdapter() 
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
     * Handle listeners on buttons
     * @param ev 
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        
        // Handle of the modification of the quantity of loaned players
        if(ev.getSource() == this.searchButton)
        {
            try {
                if(this.playerLastName.getText().trim().length() > 0) {
                    this.searchButton.setEnabled(false);
                    this.searchButton.setText("Please wait...");
                    this.displaySearchResults(this.playerLastName.getText());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.searchScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    /**
     * Open the search window results
     * @param String playerLastName
     * @throws IOException 
     */
    private void displaySearchResults(String playerLastName)  throws IOException {
        
        this.log("Searching for player with last name: " + playerLastName);
        List<Player> playerList = this.searchForPlayers(playerLastName);

        //------------------------------
        // Display result window
        //------------------------------
        if(playerList.size() > 0) { // If there is results
            JFrame playerSearchResult = new JFrame("Player search results");
            PlayerListWindow playerDataWindow = new PlayerListWindow(playerList);
            playerDataWindow.displayWindow();
            playerSearchResult.setContentPane(playerDataWindow);
            playerSearchResult.setSize(560, 200);
            playerSearchResult.setLocation(250, 250);
            ImageIcon img = new ImageIcon(MainMenu.iconName);
            playerSearchResult.setIconImage(img.getImage());

            playerSearchResult.addWindowListener( new WindowAdapter() 
            {
                @Override
                public void windowClosing(WindowEvent we) 
                {
                    setSearchButtonActive();
                }
            } );

            playerSearchResult.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this.searchScreen, "No results", "Error", JOptionPane.INFORMATION_MESSAGE);
            this.setSearchButtonActive();
        }
    }
    
    /**
     * Set the search button usable
     */
    private void setSearchButtonActive() {
        searchButton.setEnabled(true);
        searchButton.setText("Search");
        searchScreen.requestFocus();
    }
    
    /**
     * Cross the savegame to look for players
     * @param playerLastName
     * @return playerList
     * @throws java.io.IOException
     */
    public List<Player> searchForPlayers(String playerLastName) throws IOException {
        
        int chunkSize = 156;
        
        // Getting the start position of all the occurences
        List<Long> positions = BinaryFileHelper.getInstance().getStringPosition(playerLastName);
        
        // List of players
        List<Player> playerList = new ArrayList<>();
        
        positions.forEach((position) -> { // Extracting all the players
            position -= 14;
            try {
                long byteLength = BinaryFileHelper.getInstance().geFileLength();
                long maxLength = byteLength - position - 1;
                if (maxLength > chunkSize) {
                    maxLength = chunkSize;
                }

                maxLength --;
                
                byte[] hexValue = BinaryFileHelper.getInstance().getFilePart(position, (int) maxLength);
                System.out.println("Chunk length: " + hexValue.length);
                if(hexValue.length == chunkSize -1) {
                    Player newPlayer = PlayerHelper.getInstance().fillPlayer(position, playerLastName.length(), hexValue, (int) maxLength);
                    playerList.add(newPlayer);
                }
            } catch (SkillValueException exi) {
                // Do nothing
            } catch (Exception ex) {
                Logger.getLogger(SaveGameEditor.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this.searchScreen, "Sorry, and error occurred", "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    
        return playerList;
    }
}
