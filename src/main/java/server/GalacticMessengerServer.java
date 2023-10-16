package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GalacticMessengerServer {
    public static void main(String[] args) {
        SpringApplication.run(GalacticMessengerServer.class, args);
        System.out.println("Galactic Messenger Server is running!");
    }
}
