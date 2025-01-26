import apiClient from '../utils/apiClient';
import {deleteToken} from "@/utils/secureStore";
import {UserDTO} from "@/models/userDTO";


export const userService = {
  searchUsers: async (query: string, page: number = 0, size: number = 10): Promise<UserDTO[]> => {
    try {
      const response = await apiClient.get('/users/search', {
        params: {
          query,
          page,
          size
        }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to search users:', error);
      throw error;
    }
  },

  fetchUserInfo: async (): Promise<UserDTO> => {
    const response = await apiClient.get("/users/me");
    console.log(response.data);
    return response.data;
  },

  logoutUser: async (): Promise<void> => {
    await deleteToken();
  }
};
