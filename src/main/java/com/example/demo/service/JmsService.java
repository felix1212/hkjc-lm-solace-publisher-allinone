package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;

import javax.jms.*;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

@Service
public class JmsService {

    private static final Logger logger = LoggerFactory.getLogger(JmsService.class);

    @Value("${jms.topic.default:poc/hkjc/updates/demo}")
    private String defaultTopic;

    @Value("${jms.message.json}")
    private String defaultJsonPayload;

    @Value("${solace.jms.host}")
    private String solaceHost;

    @Value("${solace.jms.msgVpn}")
    private String solaceMsgVpn;

    @Value("${solace.jms.clientUsername}")
    private String solaceUsername;

    @Value("${solace.jms.clientPassword}")
    private String solacePassword;

    @Value("${solace.jms.directTransport:false}")
    private boolean directTransport;

    @Value("${jms.delay:0}")
    private long jmsDelay;

    private static final Tracer TRACER = GlobalOpenTelemetry.get().getTracer("poc.solace.jms");

    /**
     * Send the given JSON string as a JMS message
     */
    public void sendJmsMessage(String destination, String json) throws Exception {
        String tgtDestination = (destination == null || destination.isBlank()) ? defaultTopic : destination;
        String traceId = Span.current().getSpanContext().getTraceId();
        
        logger.info("Sending JSON message to JMS destination: {} [traceId: {}]", tgtDestination, traceId);
        logger.debug("JSON payload: {}", json);

        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            // Create Solace connection factory
            SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
            connectionFactory.setHost(solaceHost);
            connectionFactory.setVPN(solaceMsgVpn);
            connectionFactory.setUsername(solaceUsername);
            connectionFactory.setPassword(solacePassword);
            connectionFactory.setDirectTransport(directTransport);
            
            // Create connection and session
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Create destination and producer
            Destination jmsDestination = session.createTopic(tgtDestination);
            producer = session.createProducer(jmsDestination);
            
            // Create message
            TextMessage message = session.createTextMessage(json);
            
            // Add custom properties similar to MQTT user properties
            message.setStringProperty("json", json);
            message.setStringProperty("contentType", "application/json");
            
            // Add W3C Trace Context headers
            String traceparent = Span.current().getSpanContext().getTraceId() + "-" + 
                               Span.current().getSpanContext().getSpanId() + "-" + 
                               (Span.current().getSpanContext().getTraceFlags().isSampled() ? "01" : "00");
            message.setStringProperty("traceparent", traceparent);
            
            logger.debug("Setting JMS properties - contentType: application/json, json={}, traceparent={} [traceId: {}]", 
                        json, traceparent, traceId);
            
            // Send message
            producer.send(message);
            
            logger.info("Successfully sent message to JMS destination: {} [traceId: {}]", tgtDestination, traceId);
            
        } catch (JMSException e) {
            logger.error("Failed to send JMS message", e);
            throw new RuntimeException("Failed to send JMS message", e);
        } finally {
            // Clean up resources
            if (producer != null) {
                try { producer.close(); } catch (JMSException e) { logger.warn("Error closing producer", e); }
            }
            if (session != null) {
                try { session.close(); } catch (JMSException e) { logger.warn("Error closing session", e); }
            }
            if (connection != null) {
                try { connection.close(); } catch (JMSException e) { logger.warn("Error closing connection", e); }
            }
        }
    }

    /** Convenience method using the default JSON from application.properties */
    public void publish() throws Exception {
        Span span = TRACER.spanBuilder("jms.publish")
                .setAttribute("jms.destination", defaultTopic)
                .setAttribute("jms.delay", jmsDelay)
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            String traceId = Span.current().getSpanContext().getTraceId();
            logger.info("Publishing JSON message via JMS [traceId: {}]", traceId);
            logger.debug("Using destination: {} [traceId: {}]", defaultTopic, traceId);
            logger.debug("Using JSON payload: {} [traceId: {}]", defaultJsonPayload, traceId);
            
            // Apply delay if configured before sending
            if (jmsDelay > 0) {
                logger.debug("Applying JMS delay of {}ms [traceId: {}]", jmsDelay, traceId);
                Thread.sleep(jmsDelay);
            }
            
            sendJmsMessage(defaultTopic, defaultJsonPayload);
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
