import {Device} from "@/models/device";

export interface Notification {
    uuid: string;
    title: string;
    message: string;
    type: string;
    imageUrl?: string;
    createdAt: string;
    emitter: Device;
}
