import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';
import { Link, router } from 'expo-router';
import AnimatedGradientBg from "@/components/ui/animated-gradient-bg";
import ApolloButton from "@/components/ui/apollo-button";
import globalStyles from "@/constants/global-styles";
import { getToken } from "@/utils/secureStore";

export default function EntryScreen() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkAuthToken = async () => {
            try {
                const token = await getToken();
                if (token) {
                    router.replace('/(tabs)/home');
                } else {
                    setLoading(false);
                }
            } catch (error) {
                console.error("Error fetching auth token:", error);
                setLoading(false);
            }
        };

        checkAuthToken();
    }, []);

    if (loading) {
        return (
            <AnimatedGradientBg>
                <View style={globalStyles.container}>
                    <Text style={styles.loadingText}>Loading...</Text>
                </View>
            </AnimatedGradientBg>
        );
    }

    return (
        <AnimatedGradientBg>
            <View style={globalStyles.container}>
                {/* Image at the top */}
                <Image
                    source={require('@/assets/images/header-images/security-shield.png')}
                    style={styles.image}
                />

                {/* Title */}
                <Text style={styles.title}>apollo.</Text>

                {/* Login Button */}
                <ApolloButton
                    title="Login"
                    onPress={() => router.push('/login')}
                    style={styles.loginButton}
                />

                {/* Register Link */}
                <Text style={styles.registerPrompt}>
                    New user? <Link href="/signup" style={styles.registerText}>Register</Link>
                </Text>
            </View>
        </AnimatedGradientBg>
    );
}

const styles = StyleSheet.create({
    image: {
        width: 200,
        height: 200,
        marginBottom: 20,
    },
    title: {
        fontSize: 36,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 30,
        textShadowColor: 'rgba(255, 255, 255, 0.7)',
        textShadowOffset: { width: 0, height: 0 },
        textShadowRadius: 5,
        letterSpacing: 1,
        shadowColor: '#fff',
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.5,
        shadowRadius: 8,
        elevation: 5,
    },
    loginButton: {
        paddingVertical: 12,
        paddingHorizontal: 60,
        borderRadius: 25,
        marginBottom: 40,
    },
    registerPrompt: {
        fontSize: 14,
        color: '#d1d5db',
    },
    registerText: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#ffffff',
    },
    loadingText: {
        fontSize: 18,
        color: '#ffffff',
    },
});
