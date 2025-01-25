import { NotificationDTO } from "@/models/notificationDTO";
import apiClient from "@/utils/apiClient";

const NotificationService = {
    async getNotificationsForDevice(deviceUuid: string): Promise<NotificationDTO[]> {
        const response = await apiClient.get(`/notification/device/${deviceUuid}`);
        return response.data;
    },

    async getNotificationsForUser(): Promise<NotificationDTO[]> {
        const response = await apiClient.get(`/notification/user`);
        return response.data;
    },
};

export default NotificationService;
