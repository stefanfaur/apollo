import React, { useState, useEffect } from 'react';
import {View, Text, Image, StyleSheet, Alert, TouchableOpacity} from 'react-native';
import GradientBg from '@/components/ui/gradient-bg';
import ApolloTextInput from '@/components/ui/apollo-text-input';
import ApolloButton from '@/components/ui/apollo-button';
import globalStyles from '@/constants/global-styles';
import * as AuthSession from 'expo-auth-session';
import { AuthService, LoginRequest } from '@/services/auth/auth-service';
import {saveToken} from "@/utils/secureStore";
import {useNavigation} from "expo-router";

const discovery = {
    authorizationEndpoint: 'https://accounts.google.com/o/oauth2/auth',
    tokenEndpoint: 'https://oauth2.googleapis.com/token',
};

export default function LoginScreen(): JSX.Element {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);

    const navigation = useNavigation();

    const redirectUri = AuthSession.makeRedirectUri({
        scheme: 'com.anonymous.safr',
    });
    console.log('Redirect URI:', redirectUri);

    const [request, response, promptAsync] = AuthSession.useAuthRequest(
        {
            clientId: '279681675256-s1ve5t88tc9d0gu86uvbqhjvuhq5tn24.apps.googleusercontent.com', // TODO: extract this to a config file
            redirectUri: redirectUri,
            scopes: ['openid', 'profile', 'email'],
            responseType: 'code',
        },
        discovery
    );

    useEffect(() => {
        if (response?.type === 'success' && response.params.code) {
            handleOAuth2Login(response.params.code);
        }
    }, [response]);

    const handleLogin = async () => {
        setLoading(true);
        try {
            const requestData: LoginRequest = { username, password };
            const data = await AuthService.login(requestData);
            await saveToken(data.token);
            Alert.alert('Login Successful', `Welcome back, ${username}!`);
            navigation.reset({
                index: 0,
                // @ts-ignore TODO: fix this when properly defining the routes
                routes: [{ name: '(tabs)' }],
            });
        } catch (error: any) {
            Alert.alert('Login Failed', error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleOAuth2Login = async (code: string) => {
        try {
            const tokenResponse = await AuthSession.exchangeCodeAsync(
                {
                    code,
                    clientId: '279681675256-s1ve5t88tc9d0gu86uvbqhjvuhq5tn24.apps.googleusercontent.com',
                    redirectUri: redirectUri,
                    extraParams: {
                        code_verifier: request?.codeVerifier || '',
                    },
                },
                discovery
            );

            if (tokenResponse.accessToken) {
                let idToken = tokenResponse.idToken ? tokenResponse.idToken : '';
                let apolloJwt = await AuthService.oauth2Login(idToken);
                await saveToken(apolloJwt.token);
                Alert.alert('Google login Successful', `Welcome back!`);
                navigation.reset({
                    index: 0,
                    // @ts-ignore TODO: fix this when properly defining the routes
                    routes: [{ name: '(tabs)' }],
                });
            } else {
                Alert.alert('OAuth2 Login Failed', 'No access token received');
            }
        } catch (error: any) {
            Alert.alert('OAuth2 Login Failed', error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <GradientBg theme="dark">
            <View style={globalStyles.container}>
                <Image
                    source={require('@/assets/images/header-images/login.png')}
                    style={styles.image}
                />
                <Text style={styles.title}>Login</Text>
                <View style={styles.inputContainer}>
                    <ApolloTextInput
                        placeholder="Username"
                        value={username}
                        onChangeText={setUsername}
                        theme="dark"
                    />
                    <ApolloTextInput
                        placeholder="Password"
                        value={password}
                        onChangeText={setPassword}
                        secureTextEntry
                        theme="dark"
                    />
                </View>
                <ApolloButton
                    title={loading ? 'Loading...' : 'Submit'}
                    onPress={handleLogin}
                    theme="dark"
                    disabled={loading}
                />

                <TouchableOpacity
                    onPress={() => promptAsync()}
                    disabled={!request || loading}
                    style={{ marginTop: 16 }}
                >
                    <Image
                        source={require('@/assets/images/google-logo.png')}
                        style={{ width: 48, height: 48 }}
                    />
                </TouchableOpacity>

            </View>
        </GradientBg>
    );
}

const styles = StyleSheet.create({
    image: {
        width: 200,
        height: 200,
        marginBottom: 30,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#fff',
        marginBottom: 40,
    },
    inputContainer: {
        width: '80%',
    },

});
