/**
 * Entry class of Usm9899Tweaker
 * @author Eric COURTIAL
 */
package usm9899tweaker;


/**
 * @author Eric COURTIAL
 */
public class Usm9899Tweaker {
    
    final public static String OPTION_FILE = "options.rtfm";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MainMenu firstWindow = new MainMenu();
        firstWindow.createWindow();
    }
}