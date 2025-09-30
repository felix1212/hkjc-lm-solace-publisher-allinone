# Spring Boot MQTT & JMS Publisher Demo

This Spring Boot application provides REST endpoints to publish messages to Solace via both MQTT and JMS protocols with OpenTelemetry tracing support.

## Features

- **Dual Protocol Support**: MQTT and JMS messaging
- **OpenTelemetry Integration**: Distributed tracing with W3C Trace Context propagation
- **Configurable Delays**: Built-in delay options for easier trace observation
- **Solace Integration**: Native support for Solace MQTT and JMS

## Configuration

The application is configured via `application.properties`:

```properties
# MQTT Configuration
mqtt.broker.url=tcp://localhost:1883
mqtt.broker.username=app-pub
mqtt.broker.password=P@ss1234
mqtt.client.id=spring-boot-demo
mqtt.topic.default=poc/hkjc/updates/demo
mqtt.message.json={"message@MQTT":"Hello subscriber!!!"}
mqtt.qos=1
mqtt.retained=false
mqtt.delay=0

# JMS Configuration (Solace)
solace.jms.host=smf://localhost:55555
solace.jms.msgVpn=default
solace.jms.clientUsername=app-pub
solace.jms.clientPassword=P@ss1234
solace.jms.directTransport=false
jms.message.json={"message@JMS":"Hello JMS subscriber!!!"}
jms.topic.default=poc/hkjc/updates/demo
jms.delay=0

# Server Configuration
server.port=13579
```

## Endpoints

### GET /publishmqtt
Publish a message via MQTT using the configured JSON message from `mqtt.message.json`.

**Features:**
- Uses MQTT v5 with user properties
- Includes OpenTelemetry trace context propagation
- Configurable delay via `mqtt.delay` property

**Example:**
```bash
curl "http://localhost:13579/publishmqtt"
```

**Response:**
```
Published JSON to MQTT with v5 user property 'json'.
```

### GET /publishjms
Publish a message via JMS using the configured JSON message from `jms.message.json`.

**Features:**
- Uses Solace JMS with custom properties
- Includes OpenTelemetry trace context propagation
- Configurable delay via `jms.delay` property

**Example:**
```bash
curl "http://localhost:13579/publishjms"
```

**Response:**
```
Published JSON to JMS with custom properties.
```

## OpenTelemetry Integration

Both endpoints include comprehensive OpenTelemetry tracing:

- **Trace Context Propagation**: W3C Trace Context headers are included in message properties
- **Span Attributes**: Each operation includes relevant attributes (topic, delay, etc.)
- **Error Handling**: Exceptions are recorded in spans with proper error status
- **Performance Monitoring**: Built-in timing and performance metrics

## Running the Application

### Prerequisites
1. **Solace PubSub+ Broker**: Running on localhost:1883 (MQTT) and localhost:55555 (JMS)
2. **Java 17+**: Required for Spring Boot 3.x
3. **Maven 3.6+**: For building the application

### Build and Run
```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on port **13579**.

## Testing

### Basic Testing
```bash
# Test MQTT endpoint
curl "http://localhost:13579/publishmqtt"

# Test JMS endpoint  
curl "http://localhost:13579/publishjms"
```

### Testing with Delays
To make tracing easier to observe, configure delays in `application.properties`:

```properties
# Add delays for better trace visibility
mqtt.delay=2000  # 2 second delay for MQTT
jms.delay=3000   # 3 second delay for JMS
```

### Docker Deployment
The application can be deployed using Docker:

```bash
# Build Docker image
docker build -t hkjc-lm-solace-publisher-mqtt:latest .

# Run with docker-compose (includes OpenTelemetry collector)
docker-compose up -d
```

## Dependencies

### Core Dependencies
- **Spring Boot 3.5.6**: Web framework and auto-configuration
- **Eclipse Paho MQTT v5**: MQTT client with v5 support
- **Solace JMS**: Native Solace JMS integration
- **OpenTelemetry**: Distributed tracing and observability

### Key Features
- **MQTT v5 Support**: User properties and enhanced features
- **JMS Integration**: Native Solace JMS connectivity
- **OpenTelemetry Tracing**: Comprehensive observability
- **Configurable Delays**: For easier trace observation
- **Docker Support**: Containerized deployment with OpenTelemetry collector
