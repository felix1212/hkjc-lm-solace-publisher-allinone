/*
 *  0.1 First version
 *  0.2 - Migrate JMS into this app and both send to topic=poc/hkjc/updates/demo
 *  0.2.1 - Added delay option
 */

package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import com.example.demo.service.MqttService;
import com.example.demo.service.JmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class MqttController {

    private static final Logger logger = LoggerFactory.getLogger(MqttController.class);
    private final MqttService mqttService;
    private final JmsService jmsService;

    public MqttController(MqttService mqttService, JmsService jmsService) {
        this.mqttService = mqttService;
        this.jmsService = jmsService;
    }

    /** Publish the configurable JSON from application.properties */
    @GetMapping("/publishmqtt")
    public String publishDefault() throws Exception {
        logger.info("Received GET request for MQTT");
        mqttService.publish();
        return "Published JSON to MQTT with v5 user property 'json'.";
    }

    /** Publish the configurable JSON from application.properties via JMS */
    @GetMapping("/publishjms")
    public String publishJms() throws Exception {
        logger.info("Received GET request for JMS");
        jmsService.publish();
        return "Published JSON to JMS with custom properties.";
    }
}


// import com.example.demo.service.MqttService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.HashMap;
// import java.util.Map;

// @RestController
// public class MqttController {

//     private static final Logger logger = LoggerFactory.getLogger(MqttController.class);

//     @Autowired
//     private MqttService mqttService;

//     @Value("${mqtt.topic.default}")
//     private String defaultTopic;

//     @Value("${mqtt.message.json}")
//     private String configurableJsonMessage;

//     @GetMapping("/publish")
//     public ResponseEntity<Map<String, Object>> publishMessage() {
//         logger.info("Received request to /publish endpoint");
//         logger.debug("Using topic: {}", defaultTopic);
//         logger.debug("Using message: {}", configurableJsonMessage);
        
//         try {
//             // Use configurable JSON message
//             String jsonMessage = configurableJsonMessage;
//             logger.debug("Prepared JSON message: {}", jsonMessage);
            
//             logger.debug("Attempting to publish message to MQTT broker");
//             // Publish the message
//             mqttService.publishMessage(defaultTopic, jsonMessage);
            
//             logger.info("Message published successfully to topic: {}", defaultTopic);
            
//             Map<String, Object> response = new HashMap<>();
//             response.put("success", true);
//             response.put("message", "Message published successfully");
//             response.put("topic", defaultTopic);
//             response.put("payload", jsonMessage);
            
//             logger.debug("Returning success response: {}", response);
//             return ResponseEntity.ok(response);
            
//         } catch (Exception e) {
//             logger.error("Failed to publish message to MQTT broker", e);
            
//             Map<String, Object> response = new HashMap<>();
//             response.put("success", false);
//             response.put("message", "Failed to publish message: " + e.getMessage());
            
//             logger.debug("Returning error response: {}", response);
//             return ResponseEntity.status(500).body(response);
//         }
//     }
// }
