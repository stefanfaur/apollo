import React from 'react';
import { View, Text, StyleSheet, Image, ImageSourcePropType } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';

type DeviceCardProps = {
    imageUri: string | ImageSourcePropType; // Accept both local (require) and remote (URL) images(for later when we get CDN)
    title: string;
    description: string;
    status: 'ok' | 'warn' | 'error';
};

const DeviceCard: React.FC<DeviceCardProps> = ({ imageUri, title, description, status }) => {
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
        <View style={styles.card}>
            <Image source={getImageSource()} style={styles.image} />
            <View style={styles.info}>
                <Text style={styles.title}>{title}</Text>
                <Text style={styles.description}>{description}</Text>
            </View>
            <View style={styles.statusIcon}>{getStatusIcon()}</View>
        </View>
    );
};

const styles = StyleSheet.create({
    card: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 10,
        borderRadius: 8,
        backgroundColor: Colors.dark.background,
        marginBottom: 10,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
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
    statusIcon: {
        marginLeft: 10,
    },
});

export default DeviceCard;
