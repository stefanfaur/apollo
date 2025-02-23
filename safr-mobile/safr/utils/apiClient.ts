import axios from 'axios';
import { getToken, deleteToken } from './secureStore';
import { router } from 'expo-router';
import { Alert } from 'react-native';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api', // TODO: extract this into an environment variable
});

// include JWT in all requests
apiClient.interceptors.request.use(
    async (config) => {
        const token = await getToken();
        console.log('sent request ' + config.method!.toUpperCase() + ' to ' + config.url);
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        console.error("Request error:", error);
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            console.warn("Unauthorized! Redirecting to login.");

            // clear stale token
            await deleteToken();

            // redirect to entry
            router.replace('/(entry)');
        } else if (error.response?.status === 400) {
            console.warn("Bad Request! Showing alert.");

            // display the error message in an alert
            console.log(error.response)
            const message = error.response.data || "Bad Request";
            Alert.alert("Error", message);
        }
        return Promise.reject(error);
    }
);

export default apiClient;
