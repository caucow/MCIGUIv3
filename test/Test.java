
import com.caucraft.mciguiv3.launch.Launcher;
import java.lang.management.RuntimeMXBean;
import javax.swing.UIManager;

/**
 *
 * @author caucow
 */
public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(Launcher.CD);
        System.out.println(System.getProperties().toString().replace(',', '\n'));
        RuntimeMXBean bean = java.lang.management.ManagementFactory.getRuntimeMXBean();
        System.out.println(bean.getInputArguments());
        System.out.println(bean.getName());
        System.out.println(bean.getSpecName());
        System.out.println(bean.getVmName());
        System.out.println(UIManager.getDefaults());
    }
}
