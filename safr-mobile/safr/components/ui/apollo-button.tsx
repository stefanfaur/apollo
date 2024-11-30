import React from 'react';
import { TouchableOpacity, Text, StyleSheet, StyleProp, ViewStyle } from 'react-native';
import { Colors } from '@/constants/colors';

interface ApolloButtonProps {
    title: string;
    onPress: () => void;
    style?: StyleProp<ViewStyle>;
    theme?: 'light' | 'dark';
}

export default function ApolloButton({
                                         title,
                                         onPress,
                                         style,
                                         theme = 'dark',
                                     }: ApolloButtonProps) {
    return (
        <TouchableOpacity
            style={[styles(theme).button, style]}
            onPress={onPress}
            activeOpacity={0.7}
        >
            <Text style={styles(theme).buttonText}>{title}</Text>
        </TouchableOpacity>
    );
}

const styles = (theme: 'light' | 'dark') =>
    StyleSheet.create({
        button: {
            backgroundColor: Colors[theme].buttonBackground,
            paddingVertical: 12,
            paddingHorizontal: 20,
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
    });
