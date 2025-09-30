package com.example.demo.service;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private final MqttAsyncClient client;

    @Value("${mqtt.topic.default}")
    private String defaultTopic;

    @Value("${mqtt.qos:1}")
    private int qos;

    @Value("${mqtt.retained:false}")
    private boolean retained;

    @Value("${mqtt.message.json}")
    private String defaultJsonPayload;

    @Value("${mqtt.delay:0}")
    private long mqttDelay;

    public MqttService(MqttAsyncClient mqttV5Client) {
        this.client = mqttV5Client;
    }

    private static final Tracer TRACER = GlobalOpenTelemetry.get().getTracer("poc.solace.mqtt");
    private static final TextMapPropagator PROPAGATOR = GlobalOpenTelemetry.get().getPropagators().getTextMapPropagator();

    /**
     * Publish the given JSON string as:
     *   - message payload (bytes)
     *   - MQTT v5 User Property key "json" -> same JSON string
     */
    public void sendMqttMessage(String topic, String json) throws Exception {
        String tgtTopic = (topic == null || topic.isBlank()) ? defaultTopic : topic;
        String traceId = Span.current().getSpanContext().getTraceId();
        
        logger.info("Publishing JSON message to topic: {} [traceId: {}]", tgtTopic, traceId);
        logger.debug("JSON payload: {}", json);

        MqttMessage msg = new MqttMessage(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        msg.setQos(qos);
        msg.setRetained(retained);
        
        logger.debug("Message QoS: {}, Retained: {} [traceId: {}]", qos, retained, traceId);
        
        // Create a carrier to inject trace context
        java.util.Map<String, String> carrier = new java.util.HashMap<>();
        PROPAGATOR.inject(io.opentelemetry.context.Context.current(), carrier, (carrierMap, key, value) -> carrierMap.put(key, value));
        
        MqttProperties props = new MqttProperties();
        props.setContentType("application/json");
        java.util.List<UserProperty> ups = new java.util.ArrayList<>();
        ups.add(new UserProperty("json", json));
        
        // Add W3C Trace Context headers
        if (carrier.containsKey("traceparent")) {
            ups.add(new UserProperty("traceparent", carrier.get("traceparent")));
        }
        if (carrier.containsKey("tracestate")) {
            ups.add(new UserProperty("tracestate", carrier.get("tracestate")));
        }
        
        props.setUserProperties(ups);
        msg.setProperties(props);
        
        logger.debug("Setting MQTT v5 properties - ContentType: application/json, UserProperty: json={}, traceparent={}, tracestate={} [traceId: {}]", 
                    json, carrier.get("traceparent"), carrier.get("tracestate"), traceId);
        
        IMqttToken token = client.publish(tgtTopic, msg);
        logger.debug("Publish token created, waiting for completion... [traceId: {}]", traceId);
        token.waitForCompletion();
        
        logger.info("Successfully published message to topic: {} [traceId: {}]", tgtTopic, traceId);
    }

    /** Convenience method using the default JSON from application.properties */
    public void publish() throws Exception {
        Span span = TRACER.spanBuilder("mqtt.publish")
                .setAttribute("mqtt.topic", defaultTopic)
                .setAttribute("mqtt.delay", mqttDelay)
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            String traceId = Span.current().getSpanContext().getTraceId();
            logger.info("Publishing JSON message [traceId: {}]", traceId);
            logger.debug("Using topic: {} [traceId: {}]", defaultTopic, traceId);
            logger.debug("Using JSON payload: {} [traceId: {}]", defaultJsonPayload, traceId);
            
            // Apply delay if configured before sending
            if (mqttDelay > 0) {
                logger.debug("Applying MQTT delay of {}ms [traceId: {}]", mqttDelay, traceId);
                Thread.sleep(mqttDelay);
            }
            
            sendMqttMessage(defaultTopic, defaultJsonPayload);
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
