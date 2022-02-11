package main.java.de.voidtech.ytparty.api.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class GatewayConfiguration implements WebSocketConfigurer {
	
	@Autowired
	private GatewayHandler socketHandler; //Inject the Gateway Handler ()
	
	@Override //Override the default registry method with our own method
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketHandler, "/gateway").setAllowedOrigins("*"); 
		//The handler will accept connections from /gateway, from any origin 
	}
}


