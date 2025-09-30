// MqttConfig.java
package com.example.demo.config;

import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.broker.username:}")
    private String username;

    @Value("${mqtt.broker.password:}")
    private String password;

    @Bean(destroyMethod = "disconnect")
    public MqttAsyncClient mqttV5Client() throws Exception {
        MqttAsyncClient client = new MqttAsyncClient(brokerUrl, clientId + "-v5", new MemoryPersistence());

        MqttConnectionOptions opts = new MqttConnectionOptions();
        // Auth (optional)
        if (username != null && !username.isBlank()) opts.setUserName(username);
        if (password != null && !password.isBlank()) opts.setPassword(password.getBytes());

        // Automatic reconnect
        opts.setAutomaticReconnect(true);
        opts.setCleanStart(true);
        opts.setConnectionTimeout(10);
        opts.setKeepAliveInterval(60);

        try {
            client.connect(opts).waitForCompletion();
            System.out.println("MQTT client connected successfully to: " + brokerUrl);
        } catch (Exception e) {
            System.err.println("Failed to connect to MQTT broker: " + brokerUrl);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
        return client;
    }
}



// package com.example.demo.config;

// import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.integration.annotation.ServiceActivator;
// import org.springframework.integration.channel.DirectChannel;
// import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
// import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
// import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
// import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
// import org.springframework.messaging.MessageChannel;
// import org.springframework.messaging.MessageHandler;

// @Configuration
// public class MqttConfig {

//     @Value("${mqtt.broker.url}")
//     private String brokerUrl;

//     @Value("${mqtt.broker.username:}")
//     private String username;

//     @Value("${mqtt.broker.password:}")
//     private String password;

//     @Value("${mqtt.client.id}")
//     private String clientId;

//     @Value("${mqtt.qos}")
//     private int qos;

//     @Value("${mqtt.retained}")
//     private boolean retained;

//     @Bean
//     public MqttPahoClientFactory mqttClientFactory() {
//         DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
//         MqttConnectOptions options = new MqttConnectOptions();
//         options.setServerURIs(new String[]{brokerUrl});
//         if (!username.isEmpty()) {
//             options.setUserName(username);
//         }
//         if (!password.isEmpty()) {
//             options.setPassword(password.toCharArray());
//         }
//         options.setCleanSession(true);
//         options.setAutomaticReconnect(true);
//         factory.setConnectionOptions(options);
//         return factory;
//     }

//     @Bean
//     public MessageChannel mqttOutboundChannel() {
//         return new DirectChannel();
//     }

//     @Bean
//     @ServiceActivator(inputChannel = "mqttOutboundChannel")
//     public MessageHandler mqttOutbound() {
//         MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
//         messageHandler.setAsync(true);
//         messageHandler.setDefaultQos(qos);
//         messageHandler.setDefaultRetained(retained);
//         messageHandler.setConverter(new DefaultPahoMessageConverter());
//         return messageHandler;
//     }
// }
