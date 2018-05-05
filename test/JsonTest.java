
import com.caucraft.mciguiv3.json.JsonConfig;
import com.google.gson.JsonParser;
import java.io.File;

/**
 *
 * @author caucow
 */
public class JsonTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println(new JsonParser().parse(
                "{\"test\":[{\"values\":[\"first\"]},{\"values\":[\"second\"]},{\"values\":[\"third\"]}]}"
        ).getAsJsonObject().getAsJsonArray("test").getAsJsonArray());
        JsonConfig cfg = new JsonConfig();
        cfg.load(new File("C:\\Users\\caucow\\AppData\\Roaming\\.minecraft\\versions\\18w08b\\18w08b.json"));
        System.out.println(cfg.getRootElement());
        System.out.println(new JsonParser().parse("{\"arguments\":{\"game\":[{\"values\":[\"--username\"]},{\"values\":[\"${auth_player_name}\"]},{\"values\":[\"--version\"]},{\"values\":[\"${version_name}\"]},{\"values\":[\"--gameDir\"]},{\"values\":[\"${game_directory}\"]},{\"values\":[\"--assetsDir\"]},{\"values\":[\"${assets_root}\"]},{\"values\":[\"--assetIndex\"]},{\"values\":[\"${assets_index_name}\"]},{\"values\":[\"--uuid\"]},{\"values\":[\"${auth_uuid}\"]},{\"values\":[\"--accessToken\"]},{\"values\":[\"${auth_access_token}\"]},{\"values\":[\"--userType\"]},{\"values\":[\"${user_type}\"]},{\"values\":[\"--versionType\"]},{\"values\":[\"${version_type}\"]},{\"values\":[\"--demo\"],\"compatibilityRules\":[{\"action\":\"allow\",\"features\":{\"is_demo_user\":true}}]},{\"values\":[\"--width\",\"${resolution_width}\",\"--height\",\"${resolution_height}\"],\"compatibilityRules\":[{\"action\":\"allow\",\"features\":{\"has_custom_resolution\":true}}]}],\"jvm\":[{\"values\":[\"-XstartOnFirstThread\"],\"compatibilityRules\":[{\"action\":\"allow\",\"os\":{\"name\":\"osx\"}}]},{\"values\":[\"-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump\"],\"compatibilityRules\":[{\"action\":\"allow\",\"os\":{\"name\":\"windows\"}}]},{\"values\":[\"-Dos.name=Windows 10\",\"-Dos.version=10.0\"],\"compatibilityRules\":[{\"action\":\"allow\",\"os\":{\"name\":\"windows\",\"version\":\"^10\\\\.\"}}]},{\"values\":[\"-Djava.library.path=${natives_directory}\"]},{\"values\":[\"-Dminecraft.launcher.brand=${launcher_name}\"]},{\"values\":[\"-Dminecraft.launcher.version=${launcher_version}\"]},{\"values\":[\"-cp\"]},{\"values\":[\"${classpath}\"]}]}}")
                .getAsJsonObject()
                .get("arguments")
                .getAsJsonObject()
                .get("game")
                .getAsJsonArray());
    }
    
}
