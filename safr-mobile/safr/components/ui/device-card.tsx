import React from 'react';
import { View, Text, StyleSheet, Image, ImageSourcePropType, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';

type DeviceCardProps = {
    imageUri: string | ImageSourcePropType; // Accept both local (require) and remote (URL) images (for later when we get CDN)
    title: string;
    description: string;
    status: 'ok' | 'warn' | 'error';
    /**
     * Triggered when the user taps anywhere on the card (e.g. navigate to device details).
     */
    onPress?: () => void;
    /**
     * Triggered when the user taps the fingerprint button to start fingerprint enrollment.
     */
    onEnrollPress?: () => void;
};

const DeviceCard: React.FC<DeviceCardProps> = ({ imageUri, title, description, status, onPress, onEnrollPress }) => {
    const getStatusIcon = () => {
        switch (status) {
            case 'ok':
                return <Ionicons name="checkmark-circle" size={24} color="green" />;
            case 'warn':
                return <Ionicons name="alert-circle" size={24} color="orange" />;
            case 'error':
                return <Ionicons name="close-circle" size={24} color="red" />;
            default:
                return null;
        }
    };

    const getImageSource = () => {
        if (typeof imageUri === 'string') {
            return { uri: imageUri }; // URL
        }
        return imageUri; // `require`
    };

    return (
        <TouchableOpacity activeOpacity={0.8} onPress={onPress} style={styles.card}>
            {/* Device thumbnail */}
            <Image source={getImageSource()} style={styles.image} />

            {/* Main info (title & description) */}
            <View style={styles.info}>
                <Text style={styles.title}>{title}</Text>
                <Text style={styles.description}>{description}</Text>
            </View>

            {/* Fingerprint enrollment trigger */}
            <TouchableOpacity
                accessibilityLabel="Start fingerprint enrollment"
                onPress={onEnrollPress}
                style={styles.enrollButton}
                hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            >
                <Ionicons name="finger-print" size={22} color={Colors.dark.primary} />
            </TouchableOpacity>

            {/* Device status icon */}
            <View style={styles.statusIcon}>{getStatusIcon()}</View>
        </TouchableOpacity>
    );
};

const styles = StyleSheet.create({
    card: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 12,
        borderRadius: 10,
        backgroundColor: Colors.dark.gradientStart, // slightly lighter than the page background for contrast
        marginBottom: 12,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 5,
    },
    image: {
        width: 50,
        height: 50,
        borderRadius: 5,
        marginRight: 10,
    },
    info: {
        flex: 1,
    },
    title: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Colors.dark.text,
    },
    description: {
        fontSize: 14,
        color: Colors.dark.icon,
    },
    enrollButton: {
        marginHorizontal: 6,
    },
    statusIcon: {
        marginLeft: 6,
    },
});

export default DeviceCard;
