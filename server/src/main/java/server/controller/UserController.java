package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import server.component.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private final Map<String, String> users = new HashMap<>();
    private final UserSessionManager userSessionManager;

    @Autowired
    public UserController(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        if (users.containsKey(username)) {
            return "Username already exists.";
        } else {
            users.put(username, password);
            return "Registration successful.";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password) {
        String storedPassword = users.get(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            userSessionManager.addUser(username);
            return "Login successful.";
        } else {
            return "Login failed.";
        }
    }

    @PostMapping("/logout")
    public String logoutUser(@RequestParam String username) {
        userSessionManager.removeUser(username);
        return "Logout successful.";
    }

    @PostMapping("/online-users")
    public String getUsers() {
        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, String> entry : users.entrySet()){
            output.append(entry.getKey() + " ");
        }
        return output.toString();
    }
}
