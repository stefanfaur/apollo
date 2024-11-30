import React from 'react';
import { TextInput, StyleSheet, StyleProp, TextStyle, TextInputProps } from 'react-native';
import { Colors } from '@/constants/colors';

interface ApolloTextInputProps extends TextInputProps {
    theme?: 'light' | 'dark'; // Default to dark theme
    style?: StyleProp<TextStyle>;
}

export default function ApolloTextInput({
                                            theme = 'dark',
                                            style,
                                            ...props
                                        }: ApolloTextInputProps) {
    return (
        <TextInput
            style={[styles(theme).input, style]}
            placeholderTextColor={Colors[theme].icon}
            {...props}
        />
    );
}

const styles = (theme: 'light' | 'dark') =>
    StyleSheet.create({
        input: {
            backgroundColor: 'rgba(255, 255, 255, 0.1)',
            borderRadius: 20,
            paddingHorizontal: 15,
            paddingVertical: 12,
            fontSize: 16,
            color: Colors[theme].text,
            marginBottom: 20,
            borderWidth: 1,
            borderColor: Colors[theme].inputAccent,
            shadowColor: '#000',
            shadowOffset: { width: 0, height: 2 },
            shadowOpacity: 0.15,
            shadowRadius: 4,
            elevation: 2,
        },
    });
