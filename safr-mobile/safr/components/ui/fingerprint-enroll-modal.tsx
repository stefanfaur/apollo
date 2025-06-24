import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { Colors } from '@/constants/colors';
import { deviceService } from '@/services/device-service';

interface FingerprintEnrollModalProps {
  visible: boolean;
  onClose: () => void;
  deviceId: string | null; // backend UUID of the device
}

type WizardStep = 'intro' | 'sending' | 'waiting' | 'success' | 'failure';

const FingerprintEnrollModal: React.FC<FingerprintEnrollModalProps> = ({ visible, onClose, deviceId }) => {
  const [step, setStep] = useState<WizardStep>('intro');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!visible) {
      // reset wizard once closed
      setTimeout(() => {
        setStep('intro');
        setErrorMessage(null);
      }, 300);
    }
  }, [visible]);

  const startEnrollment = async () => {
    if (!deviceId) return;
    try {
      setStep('sending');
      // This hard-coded ID should come from a proper user picker in the future
      const userFpId = 1;
      await deviceService.startFingerprintEnrollment(deviceId, userFpId);
      // short vibration feedback
      await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      setStep('waiting');

      // naive polling to see if enrollment finishes
      const poll = async (attempt: number) => {
        if (attempt > 20) {
          setErrorMessage('Enrollment timed out.');
          setStep('failure');
          return;
        }
        try {
          const status = await deviceService.getFingerprintEnrollmentStatus(deviceId);
          if (status === 'success') {
            setStep('success');
            await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
          } else if (status === 'failure') {
            setErrorMessage('Board reported failure.');
            setStep('failure');
            await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
          } else {
            // still waiting
            setTimeout(() => poll(attempt + 1), 1500);
          }
        } catch (e) {
          console.warn('Polling error', e);
          setTimeout(() => poll(attempt + 1), 2000);
        }
      };
      poll(0);
    } catch (e) {
      console.error('Enrollment start error', e);
      setErrorMessage('Failed to start enrollment.');
      setStep('failure');
      await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
    }
  };

  const renderContent = () => {
    switch (step) {
      case 'intro':
        return (
          <>
            <Ionicons name="finger-print" size={64} color={Colors.dark.primary} />
            <Text style={styles.title}>Enroll Fingerprint</Text>
            <Text style={styles.subtitle}>Place the finger you wish to enroll on the sensor when prompted. You will be asked to lift and place it multiple times.</Text>
            <TouchableOpacity style={styles.primaryButton} onPress={startEnrollment}>
              <Text style={styles.primaryButtonText}>Start</Text>
            </TouchableOpacity>
          </>
        );
      case 'sending':
        return (
          <>
            <ActivityIndicator size="large" color={Colors.dark.primary} />
            <Text style={styles.subtitle}>Sending enrollment request...</Text>
          </>
        );
      case 'waiting':
        return (
          <>
            <Ionicons name="finger-print" size={64} color={Colors.dark.primary} />
            <Text style={styles.title}>Follow the sound</Text>
            <Text style={styles.subtitle}>Place your finger after the sound, then take it off and replace after next sound.</Text>
            <ActivityIndicator style={{ marginTop: 20 }} size="small" color={Colors.dark.primary} />
          </>
        );
      case 'success':
        return (
          <>
            <Ionicons name="checkmark-circle" size={64} color="green" />
            <Text style={styles.title}>Enrollment Successful</Text>
            <TouchableOpacity style={styles.primaryButton} onPress={onClose}>
              <Text style={styles.primaryButtonText}>Done</Text>
            </TouchableOpacity>
          </>
        );
      case 'failure':
        return (
          <>
            <Ionicons name="close-circle" size={64} color="red" />
            <Text style={styles.title}>Enrollment Failed</Text>
            {errorMessage && <Text style={styles.subtitle}>{errorMessage}</Text>}
            <TouchableOpacity style={styles.primaryButton} onPress={startEnrollment}>
              <Text style={styles.primaryButtonText}>Try Again</Text>
            </TouchableOpacity>
          </>
        );
    }
  };

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.container}>{renderContent()}</View>
        <TouchableOpacity style={styles.closeIcon} onPress={onClose}>
          <Ionicons name="close" size={24} color={Colors.dark.text} />
        </TouchableOpacity>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.7)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  container: {
    width: '85%',
    minHeight: 300,
    backgroundColor: Colors.dark.background,
    borderRadius: 12,
    padding: 24,
    alignItems: 'center',
  },
  closeIcon: {
    position: 'absolute',
    top: 40,
    right: 30,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: Colors.dark.text,
    marginTop: 16,
    marginBottom: 8,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 14,
    color: Colors.dark.icon,
    textAlign: 'center',
    marginHorizontal: 10,
    marginTop: 8,
  },
  primaryButton: {
    marginTop: 24,
    backgroundColor: Colors.dark.primary,
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  primaryButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
});

export default FingerprintEnrollModal; 