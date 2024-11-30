import React from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Colors } from '@/constants/colors';

interface GradientBackgroundProps {
    children: React.ReactNode;
    style?: ViewStyle;
    theme?: 'light' | 'dark';
}

export default function GradientBackground({
                                               children,
                                               style,
                                               theme = 'dark',
                                           }: GradientBackgroundProps) {
    // as const is used to infer the tuple type, otherwise LinearGradient will throw an error
    const gradientColors = [Colors[theme].gradientStart, Colors[theme].gradientEnd] as const;

    return (
        <LinearGradient colors={gradientColors} style={[styles.gradient, style]}>
            {children}
        </LinearGradient>
    );
}

const styles = StyleSheet.create({
    gradient: {
        flex: 1,
    },
});
