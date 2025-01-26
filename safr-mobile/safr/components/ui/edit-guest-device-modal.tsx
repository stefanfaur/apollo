import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, Switch } from 'react-native';
import CustomModal from './custom-modal';
import ApolloButton from './apollo-button';
import { Colors } from '@/constants/colors';
import apiClient from "@/utils/apiClient";
import {deviceService} from "@/services/device-service";

interface Device {
  uuid: string;
  name: string;
}

interface GuestDevice {
  deviceId: string;
  accessRights: string[];
}

interface EditGuestDeviceModalProps {
  visible: boolean;
  onClose: () => void;
  onSubmit: (deviceRights: { deviceId: string, rights: Set<string> }[]) => Promise<void>;
  homeId: string;
  guestId: string;
  currentDevices: GuestDevice[];
}

const DEVICE_RIGHTS = [
  'VIEW_IMAGES',
  'VIEW_LIVE_STREAM',
  'VIEW_NOTIFICATIONS',
  'UNLOCK_DOOR',
  'VIEW_DEVICE_INFO',
  'VIEW_DEVICE_SETTINGS',
];

const EditGuestDeviceModal: React.FC<EditGuestDeviceModalProps> = ({
  visible,
  onClose,
  onSubmit,
  homeId,
  guestId,
  currentDevices,
}) => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedRights, setSelectedRights] = useState<Map<string, Set<string>>>(new Map());
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchDevices = async () => {
    try {
      const data = await deviceService.getHomeDevices(homeId);
      setDevices(data);
    } catch (err) {
      setError('Failed to load devices');
      console.error(err);
    }
  };

  useEffect(() => {
    if (visible) {
      fetchDevices();
      // Initialize selected rights from current devices
      const initialRights = new Map();
      currentDevices.forEach(device => {
        initialRights.set(device.deviceId, new Set(device.accessRights));
      });
      setSelectedRights(initialRights);
    }
  }, [visible, currentDevices]);

  const toggleRight = (deviceId: string, right: string) => {
    setSelectedRights(prev => {
      const newRights = new Map(prev);
      const deviceRights = new Set(prev.get(deviceId) || []);

      if (deviceRights.has(right)) {
        deviceRights.delete(right);
      } else {
        deviceRights.add(right);
      }

      if (deviceRights.size > 0) {
        newRights.set(deviceId, deviceRights);
      } else {
        newRights.delete(deviceId);
      }

      return newRights;
    });
  };

  const handleSubmit = async () => {
    setError(null);
    setIsLoading(true);

    try {
      const deviceRights = Array.from(selectedRights.entries()).map(
        ([deviceId, rights]) => ({
          deviceId,
          rights,
        })
      );

      await onSubmit(deviceRights);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update permissions');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <CustomModal visible={visible} title="Edit Device Permissions" onClose={onClose}>
      <ScrollView style={styles.container}>
        {error && <Text style={styles.errorText}>{error}</Text>}

        {devices.map(device => (
          <View key={device.uuid} style={styles.deviceSection}>
            <Text style={styles.deviceName}>{device.name}</Text>
            {DEVICE_RIGHTS.map(right => {
              const isEnabled = selectedRights.get(device.uuid)?.has(right) || false;
              return (
                <View key={right} style={styles.rightItem}>
                  <Text style={styles.rightLabel}>
                    {right.split('_').map(word =>
                      word.charAt(0) + word.slice(1).toLowerCase()
                    ).join(' ')}
                  </Text>
                  <Switch
                    value={isEnabled}
                    onValueChange={() => toggleRight(device.uuid, right)}
                    trackColor={{
                      false: Colors.dark.buttonBackground,
                      true: Colors.dark.primary
                    }}
                  />
                </View>
              );
            })}
          </View>
        ))}

        <ApolloButton
          title="Save Changes"
          onPress={handleSubmit}
          disabled={isLoading}
          style={styles.button}
        />
      </ScrollView>
    </CustomModal>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 16,
  },
  deviceSection: {
    marginBottom: 20,
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 8,
    padding: 12,
  },
  deviceName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: Colors.dark.text,
    marginBottom: 12,
  },
  rightItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  rightLabel: {
    color: Colors.dark.text,
    fontSize: 14,
  },
  button: {
    marginTop: 20,
    marginBottom: 40,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
    marginVertical: 8,
    textAlign: 'center',
  },
});

export default EditGuestDeviceModal;
