import apiClient from '../utils/apiClient';

export interface Device {
  uuid: string;
  name: string;
  description: string;
  deviceType: string;
  hardwareId: string;
}

export const deviceService = {
  createDeviceInHome: async (
    homeId: string,
    name: string,
    deviceType: string,
    description: string,
    hardwareId: string
  ): Promise<Device> => {
    console.log('Creating device in home:', homeId, name, description, hardwareId);
    try {
      const response = await apiClient.post(`/home/${homeId}/devices`, {
        name,
        deviceType,
        description,
        hardwareId,
      });
      return response.data;
    } catch (error) {
      console.error('Failed to create device:', error);
      throw error;
    }
  },

  getHomeDevices: async (homeId: string): Promise<Device[]> => {
    try {
      const response = await apiClient.get(`/home/${homeId}/devices`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch home devices:', error);
      throw error;
    }
  },

  getDevice: async (deviceId: string): Promise<Device> => {
    try {
      const response = await apiClient.get(`/devices/${deviceId}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch device:', error);
      throw error;
    }
  },
};
