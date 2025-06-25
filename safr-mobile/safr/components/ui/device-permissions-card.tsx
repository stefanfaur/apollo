import React from 'react';
import { View, Text, StyleSheet, Switch, Animated, Easing } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';
import { Device } from '@/services/device-service';
import { DEVICE_RIGHTS } from '@/constants/device-rights';

interface DevicePermissionsCardProps {
  device: Device;
  selectedRights: Set<string>;
  onToggleRight: (right: string) => void;
}

const RightTile: React.FC<{ right: typeof DEVICE_RIGHTS[number]; isEnabled: boolean; onToggle: () => void; }> = ({ right, isEnabled, onToggle }) => {
  const scale = React.useRef(new Animated.Value(1)).current;
  const bgAnim = React.useRef(new Animated.Value(isEnabled ? 1 : 0)).current;

  React.useEffect(() => {
    // pop animation 
    Animated.sequence([
      Animated.spring(scale, {
        toValue: 1.05,
        friction: 5,
        useNativeDriver: false,
      }),
      Animated.spring(scale, {
        toValue: 1,
        friction: 6,
        useNativeDriver: false,
      }),
    ]).start();

    // background colour transition
    Animated.timing(bgAnim, {
      toValue: isEnabled ? 1 : 0,
      duration: 250,
      easing: Easing.linear,
      useNativeDriver: false,
    }).start();
  }, [isEnabled]);

  const backgroundColor = bgAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [Colors.dark.background, Colors.dark.primary + '20'],
  });

  return (
    <Animated.View style={[styles.rightItem, { backgroundColor, transform: [{ scale }] }]}>
      <View style={[styles.rightIconContainer, isEnabled && styles.rightIconContainerEnabled]}>
        <Ionicons
          name={right.icon}
          size={20}
          color={isEnabled ? Colors.dark.primary : Colors.dark.text}
        />
      </View>
      <Text
        style={[styles.rightLabel, isEnabled && styles.rightLabelEnabled]}
        numberOfLines={2}
      >
        {right.label}
      </Text>
      <Switch
        value={isEnabled}
        onValueChange={onToggle}
        trackColor={{ false: Colors.dark.buttonBackground, true: Colors.dark.primary }}
        style={styles.switch}
      />
    </Animated.View>
  );
};

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
        {DEVICE_RIGHTS.map(right => (
          <RightTile
            key={right.id}
            right={right}
            isEnabled={selectedRights.has(right.id)}
            onToggle={() => onToggleRight(right.id)}
          />
        ))}
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
    overflow: 'hidden',
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
    justifyContent: 'space-between',
  },
  rightItem: {
    width: '47%',
    backgroundColor: Colors.dark.background,
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 10,
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 16,
    flexDirection: 'column',
  },
  rightItemLeft: {
    display: 'none',
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
    textAlign: 'center',
    marginTop: 8,
  },
  rightLabelEnabled: {
    color: Colors.dark.primary,
    fontWeight: 'bold',
    opacity: 1,
  },
  switch: {
    transform: [{ scaleX: 0.8 }, { scaleY: 0.8 }],
    marginTop: 8,
  },
});

export default DevicePermissionsCard;
