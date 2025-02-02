import React, { useState } from 'react';
import { View, Text, StyleSheet, Image, Modal, TouchableOpacity } from 'react-native';
import { Colors } from '@/constants/colors';
import { Ionicons } from '@expo/vector-icons';

type NotificationCardProps = {
    title: string;
    description: string;
    time: string;
    imageUrl?: string;
};

const NotificationCard: React.FC<NotificationCardProps> = ({title, description, time, imageUrl }) => {
    const [modalVisible, setModalVisible] = useState(false);
    return (
        <View style={styles.card}>
            <Ionicons name={'notifications'} size={40} color={Colors.dark.icon} style={styles.icon} />
            <View style={styles.content}>
                <Text style={styles.title}>{title}</Text>
                <Text style={styles.description}>{description}</Text>
            </View>
            <Text style={styles.time}>{time}</Text>
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
        shadowOpacity: 0.1,
        shadowRadius: 5,
    },
    icon: {
        width: 40,
        height: 40,
        marginRight: 10,
        borderRadius: 20,
    },
    content: {
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
        marginTop: 2,
    },
    time: {
        fontSize: 12,
        color: Colors.dark.icon,
        alignSelf: 'flex-start',
    },
    modalContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
    },
    modalBackground: {
        position: 'absolute',
        width: '100%',
        height: '100%',
    },
    fullImage: {
        width: '90%',
        height: '70%',
        resizeMode: 'contain',
        borderRadius: 10,
    },
});

export default NotificationCard;
