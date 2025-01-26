import * as SecureStore from 'expo-secure-store';

const SecureStoreKeys = {
    AUTH_TOKEN: 'authToken',
};

export const saveToken = async (token: string): Promise<void> => {
    console.log('saving token', token);
    await SecureStore.setItemAsync(SecureStoreKeys.AUTH_TOKEN, token);
};

export const getToken = async (): Promise<string | null> => {
    return await SecureStore.getItemAsync(SecureStoreKeys.AUTH_TOKEN);
};

export const deleteToken = async (): Promise<void> => {
    await SecureStore.deleteItemAsync(SecureStoreKeys.AUTH_TOKEN);
};
