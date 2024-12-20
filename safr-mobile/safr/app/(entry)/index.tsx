import React from 'react';
import {View, Text, StyleSheet, Image, TouchableOpacity} from 'react-native';
import {Link, router} from 'expo-router';
import AnimatedGradientBg from "@/components/ui/animated-gradient-bg";
import ApolloButton from "@/components/ui/apollo-button";
import globalStyles from "@/constants/global-styles";

export default function HomeScreen() {
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
                    theme="dark"
                    style={styles.loginButton}
                />

                {/* Register Link */}
                <Text style={styles.registerPrompt}>
                    New user? <Link href="/signup" style={styles.registerText}>Register</Link>
                </Text>

                <Link href="/(tabs)/home" style={styles.registerText}>home</Link>
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
        // Layered text shadow for glow effect
        textShadowColor: 'rgba(255, 255, 255, 0.7)',
        textShadowOffset: { width: 0, height: 0 },
        textShadowRadius: 5,
        // Additional styling for enhanced visibility
        letterSpacing: 1,
        // For iOS
        shadowColor: '#fff',
        shadowOffset: { width: 0, height: 0 },
        shadowOpacity: 0.5,
        shadowRadius: 8,
        // For Android
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
});
