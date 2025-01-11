import apiClient from "@/utils/apiClient";
import {HomeDTO} from "@/models/homeDTO";

export const fetchHomes = async (): Promise<HomeDTO[]> => {
    const response = await apiClient.get("/home");
    return Array.isArray(response.data) ? response.data : [];
};

export const createHome = async (name: string, address: string): Promise<void> => {
    const params = new URLSearchParams({ name, address });
    await apiClient.post(`/home?${params.toString()}`);
};

