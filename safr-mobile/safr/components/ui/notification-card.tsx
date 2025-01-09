import React from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';
import { Colors } from '@/constants/colors';

type NotificationCardProps = {
    icon: any;
    title: string;
    description: string;
    time: string;
};

const NotificationCard: React.FC<NotificationCardProps> = ({ icon, title, description, time }) => {
    return (
        <View style={styles.card}>
            {/* Icon */}
            <Image source={icon} style={styles.icon} />

            {/* Content */}
            <View style={styles.content}>
                <Text style={styles.title}>{title}</Text>
                <Text style={styles.description}>{description}</Text>
            </View>

            {/* Time */}
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
});

export default NotificationCard;
