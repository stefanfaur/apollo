import mqtt from 'mqtt';
import { HelloPayload, NotificationPayload } from '../types';

class MQTTService {
  private client: mqtt.MqttClient | null = null;
  private static instance: MQTTService;
  private connectionRetryTimeout: NodeJS.Timeout | null = null;
  private isConnecting = false;

  private constructor() {
    this.connect();
  }

  private connect() {
    if (this.isConnecting) return;
    this.isConnecting = true;

    try {
      this.client = mqtt.connect('ws://localhost:1884', {
        reconnectPeriod: 5000,  // Try to reconnect every 5 seconds
        connectTimeout: 3000,   // Connection timeout after 3 seconds
      });
      
      this.client.on('connect', () => {
        console.log('Connected to MQTT broker');
        this.isConnecting = false;
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

    } catch (error) {
      console.error('Failed to connect to MQTT broker:', error);
      this.isConnecting = false;
      // Try to reconnect after 5 seconds
      this.connectionRetryTimeout = setTimeout(() => this.connect(), 5000);
    }
  }

  public static getInstance(): MQTTService {
    if (!MQTTService.instance) {
      MQTTService.instance = new MQTTService();
    }
    return MQTTService.instance;
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
