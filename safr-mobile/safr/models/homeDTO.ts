import {Device} from "@/models/device";

export interface HomeDTO {
    uuid: string;
    name: string;
    address: string;
    devices: Device[];
}
