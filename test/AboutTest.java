
import com.caucraft.mciguiv3.update.AboutWindow;
import javax.swing.JFrame;

/**
 *
 * @author caucow
 */
public class AboutTest {
    
    public static void main(String[] args) {
        AboutWindow about = new AboutWindow(null);
        about.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        about.setVisible(true);
    }
    
}
