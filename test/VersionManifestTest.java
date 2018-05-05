
import com.caucraft.mciguiv3.gamefiles.versions.manifest.ManifestGameVersion;
import com.caucraft.mciguiv3.gamefiles.versions.manifest.VersionManifest;
import java.util.TreeSet;

/**
 *
 * @author caucow
 */
public class VersionManifestTest {
    
    public static void main(String[] args) throws Exception {
        VersionManifest vm = VersionManifest.getVersionManifest();
        System.out.printf("Latest release: %s%nLatestSnapshot: %s%nVersion list:%n", vm.getLatestRelease(), vm.getLatestSnapshot());
        new TreeSet<>(vm.getVersions()).descendingSet().forEach((vi) -> {
            System.out.println(vi);
        });
        
        ManifestGameVersion info = vm.getVersion(vm.getLatestRelease());
//        info.installVersionJson(new File("D:\\mchome_test"));
    }
    
}
