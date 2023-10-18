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
        SpringApplication.run(GalacticMessengerServer.class, args);
        System.out.println("Galactic Messenger Server is running!");
    }
}
