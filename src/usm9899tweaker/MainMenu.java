/*
 * Class of the main menu
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MainMenu implements ActionListener {
    
    private JFrame mainMenu;
    
    final private static JButton patchGameButton = new JButton("Patch the game");
    final private static JButton editSaveGameButton = new JButton("Edit a savegame");
    final private static JButton aboutButton = new JButton("About");
    final private static JButton quitButton = new JButton("Exit");
    public static String iconName = "mainIcon.png";
    
    /**
     * Constructor
     */
    public MainMenu()
    {
        System.out.println("\nCreating main window");
    }
    
    /**
     * Display the RTFM dialog box
     * @return boolean
     */
    private void displayRTFM() {
        
        File f = new File(Usm9899Tweaker.OPTION_FILE);
        if(!f.exists() && !f.isDirectory()) {
            String question = "Did you read the awesome, clear and concise documentation?";
            int result = JOptionPane.showConfirmDialog(this.mainMenu, question, "RTFM", JOptionPane.YES_NO_OPTION);
            boolean response = (result != 1);

            if(response) {
                JOptionPane.showMessageDialog(this.mainMenu, "Thank you, you're a good guy / girl", "Gracias", JOptionPane.INFORMATION_MESSAGE);
            } else {
                try {
                    Desktop.getDesktop().open(new File("documentation.html"));
                } catch (IOException ex) {
                    Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            try {
                PrintWriter writer = new PrintWriter("options.rtfm", "UTF-8");
                writer.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    /**
     * Create the main menu
     */
    public void createWindow()
    {
        this.mainMenu = new JFrame();
        this.mainMenu.setTitle("USM 98-99 Tweaker v1.0");
        
        this.mainMenu.setSize(300, 270);
        this.mainMenu.setLocation(250, 250);
        this.mainMenu.setResizable(false);
        
        ImageIcon img = new ImageIcon(MainMenu.iconName);
        this.mainMenu.setIconImage(img.getImage());
        
        Container windowContent = this.mainMenu.getContentPane();
        windowContent.setLayout(new GridLayout(4,1));
        
        windowContent.add(MainMenu.patchGameButton);
        MainMenu.patchGameButton.addActionListener(this);
        windowContent.add(MainMenu.editSaveGameButton);
        MainMenu.editSaveGameButton.addActionListener(this);
        windowContent.add(MainMenu.aboutButton);
        MainMenu.aboutButton.addActionListener(this);
        windowContent.add(MainMenu.quitButton);
        MainMenu.quitButton.addActionListener(this);
        
        this.mainMenu.setVisible(true);
        
        this.mainMenu.addWindowListener( new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent we) 
            {
                System.exit(0);
            }
        } );
        
        this.displayRTFM();
    }
    
    /**
     * Handle listeners on buttons
     * @param ev 
     */
    @Override
    public void actionPerformed (ActionEvent ev)
    {          
        // Patch game
        if(ev.getSource() == MainMenu.patchGameButton)
        {
            PatchGame patcher = new PatchGame(this.mainMenu);
            try {
                patcher.launch();
            } catch (Exception ex) {
                this.showErrorMessage("An error was encountered. This is the log trace: " + ex.getMessage());
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                this.mainMenu.setEnabled(true);
            }
        }
        
        // Edit savegame
        if(ev.getSource() == MainMenu.editSaveGameButton)
        {
            SaveGameEditor saveGameEditor = new SaveGameEditor(this.mainMenu);
            try {
                saveGameEditor.launch();
            } catch (Exception ex) {
                this.showErrorMessage("An error was encountered. This is the log trace: " + ex.getMessage());
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                this.mainMenu.setEnabled(true);
            }
        }
        
        // About
        if(ev.getSource() == MainMenu.aboutButton)
        {
            this.showAboutBox();
        }
        
        // Exit
        if(ev.getSource() == MainMenu.quitButton)
        {
            this.exit();
        }
    }
    
    /**
     * Exit shortcut
     */
    private void exit()
    {
        System.exit(0);
    }
    
    /**
     * Show the "About" message box
     */
    private void showAboutBox() {
        String auth = "e.courtial";
        
        String message = "This software is realeased without any garantee or support.";
        message += "\nThe source code is available on GitHub, so feel free to contribute.";
        message += "\nAuthor: The USM Community, since 2017.";
        
        message += "\n\nContributors: Ande PEARSON, Christian SMITH, Dane SMALLBONE, Eric COURTIAL";
        message += "\nSpecial thanks to the game dev's team for having created the USM series.";
        JOptionPane.showMessageDialog(this.mainMenu, message, "About", 1);
    }
    
    /**
     * Show the error message message
     * @param message 
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this.mainMenu, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

}
