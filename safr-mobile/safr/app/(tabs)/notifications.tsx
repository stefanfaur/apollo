import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import GradientBg from '@/components/ui/gradient-bg';
import NotificationCard from '@/components/ui/notification-card';
import { Colors } from '@/constants/colors';

export default function NotificationsScreen() {
  const notifications = [
    {
      icon: require('@/assets/images/notifications/unauthorized.png'),
      title: 'Handle Tried',
      description: 'Unauthorized person tried handle.',
      time: '9:41 AM',
    },
    {
      icon: require('@/assets/images/notifications/unauthorized.png'),
      title: 'Door Opened',
      description: 'Unauthorized person forced door open.',
      time: '9:41 AM',
    },
    {
      icon: require('@/assets/images/notifications/authorized.png'),
      title: 'Owner Entry',
      description: 'Successful three-way authorization.',
      time: '9:41 AM',
    },
    {
      icon: require('@/assets/images/notifications/suspicious-activity.png'),
      title: 'Suspicious Activity',
      description: 'Unknown person lingering around door.',
      time: '9:41 AM',
    },
  ];

  return (
      <GradientBg theme="dark">
        <View style={styles.container}>
          {/* Header */}
          <Text style={styles.header}>Notifications</Text>

          {/* Notifications List */}
          <ScrollView contentContainerStyle={styles.scrollContainer}>
            {notifications.map((notification, index) => (
                <NotificationCard
                    key={index}
                    icon={notification.icon}
                    title={notification.title}
                    description={notification.description}
                    time={notification.time}
                />
            ))}
          </ScrollView>
        </View>
      </GradientBg>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 10,
    paddingTop: 60,
  },
  header: {
    fontSize: 20,
    fontWeight: 'bold',
    color: Colors.dark.text,
    marginBottom: 20,
    textAlign: 'center',
  },
  scrollContainer: {
    flexGrow: 1,
    paddingBottom: 20,
  },
});
