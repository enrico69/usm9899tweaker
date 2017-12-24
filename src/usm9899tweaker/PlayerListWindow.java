/**
 * Display the list of players found in a research in a savegame
 * @author Eric COURTIAL
 */
package usm9899tweaker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PlayerListWindow extends JPanel {

    private JList list;
    private final List<Player> playerList;
    private DefaultListModel model;
    private PlayerListWindow theWindow;
    private JButton editButton;

    /**
     * Constructor
     * @param playerList 
     */
    public PlayerListWindow(List<Player> playerList) {
        this.playerList = playerList;
        this.theWindow = this;
    }
    
    /**
     * Display the window content
     */
    public void displayWindow() {
        setLayout(new BorderLayout());
        this.model = new DefaultListModel();
        this.list = new JList(this.model);
        JScrollPane pane = new JScrollPane(this.list);
        this.editButton = new JButton("Edit player");
        
        playerList.forEach((player) -> {
            model.addElement(player.getFirstName() + " " + player.getLastName());
        });


        // Action button lister to edit player
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(list.getSelectedIndex() != -1) {
                    PlayerEditWindow editWindow = new PlayerEditWindow(theWindow, playerList.get(list.getSelectedIndex()));
                    editWindow.display();
                }
            }
        });

        add(pane, BorderLayout.NORTH);
        add(editButton, BorderLayout.EAST);
    }
    
    /**
     * Set the edit button status
     * @param status 
     */
    public void setEditButtonEnabled(boolean status) {
        this.editButton.setEnabled(status);
    }
}