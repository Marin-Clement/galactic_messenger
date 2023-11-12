package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "server.config")
@ComponentScan(basePackages = "server.controller")
@ComponentScan(basePackages = "server.component")
@ComponentScan(basePackages = "server.messaging")
public class GalacticMessengerServer {
    public static void main(String[] args) {
        if (args.length > 0) {
            int port = Integer.parseInt(args[0]);
            System.setProperty("server.port", String.valueOf(port));
        }
        System.setProperty("server.port", String.valueOf("8080"));
        SpringApplication.run(GalacticMessengerServer.class, args);
        System.out.println("Galactic Messenger Server started on port " + System.getProperty("server.port"));
    }
}
