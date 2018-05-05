import com.caucraft.mciguiv3.gamefiles.profiles.LauncherProfiles;
import java.io.File;

/**
 *
 * @author caucow
 */
public class LauncherProfilesTest {
    
    public static void main(String[] args) throws Exception {
        LauncherProfiles lp = new LauncherProfiles(new File("C:\\Users\\caucow\\AppData\\Roaming\\.minecraft\\launcher_profiles.json"));
        lp.load();
        lp.save(true);
    }
    
}