import mqtt from 'mqtt';
import { HelloPayload, NotificationPayload } from '../types';

class MQTTService {
  private client: mqtt.MqttClient | null = null;
  private static instance: MQTTService;
  private connectionRetryTimeout: NodeJS.Timeout | null = null;
  private isConnecting = false;
  // Generic listeners invoked for **every** incoming message
  private messageListeners: ((topic: string, payload: any) => void)[] = [];

  private constructor() {
    this.connect();
  }

  private connect() {
    if (this.isConnecting) return;
    this.isConnecting = true;

    try {
      // Connect via WebSocket to MQTT broker
      // Port 1884 is configured for WebSocket connections in mosquitto.conf
      // Standard MQTT port 1883 is used by backend services
      this.client = mqtt.connect('ws://localhost:1884', {
        reconnectPeriod: 5000,  // Try to reconnect every 5 seconds
        connectTimeout: 3000,   // Connection timeout after 3 seconds
      });
      
      this.client.on('connect', () => {
        console.log('Connected to MQTT broker');
        this.isConnecting = false;
        this.subscribeToDefaultTopics();
      });

      this.client.on('error', (err: Error) => {
        console.error('MQTT error:', err);
      });

      this.client.on('close', () => {
        console.log('MQTT connection closed');
      });

      this.client.on('offline', () => {
        console.log('MQTT client is offline');
      });

      // Forward all messages to the listeners
      this.client.on('message', (topic, message) => {
        let payload: any = null;
        try {
          payload = JSON.parse(message.toString());
        } catch {
          payload = message.toString();
        }
        this.messageListeners.forEach((fn) => fn(topic, payload));
      });

    } catch (error) {
      console.error('Failed to connect to MQTT broker:', error);
      this.isConnecting = false;
      // Try to reconnect after 5 seconds
      this.connectionRetryTimeout = setTimeout(() => this.connect(), 5000);
    }
  }

  private subscribeToDefaultTopics() {
    if (!this.client) return;
    const topics = ['devices/commands/unlock', 'doorlock/+/enroll/start'];
    this.client.subscribe(topics, (err, granted) => {
      if (err) {
        console.error('Failed to subscribe to default topics', err);
      } else {
        console.log('Subscribed to topics:', granted?.map(g => g.topic).join(', '));
      }
    });
  }

  public static getInstance(): MQTTService {
    if (!MQTTService.instance) {
      MQTTService.instance = new MQTTService();
    }
    return MQTTService.instance;
  }

  public addMessageListener(fn: (topic: string, payload: any) => void) {
    this.messageListeners.push(fn);
  }

  public removeMessageListener(fn: (topic: string, payload: any) => void) {
    this.messageListeners = this.messageListeners.filter((f) => f !== fn);
  }

  public publishHello(payload: HelloPayload) {
    if (!this.client?.connected) {
      console.warn('MQTT client not connected, message queued');
      return;
    }
    this.client.publish('devices/hello', JSON.stringify(payload));
  }

  public publishNotification(payload: NotificationPayload) {
    if (!this.client?.connected) {
      console.warn('MQTT client not connected, message queued');
      return;
    }
    this.client.publish('devices/notifications', JSON.stringify(payload));
  }

  public publishEnrollStatus(topic: string, payload: any) {
    if (!this.client?.connected) return;
    this.client.publish(topic, JSON.stringify(payload));
  }

  public publishDoorEvent(topic: string, payload: any) {
    if (!this.client?.connected) return;
    this.client.publish(topic, JSON.stringify(payload));
  }

  // Publish sensor event notification with proper eventType field
  public publishSensorNotification(payload: any) {
    if (!this.client?.connected) return;
    // Add eventType field based on the message content for backend processing
    const enhancedPayload = {
      ...payload,
      eventType: payload.title || payload.message, // Backend uses this for categorization
    };
    this.client.publish('devices/notifications', JSON.stringify(enhancedPayload));
  }

  public disconnect() {
    if (this.connectionRetryTimeout) {
      clearTimeout(this.connectionRetryTimeout);
    }
    if (this.client?.connected) {
      this.client.end();
    }
  }
}

export default MQTTService;
