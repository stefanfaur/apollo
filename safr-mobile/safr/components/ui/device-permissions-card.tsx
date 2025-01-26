import React from 'react';
import { View, Text, StyleSheet, ScrollView, Switch } from 'react-native';
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
          size={24} 
          color={Colors.dark.text} 
          style={styles.deviceIcon}
        />
        <Text style={styles.deviceName}>{device.name}</Text>
      </View>
      
      <ScrollView 
        horizontal 
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.rightsContainer}
      >
        {DEVICE_RIGHTS.map(right => {
          const isEnabled = selectedRights.has(right.id);
          return (
            <View key={right.id} style={styles.rightItem}>
              <View style={styles.rightIconContainer}>
                <Ionicons 
                  name={right.icon} 
                  size={24} 
                  color={isEnabled ? Colors.dark.primary : Colors.dark.text}
                />
              </View>
              <Text style={[
                styles.rightLabel,
                isEnabled && styles.rightLabelEnabled
              ]}>
                {right.label}
              </Text>
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
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 16,
    padding: 20,
    marginBottom: 20,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 4.65,
    elevation: 8,
    borderWidth: 1,
    borderColor: Colors.dark.primary + '20',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
    backgroundColor: Colors.dark.background + '40',
    padding: 12,
    borderRadius: 12,
  },
  deviceIcon: {
    marginRight: 12,
    backgroundColor: Colors.dark.primary + '20',
    padding: 8,
    borderRadius: 10,
  },
  deviceName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.dark.text,
    letterSpacing: 0.5,
  },
  rightsContainer: {
    paddingRight: 16,
  },
  rightItem: {
    alignItems: 'center',
    marginRight: 24,
    width: 100,
  },
  rightIconContainer: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: Colors.dark.background,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 10,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.2,
    shadowRadius: 3.84,
    elevation: 3,
    borderWidth: 2,
    borderColor: Colors.dark.buttonBackground,
  },
  rightLabel: {
    color: Colors.dark.text,
    fontSize: 13,
    textAlign: 'center',
    marginBottom: 10,
    opacity: 0.8,
  },
  rightLabelEnabled: {
    color: Colors.dark.primary,
    fontWeight: 'bold',
    opacity: 1,
  },
  switch: {
    transform: [{ scaleX: 0.9 }, { scaleY: 0.9 }],
  },
});

export default DevicePermissionsCard;
