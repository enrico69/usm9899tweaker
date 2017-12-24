/**
 * Window to handle player edition
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class PlayerEditWindow implements ActionListener {
    
    /* Display elements */
    private final PlayerListWindow parent;
    private JFrame editPlayerScreen;
    final private JButton saveButton = new JButton("Save");
    
    //-> skills attibutes
    final private JTextField KP_Val = new JTextField("", 2);
    final private JTextField TA_Val = new JTextField("", 2);
    final private JTextField PS_Val = new JTextField("", 2);
    final private JTextField SH_Val = new JTextField("", 2);
    final private JTextField PC_Val = new JTextField("", 2);
    final private JTextField FT_Val = new JTextField("", 2);
    final private JTextField HE_Val = new JTextField("", 2);
    final private JTextField ST_Val = new JTextField("", 2);
    final private JTextField SP_Val = new JTextField("", 2);
    final private JTextField BC_Val = new JTextField("", 2);
    
    // The player to edit
    Player thePlayer;
      
    /**
     * Constructor
     * @param PlayerListWindow 
     * @param thePlayer 
     */
    public PlayerEditWindow(PlayerListWindow PlayerListWindow, Player thePlayer) {
        this.parent = PlayerListWindow;
        this.parent.setEditButtonEnabled(false);
        this.thePlayer = thePlayer;
    }
    
    /**
     * Display the window
     */
    public void display() {
        this.editPlayerScreen = new JFrame();
        this.editPlayerScreen.setTitle("Edit player - " + this.thePlayer.getFirstName() + " " + this.thePlayer.getLastName());
        this.editPlayerScreen.setSize(700, 130);
        this.editPlayerScreen.setLocation(250, 250);
        this.editPlayerScreen.setResizable(false);
        ImageIcon img = new ImageIcon(MainMenu.iconName);
        this.editPlayerScreen.setIconImage(img.getImage());
        
        Container windowContent = this.editPlayerScreen.getContentPane();
        windowContent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        
        String[] skillNames = {"KP  ", "TA  ", "PS  ", "SH  ", "PC  ", "FT  ", "HE  ", "ST  ", "SP  ", "BC  "};
        int size = skillNames.length;
        for (int i = 0; i < size; i++)
        {
            JLabel skillsLabel = new JLabel(skillNames[i]);
            c.gridx = i;
            windowContent.add(skillsLabel, c);
        }
        
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        windowContent.add(this.KP_Val, c);
        this.KP_Val.setText(String.valueOf(this.thePlayer.getKP()));
        c.gridx = 1;
        windowContent.add(this.TA_Val, c);
        this.TA_Val.setText(String.valueOf(this.thePlayer.getTA()));
        c.gridx = 2;
        windowContent.add(this.PS_Val, c);
        this.PS_Val.setText(String.valueOf(this.thePlayer.getPS()));
        c.gridx = 3;
        windowContent.add(this.SH_Val, c);
        this.SH_Val.setText(String.valueOf(this.thePlayer.getSH()));
        c.gridx = 4;
        windowContent.add(this.PC_Val, c);
        this.PC_Val.setText(String.valueOf(this.thePlayer.getPC()));
        c.gridx = 5;
        windowContent.add(this.FT_Val, c);
        this.FT_Val.setText(String.valueOf(this.thePlayer.getFT()));
        c.gridx = 6;
        windowContent.add(this.HE_Val, c);
        this.HE_Val.setText(String.valueOf(this.thePlayer.getHE()));
        c.gridx = 7;
        windowContent.add(this.ST_Val, c);
        this.ST_Val.setText(String.valueOf(this.thePlayer.getST()));
        c.gridx = 8;
        windowContent.add(this.SP_Val, c);
        this.SP_Val.setText(String.valueOf(this.thePlayer.getSP()));
        c.gridx = 9;
        windowContent.add(this.BC_Val, c);
        this.BC_Val.setText(String.valueOf(this.thePlayer.getBC()));
        
        c.gridx = 10;
        this.saveButton.setPreferredSize(new Dimension(80, 20));
        this.saveButton.addActionListener((ActionListener) this);
        windowContent.add(this.saveButton, c);
        
        //-------------------------------
        // Display
        //-------------------------------
        this.editPlayerScreen.setVisible(true);
        this.editPlayerScreen.addWindowListener( new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent we) 
            {
                parent.setEditButtonEnabled(true);
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
        if(ev.getSource() == this.saveButton)
        {
            try {
                this.saveButton.setEnabled(false);
                this.setPlayerValues();
                PlayerHelper.getInstance().savePlayer(this.thePlayer);
                JOptionPane.showMessageDialog(this.editPlayerScreen, "Done", "Status", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this.editPlayerScreen, "Sorry, impossible to perform the operation", "Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(PatchGame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SkillValueException ex) {
                JOptionPane.showMessageDialog(this.editPlayerScreen, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            this.saveButton.setEnabled(true);
        }
        
    }
    
    /**
     * Update player values
     * @throws SkillValueException 
     */
    private void setPlayerValues() throws SkillValueException {
        this.thePlayer.setKP(Integer.parseInt(this.KP_Val.getText()));
        this.thePlayer.setTA(Integer.parseInt(this.TA_Val.getText()));
        this.thePlayer.setPS(Integer.parseInt(this.PS_Val.getText()));
        this.thePlayer.setSH(Integer.parseInt(this.SH_Val.getText()));
        this.thePlayer.setPC(Integer.parseInt(this.PC_Val.getText()));
        this.thePlayer.setFT(Integer.parseInt(this.FT_Val.getText()));
        this.thePlayer.setHE(Integer.parseInt(this.HE_Val.getText()));
        this.thePlayer.setST(Integer.parseInt(this.ST_Val.getText()));
        this.thePlayer.setSP(Integer.parseInt(this.SP_Val.getText()));
        this.thePlayer.setBC(Integer.parseInt(this.BC_Val.getText()));
    }
    
}
