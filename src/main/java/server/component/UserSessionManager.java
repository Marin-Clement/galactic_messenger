package server.component;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionManager {
    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public void addUser(String username) {
        connectedUsers.add(username);
        System.out.println("User " + username + " connected.");
    }

    public void removeUser(String username) {
        connectedUsers.remove(username);
        System.out.println("User " + username + " disconnected.");
    }

    public boolean isUserConnected(String username) {
        return connectedUsers.contains(username);
    }
}
