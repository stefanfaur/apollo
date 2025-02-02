import {DeviceDTO} from "@/models/deviceDTO";

export interface NotificationDTO {
    uuid: string;
    title: string;
    message: string;
    type: string;
    mediaUrl?: string;
    createdAt: string;
    emitter: DeviceDTO;
}
