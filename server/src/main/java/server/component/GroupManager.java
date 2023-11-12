package server.component;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GroupManager {
    private final HashMap<String, Set<String>> groups = new HashMap<>();

    public void addGroup(String groupName) {
        groups.put(groupName, ConcurrentHashMap.newKeySet());
        System.out.println("Group " + groupName + " created.");
    }

    public void removeGroup(String groupName) {
        groups.remove(groupName);
        System.out.println("Group " + groupName + " deleted.");
    }

    public void addUserToGroup(String groupName, String username) {
        groups.get(groupName).add(username);
        System.out.println("User " + username + " added to group " + groupName + ".");
    }

    public void removeUserFromGroup(String groupName, String username) {
        groups.get(groupName).remove(username);
        System.out.println("User " + username + " removed from group " + groupName + ".");
    }

    public boolean isUserInGroup(String groupName, String username) {
        return groups.get(groupName).contains(username);
    }

    public boolean isGroupExists(String groupName) {
        return groups.containsKey(groupName);
    }

    public Set<String> getGroupMembers(String groupName) {
        return groups.get(groupName);
    }

    public Set<String> getGroups() {
        return groups.keySet();
    }
}
