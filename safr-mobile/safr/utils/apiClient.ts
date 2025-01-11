import axios from 'axios';
import { getToken } from './secureStore';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api', // TODO: extract this
});

// include jwt in all requests
apiClient.interceptors.request.use(
    async (config) => {
        const token = await getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        // TODO: properly handle errors
        return Promise.reject(error);
    }
);

export default apiClient;
