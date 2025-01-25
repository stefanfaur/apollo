import {DeviceDTO} from "@/models/deviceDTO";

export interface HomeDTO {
    uuid: string;
    name: string;
    address: string;
    devices: DeviceDTO[];
}
