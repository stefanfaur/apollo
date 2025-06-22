export interface Device {
  id: string;
  isActive: boolean;
  deviceType: string;
  lastMessageTime?: string;
}

export interface NotificationPayload {
  hardwareId: string;
  title: string;
  message: string;
  mediaUrl?: string;
}

export interface HelloPayload {
  hardwareId: string;
  deviceType: string;
}

export interface PresignedUrlResponse {
  url: string;
  objectName: string;
}
