import apiClient from "@/utils/apiClient";
import {DeviceDTO} from "@/models/deviceDTO";

export const createDeviceInHome = async (homeUuid: string, name: string, deviceType: string, description: string, hardwareId: string): Promise<DeviceDTO> => {
    const params = new URLSearchParams({ name, deviceType, description, hardwareId }).toString();
    const response = await apiClient.post(`/devices/${homeUuid}?${params}`);
    return response.data;
};

export const fetchDevicesInHome = async (homeUuid: string): Promise<DeviceDTO[]> => {
    const response = await apiClient.get(`/devices/${homeUuid}`);
    return response.data;
}
