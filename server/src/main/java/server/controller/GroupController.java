package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import server.component.GroupManager;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GroupController {
    private final GroupManager groupManager;

    @Autowired
    public GroupController(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @PostMapping("/create-group")
    public String createGroup(@RequestParam String groupName) {
        if (groupManager.isGroupExists(groupName)) {
            return "Group already exists.";
        } else {
            groupManager.addGroup(groupName);
            return "Group created.";
        }
    }

    @PostMapping("/delete-group")
    public String deleteGroup(@RequestParam String groupName) {
        if (groupManager.isGroupExists(groupName)) {
            groupManager.removeGroup(groupName);
            return "Group deleted.";
        } else {
            return "Group doesn't exist.";
        }
    }

    @PostMapping("/add-user-to-group")
    public String addUserToGroup(@RequestParam String groupName, @RequestParam String username) {
        if (groupManager.isGroupExists(groupName)) {
            if (groupManager.isUserInGroup(groupName, username)) {
                return "User is already in the group.";
            } else {
                groupManager.addUserToGroup(groupName, username);
                return "User added to the group.";
            }
        } else {
            return "Group doesn't exist.";
        }
    }

    @PostMapping("/remove-user-from-group")
    public String removeUserFromGroup(@RequestParam String groupName, @RequestParam String username) {
        if (groupManager.isGroupExists(groupName)) {
            if (groupManager.isUserInGroup(groupName, username)) {
                groupManager.removeUserFromGroup(groupName, username);
                return "User removed from the group.";
            } else {
                return "User is not in the group.";
            }
        } else {
            return "Group doesn't exist.";
        }
    }

    @PostMapping("/get-groups")
    public Map<String, String> getGroups() {
        Map<String, String> groups = new HashMap<>();
        for (String groupName : groupManager.getGroups()) {
            groups.put(groupName, String.join(", ", groupManager.getGroupMembers(groupName)));
        }
        return groups;
    }

    @PostMapping("/get-group-members")
    public String getGroupMembers(@RequestParam String groupName) {
        if (groupManager.isGroupExists(groupName)) {
            return String.join(", ", groupManager.getGroupMembers(groupName));
        } else {
            return "Group doesn't exist.";
        }
    }
}