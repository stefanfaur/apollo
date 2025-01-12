import apiClient from "@/utils/apiClient";
import {Device} from "@/models/device";

export const createDeviceInHome = async (homeUuid: string, name: string, deviceType: string, description: string, hardwareId: string): Promise<Device> => {
    const params = new URLSearchParams({ name, deviceType, description, hardwareId }).toString();
    const response = await apiClient.post(`/devices/${homeUuid}?${params}`);
    return response.data;
};

export const fetchDevicesInHome = async (homeUuid: string): Promise<Device[]> => {
    const response = await apiClient.get(`/devices/${homeUuid}`);
    return response.data;
}
