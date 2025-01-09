import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';

type DropdownListItemProps = {
    title: string;
    children: React.ReactNode;
    settingsAction: () => void; // settings button action
    startsOpen?: boolean;
};

const DropdownListItem: React.FC<DropdownListItemProps> = ({ title, children, settingsAction, startsOpen }) => {
    const [isOpen, setIsOpen] = useState( startsOpen ?? false );
    const rotation = useState(new Animated.Value(0))[0]; // for arrow rotation

    const toggleDropdown = () => {
        Animated.timing(rotation, {
            toValue: isOpen ? 0 : 1,
            duration: 300,
            useNativeDriver: true,
        }).start();
        setIsOpen((prev) => !prev);
    };

    const rotateStyle = {
        transform: [
            {
                rotate: rotation.interpolate({
                    inputRange: [0, 1],
                    outputRange: ['0deg', '180deg'],
                }),
            },
        ],
    };

    return (
        <View style={styles.dropdownContainer}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={styles.title}>{title}</Text>
                <View style={styles.iconGroup}>
                    {/* Settings Button */}
                    <TouchableOpacity onPress={settingsAction}>
                        <Ionicons name="settings-outline" size={20} color={Colors.dark.icon} />
                    </TouchableOpacity>
                    {/* Arrow Icon */}
                    <TouchableOpacity onPress={toggleDropdown}>
                        <Animated.View style={rotateStyle}>
                            <Ionicons name="chevron-down" size={20} color={Colors.dark.icon} />
                        </Animated.View>
                    </TouchableOpacity>
                </View>
            </View>

            {/* Content */}
            {isOpen && <View style={styles.content}>{children}</View>}
        </View>
    );
};

const styles = StyleSheet.create({
    dropdownContainer: {
        marginBottom: 15,
        borderRadius: 8,
        backgroundColor: Colors.dark.background,
        padding: 10,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 5,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    title: {
        fontSize: 18,
        fontWeight: '600',
        color: Colors.dark.text,
    },
    iconGroup: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 10, // spacing between icons
    },
    content: {
        marginTop: 10,
    },
});

export default DropdownListItem;
