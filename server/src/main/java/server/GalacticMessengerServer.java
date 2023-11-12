package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

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
            SpringApplication.run(GalacticMessengerServer.class, args);
            System.out.println("Galactic Messenger Server started at : " + getIpAddress() + " on port " + System.getProperty("server.port"));
        } else {
            System.setProperty("server.port", String.valueOf("8080"));
            SpringApplication.run(GalacticMessengerServer.class, args);
            System.out.println("Galactic Messenger Server started at : " + getIpAddress() + " on port 8080 (set by default)");
        }
    }
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
