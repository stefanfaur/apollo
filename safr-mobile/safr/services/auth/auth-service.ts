import axios from 'axios';
import { API_CONFIG } from '../../constants/config';

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
            console.log("Login request to: ", `${API_CONFIG.BASE_URL}/api/auth/login`)
            const response = await axios.post<LoginResponse>(`${API_CONFIG.BASE_URL}/api/auth/login`, data);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data || 'Login failed');
        }
    },

    register: async (data: RegisterRequest): Promise<RegisterResponse> => {
        try {
            console.log("Register request to: ", `${API_CONFIG.BASE_URL}/api/auth/register`)
            const response = await axios.post<RegisterResponse>(`${API_CONFIG.BASE_URL}/api/auth/register`, data);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data || 'Registration failed');
        }
    },

    oauth2Login: async (authorizationCode: string): Promise<LoginResponse> => {
        try {
            const response = await axios.post<LoginResponse>(
                `${API_CONFIG.BASE_URL}/api/auth/oauth2/login/google?token=${encodeURIComponent(authorizationCode)}`
            );
            return response.data;
        } catch (error: any) {
            const errorMessage = error.response?.data?.message || 'OAuth2 login failed';
            throw new Error(errorMessage);
        }
    },

};
