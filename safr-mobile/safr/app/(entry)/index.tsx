import React from 'react';
import {View, Text, StyleSheet, Image, TouchableOpacity} from 'react-native';
import {Link} from 'expo-router';
import GradientBackground from "@/components/ui/gradient-background";

export default function HomeScreen() {
    return (
        <GradientBackground theme="dark">
            <View style={styles.container}>
                {/* Image at the top */}
                <Image
                    source={require('@/assets/images/header-images/security-shield.png')}
                    style={styles.image}
                />

                {/* Title */}
                <Text style={styles.title}>Welcome to Safr.</Text>

                {/* Login Button */}
                <TouchableOpacity style={styles.loginButton}>
                    <Link href="/login" style={styles.loginText}>
                        Login
                    </Link>
                </TouchableOpacity>

                {/* Register Link */}
                <Text style={styles.registerPrompt}>
                    New user? <Link href="/signup" style={styles.registerText}>Register</Link>
                </Text>
            </View>
        </GradientBackground>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingTop: 60,
    },
    image: {
        width: 200,
        height: 200,
        marginBottom: 20,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 30,
    },
    loginButton: {
        backgroundColor: '#1f2937',
        paddingVertical: 12,
        paddingHorizontal: 60,
        borderRadius: 25,
        marginBottom: 40,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 2},
        shadowOpacity: 0.8,
        shadowRadius: 4,
        elevation: 5,
    },
    loginText: {
        color: '#ffffff',
        fontSize: 18,
        fontWeight: '500',
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
