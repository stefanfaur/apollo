import React from 'react';
import { View, Text, StyleSheet, Switch } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';
import { Device } from '@/services/device-service';
import { DEVICE_RIGHTS } from '@/constants/device-rights';

interface DevicePermissionsCardProps {
  device: Device;
  selectedRights: Set<string>;
  onToggleRight: (right: string) => void;
}

const DevicePermissionsCard: React.FC<DevicePermissionsCardProps> = ({
  device,
  selectedRights,
  onToggleRight,
}) => {
  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Ionicons 
          name={device.deviceType === 'DOOR_LOCK' ? 'lock-closed' : 'videocam'} 
          size={20} 
          color={Colors.dark.text} 
          style={styles.deviceIcon}
        />
        <Text style={styles.deviceName}>{device.name}</Text>
      </View>
      
      <View style={styles.rightsContainer}>
        {DEVICE_RIGHTS.map(right => {
          const isEnabled = selectedRights.has(right.id);
          return (
            <View key={right.id} style={styles.rightItem}>
              <View style={styles.rightItemLeft}>
                <View style={[
                  styles.rightIconContainer,
                  isEnabled && styles.rightIconContainerEnabled
                ]}>
                  <Ionicons 
                    name={right.icon} 
                    size={16} 
                    color={isEnabled ? Colors.dark.primary : Colors.dark.text}
                  />
                </View>
                <Text style={[
                  styles.rightLabel,
                  isEnabled && styles.rightLabelEnabled
                ]}>
                  {right.label}
                </Text>
              </View>
              <Switch
                value={isEnabled}
                onValueChange={() => onToggleRight(right.id)}
                trackColor={{ 
                  false: Colors.dark.buttonBackground,
                  true: Colors.dark.primary 
                }}
                style={styles.switch}
              />
            </View>
          );
        })}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: Colors.dark.primary + '20',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  deviceIcon: {
    marginRight: 8,
    backgroundColor: Colors.dark.primary + '20',
    padding: 6,
    borderRadius: 8,
  },
  deviceName: {
    fontSize: 14,
    fontWeight: 'bold',
    color: Colors.dark.text,
    letterSpacing: 0.5,
  },
  rightsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  rightItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: Colors.dark.background,
    borderRadius: 8,
    minWidth: '45%',
    flex: 1,
  },
  rightItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  rightIconContainer: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: Colors.dark.buttonBackground,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
    borderWidth: 1,
    borderColor: Colors.dark.buttonBackground,
  },
  rightIconContainerEnabled: {
    borderColor: Colors.dark.primary,
    backgroundColor: Colors.dark.primary + '20',
  },
  rightLabel: {
    color: Colors.dark.text,
    fontSize: 12,
    opacity: 0.8,
    flex: 1,
  },
  rightLabelEnabled: {
    color: Colors.dark.primary,
    fontWeight: 'bold',
    opacity: 1,
  },
  switch: {
    transform: [{ scaleX: 0.8 }, { scaleY: 0.8 }],
    marginLeft: 8,
  },
});

export default DevicePermissionsCard;
