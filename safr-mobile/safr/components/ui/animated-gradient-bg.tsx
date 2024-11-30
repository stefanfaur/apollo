import React, { useEffect } from 'react';
import { Animated, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';

// animated version of LinearGradient
const AnimatedLinearGradient = Animated.createAnimatedComponent(LinearGradient);

export default function AnimatedGradientBg({ children }: { children: React.ReactNode }) {
    const animatedValue = new Animated.Value(0);

    useEffect(() => {
        Animated.loop(
            Animated.timing(animatedValue, {
                toValue: 1,
                duration: 8000,
                useNativeDriver: false, // colors need non-transform animations
            })
        ).start();
    }, []);

    // interpolate to transition smoothly
    const color1 = animatedValue.interpolate({
        inputRange: [0, 0.5, 1],
        outputRange: ['#1a2a6c', '#b21f1f', '#1a2a6c'], // cycle back to start color
    });

    const color2 = animatedValue.interpolate({
        inputRange: [0, 0.5, 1],
        outputRange: ['#b25c1f', '#1a2a6c', '#b25c1f'], // cycle back to start color
    });

    return (
        <Animated.View style={{ flex: 1 }}>
            <AnimatedLinearGradient
                colors={[color1, color2]}
                style={StyleSheet.absoluteFill}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 1 }}
            />
            {children}
        </Animated.View>
    );
}
