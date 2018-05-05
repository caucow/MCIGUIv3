
import com.caucraft.mciguiv3.gamefiles.auth.Authenticator;
import java.util.UUID;

/**
 *
 * @author caucow
 */
public class AuthTest {
    public static void main(String[] args) throws Exception {
        System.out.println(Authenticator.loginWithPassword(UUID.randomUUID().toString(), "caucow98@gmail.com", "ChaseUherek1998"));
    }
}
