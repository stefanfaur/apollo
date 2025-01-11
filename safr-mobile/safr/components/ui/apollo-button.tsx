import React from 'react';
import {
    TouchableOpacity,
    Text,
    StyleSheet,
    StyleProp,
    ViewStyle,
    Pressable
} from 'react-native';
import { Colors } from '@/constants/colors';

interface ApolloButtonProps {
    title: string;
    onPress: () => void;
    style?: StyleProp<ViewStyle>;
    theme?: 'light' | 'dark';
    disabled?: boolean;
}

export default function ApolloButton({
                                         title,
                                         onPress,
                                         style,
                                         theme = 'dark',
                                         disabled = false
                                     }: ApolloButtonProps) {
    return (
        <Pressable
            style={({ pressed }) => [
                styles(theme).button,
                style,
                pressed && styles(theme).pressed,
                disabled && styles(theme).disabled
            ]}
            onPress={onPress}
            disabled={disabled}
            android_ripple={{ color: 'rgba(0,0,0,0.1)' }}
            accessibilityRole="button"
            accessibilityLabel={title}
        >
            <Text style={[
                styles(theme).buttonText,
                disabled && styles(theme).disabledText
            ]}>
                {title}
            </Text>
        </Pressable>
    );
}

const styles = (theme: 'light' | 'dark') =>
    StyleSheet.create({
        button: {
            backgroundColor: Colors[theme].buttonBackground,
            paddingVertical: 16,
            paddingHorizontal: 24,
            borderRadius: 25,
            alignItems: 'center',
            justifyContent: 'center',
            shadowColor: '#000',
            shadowOffset: { width: 0, height: 2 },
            shadowOpacity: 0.8,
            shadowRadius: 4,
            elevation: 5,
        },
        buttonText: {
            color: Colors[theme].buttonText,
            fontSize: 16,
            fontWeight: 'bold',
        },
        pressed: {
            opacity: 0.8,
            transform: [{ scale: 0.98 }],
        },
        disabled: {
            opacity: 0.5,
            backgroundColor: '#ccc',
        },
        disabledText: {
            color: '#666',
        }
    });