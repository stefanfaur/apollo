import apiClient from "@/utils/apiClient";
import { deleteToken } from "@/utils/secureStore";
import {UserDTO} from "@/models/userDTO";

export const fetchUserInfo = async (): Promise<UserDTO> => {
    const response = await apiClient.get("/user/me");
    return response.data;
};

export const logoutUser = async (): Promise<void> => {
    await deleteToken();
};
