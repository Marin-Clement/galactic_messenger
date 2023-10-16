package server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker( "/queue");
        config.setApplicationDestinationPrefixes("/app");
        System.out.println("Configured message broker");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String endpoint = "/messages";
        registry.addEndpoint(endpoint).withSockJS();
        System.out.println("Registered STOMP endpoint: " + endpoint);
    }
}
