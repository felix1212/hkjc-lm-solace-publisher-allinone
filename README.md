# Spring Boot MQTT Publisher Demo

This Spring Boot application provides REST endpoints to publish messages to Solace via MQTT.

## Configuration

The application is configured via `application.properties`:

```properties
# MQTT Configuration
mqtt.broker.url=tcp://localhost:1883
mqtt.broker.username=
mqtt.broker.password=
mqtt.client.id=spring-boot-demo
mqtt.topic.default=/poc/hkjc/updates/demo
mqtt.qos=1
mqtt.retained=false
```

## Endpoints

### POST /api/publish
Publish a message via POST request with JSON body.

**Request Body:**
```json
"Your message here"
```

**Query Parameters:**
- `topic` (optional): MQTT topic (defaults to `/poc/hkjc/updates/demo`)
- `qos` (optional): Quality of Service 0, 1, or 2 (defaults to 1)
- `retained` (optional): Retain message true/false (defaults to false)

**Example:**
```bash
curl -X POST "http://localhost:8080/api/publish" \
  -H "Content-Type: application/json" \
  -d "Hello from Spring Boot" \
  --data-urlencode "topic=/custom/topic" \
  --data-urlencode "qos=1" \
  --data-urlencode "retained=false"
```

### GET /api/publish
Publish a message via GET request with query parameters.

**Query Parameters:**
- `message` (optional): Message to publish (defaults to "Hello from Spring Boot MQTT Publisher")
- `topic` (optional): MQTT topic (defaults to configured default topic)
- `qos` (optional): Quality of Service 0, 1, or 2 (defaults to 1)
- `retained` (optional): Retain message true/false (defaults to false)

**Example:**
```bash
curl "http://localhost:8080/api/publish?message=Hello%20World&topic=/test/topic&qos=1&retained=false"
```

### GET /api/config
Get application configuration and endpoint information.

**Example:**
```bash
curl "http://localhost:8080/api/config"
```

## Running the Application

1. Make sure Solace is running locally on port 1883 (or update the configuration)
2. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```
3. The application will start on port 8080

## Testing

You can test the endpoints using curl or any HTTP client:

```bash
# Simple message publish
curl -X POST "http://localhost:8080/api/publish" -H "Content-Type: application/json" -d "Test message"

# Publish with custom topic
curl "http://localhost:8080/api/publish?message=Custom%20Message&topic=/custom/topic"

# Get configuration
curl "http://localhost:8080/api/config"
```

## Dependencies

The application uses:
- Spring Boot Web Starter
- Spring Integration MQTT
- Eclipse Paho MQTT Client
