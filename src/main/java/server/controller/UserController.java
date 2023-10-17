package server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
class UserController {
    private Map<String, String> users = new HashMap<>();

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        users.put(username, password);
        System.out.println("Registered user " + username + " with password " + password);
        return "User registered successfully.";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password) {
        String storedPassword = users.get(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            return "Login successful.";
        } else {
            return "Invalid credentials.";
        }
    }
}
