import apiClient from '../utils/apiClient';

export interface AdminDTO {
  uuid: string;
  email: string;
}

export interface GuestDeviceRights {
  deviceId: string;
  rights: string[];
}

export interface GuestDTO {
  uuid: string;
  email: string;
  devices: GuestDeviceRights[];
}

export const homeAccessService = {
  // Admin Management
  getHomeAdmins: async (homeId: string): Promise<AdminDTO[]> => {
    try {
      const response = await apiClient.get(`/home/${homeId}/admins`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch home admins:', error);
      throw error;
    }
  },

  addHomeAdmin: async (homeId: string, email: string): Promise<void> => {
    try {
      await apiClient.post(`/home/${homeId}/admins`, null, {
        params: { email }
      });
    } catch (error) {
      console.error('Failed to add home admin:', error);
      throw error;
    }
  },

  removeHomeAdmin: async (homeId: string, adminId: string): Promise<void> => {
    try {
      await apiClient.delete(`/home/${homeId}/admins/${adminId}`);
    } catch (error) {
      console.error('Failed to remove home admin:', error);
      throw error;
    }
  },

  // Guest Management
  getHomeGuests: async (homeId: string): Promise<GuestDTO[]> => {
    try {
      const response = await apiClient.get(`/home/${homeId}/guests`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch home guests:', error);
      throw error;
    }
  },

  addHomeGuest: async (homeId: string, email: string, deviceRights: { deviceId: string, rights: Set<string> }[]): Promise<void> => {
    try {
      await apiClient.post(`/home/${homeId}/guests`, {
        email,
        deviceRights: deviceRights.map(({ deviceId, rights }) => ({
          deviceId,
          rights: Array.from(rights)
        }))
      });
    } catch (error) {
      console.error('Failed to add home guest:', error);
      throw error;
    }
  },

  updateGuestDeviceRights: async (
    homeId: string,
    guestId: string,
    deviceRights: { deviceId: string, rights: Set<string> }[]
  ): Promise<void> => {
    try {
      await apiClient.put(`/home/${homeId}/guests/${guestId}/devices`,
        deviceRights.map(({ deviceId, rights }) => ({
          deviceId,
          rights: Array.from(rights)
        }))
      );
    } catch (error) {
      console.error('Failed to update guest device rights:', error);
      throw error;
    }
  },

  removeHomeGuest: async (homeId: string, guestId: string): Promise<void> => {
    try {
      await apiClient.delete(`/home/${homeId}/guests/${guestId}`);
    } catch (error) {
      console.error('Failed to remove home guest:', error);
      throw error;
    }
  }
};
