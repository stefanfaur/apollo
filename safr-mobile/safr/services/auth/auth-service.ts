import axios, { AxiosError } from 'axios';
import { API_CONFIG } from '../../constants/config';
import apiClient from '../../utils/apiClient';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    token: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface RegisterResponse {
    message: string;
}

export const AuthService = {
    login: async (data: LoginRequest): Promise<LoginResponse> => {
        try {
            console.log("Login request to: ", `${API_CONFIG.API_URL}/auth/login`);
            const response = await apiClient.post<LoginResponse>(`/auth/login`, data);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data || 'Login failed');
        }
    },

    register: async (data: RegisterRequest): Promise<RegisterResponse> => {
        try {
            console.log("Register request to: ", `${API_CONFIG.API_URL}/auth/register`);
            const response = await apiClient.post<RegisterResponse>(`/auth/register`, data);
            console.log("Register response: ", response.data)
            return response.data;
        } catch (error: unknown) {
            console.log("Register error: ", error);
            const responseData = (error as AxiosError).response?.data;
            const errorMsg = typeof responseData === 'string' ? responseData : JSON.stringify(responseData ?? 'Registration failed');
            throw new Error(errorMsg);
        }
    },

    oauth2Login: async (authorizationCode: string): Promise<LoginResponse> => {
        try {
            const response = await axios.post<LoginResponse>(
                `${API_CONFIG.API_URL}/auth/oauth2/login/google?token=${encodeURIComponent(authorizationCode)}`
            );
            return response.data;
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || 'OAuth2 login failed';
            throw new Error(errorMessage);
        }
    },

};
