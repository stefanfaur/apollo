import { Notification } from "@/models/notification";
import apiClient from "@/utils/apiClient";

const NotificationService = {
    async getNotificationsForDevice(deviceUuid: string): Promise<Notification[]> {
        const response = await apiClient.get(`/notification/device/${deviceUuid}`);
        return response.data;
    },

    async getNotificationsForUser(): Promise<Notification[]> {
        const response = await apiClient.get(`/notification/user`);
        return response.data;
    },
};

export default NotificationService;
