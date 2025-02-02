import React, {useCallback, useState} from 'react';
import {ActivityIndicator, Image, Modal, ScrollView, StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import {useFocusEffect} from '@react-navigation/native';
import GradientBg from '@/components/ui/gradient-bg';
import NotificationCard from '@/components/ui/notification-card';
import {Colors} from '@/constants/colors';
import NotificationService from '@/services/notification-service';
import {NotificationDTO} from '@/models/notificationDTO';
import {ResizeMode, Video} from "expo-av";

export default function NotificationsScreen() {
  const [notifications, setNotifications] = useState<NotificationDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [polling, setPolling] = useState(false);
  const [pollingInterval, setPollingInterval] = useState<NodeJS.Timeout | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMedia, setModalMedia] = useState<string | null>(null);
  const [isVideo, setIsVideo] = useState(false);

  const fetchNotifications = async () => {
    try {
      const fetchedNotifications = await NotificationService.getNotificationsForUser();
      setNotifications(fetchedNotifications);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (isoTime: string) => {
    const date = new Date(isoTime);
    if (isNaN(date.getTime())) {
      return 'Invalid date';
    }
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const handleCardPress = (mediaUrl: string | undefined) => {
    if (mediaUrl) {
      setIsVideo(mediaUrl.endsWith('.mp4') || mediaUrl.endsWith('.mov'));
      setModalMedia(mediaUrl);
      setModalVisible(true);
    }
  };

  useFocusEffect(
      useCallback(() => {
        setLoading(true);
        fetchNotifications();

        const intervalId = setInterval(() => {
          if (!polling) {
            setPolling(true);
            fetchNotifications().finally(() => setPolling(false));
          }
        }, 2000);
        setPollingInterval(intervalId);

        return () => {
          clearInterval(intervalId);
          setPollingInterval(null);
        };
      }, [])
  );

  if (loading) {
    return (
        <GradientBg theme="dark">
          <View style={styles.container}>
            <ActivityIndicator size="large" color={Colors.dark.text} />
          </View>
        </GradientBg>
    );
  }

  return (
      <GradientBg theme="dark">
        <View style={styles.container}>
          <Text style={styles.header}>Notifications</Text>
          <ScrollView contentContainerStyle={styles.scrollContainer}>
            {notifications.length > 0 ? (
                notifications.map((notification) => (
                    <TouchableOpacity
                        key={notification.uuid}
                        onPress={() => handleCardPress(notification.mediaUrl)}
                    >
                      <NotificationCard
                          title={notification.title}
                          description={notification.message}
                          time={formatTime(notification.createdAt)}
                      />
                    </TouchableOpacity>
                ))
            ) : (
                <Text style={styles.noNotificationsText}>No notifications available.</Text>
            )}
          </ScrollView>

          {modalMedia && (
              <Modal visible={modalVisible} transparent animationType="fade">
                <View style={styles.modalContainer}>
                  <TouchableOpacity
                      style={styles.modalBackground}
                      onPress={() => setModalVisible(false)}
                  />
                  {isVideo ? (
                      <Video
                          source={{ uri: modalMedia }}
                          style={styles.fullMedia}
                          useNativeControls
                          resizeMode={ResizeMode.CONTAIN}
                          shouldPlay
                      />
                  ) : (
                      <Image source={{ uri: modalMedia }} style={styles.fullMedia} />
                  )}
                </View>
              </Modal>
          )}
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
  noNotificationsText: {
    fontSize: 16,
    color: Colors.dark.text,
    textAlign: 'center',
    marginTop: 20,
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
  fullMedia: {
    width: '90%',
    height: '70%',
    resizeMode: 'contain',
    borderRadius: 10,
  },
});
