import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Device, LockState, EnrollStatus, CommandLogEntry } from '../types';
import MQTTService from '../services/mqtt';

const STORAGE_KEY = 'apollo-simulator-devices';

// Load devices from localStorage
const loadDevicesFromStorage = (): Device[] => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      const devices = JSON.parse(stored);
      // Ensure all devices have required fields with defaults
      return devices.map((device: any) => ({
        ...device,
        lockState: device.lockState || 'locked',
        enrollStatus: device.enrollStatus || 'idle',
      }));
    }
  } catch (error) {
    console.error('Failed to load devices from storage:', error);
  }
  return [];
};

// Save devices to localStorage
const saveDevicesToStorage = (devices: Device[]) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(devices));
  } catch (error) {
    console.error('Failed to save devices to storage:', error);
  }
};

interface DeviceContextType {
  devices: Device[];
  addDevice: (deviceId: string) => void;
  removeDevice: (deviceId: string) => void;
  toggleDeviceActive: (deviceId: string) => void;
  updateDeviceLastMessage: (deviceId: string) => void;
  selectedDevice: Device | null;
  setSelectedDevice: (device: Device | null) => void;
  commandLog: CommandLogEntry[];
}

const DeviceContext = createContext<DeviceContextType | undefined>(undefined);

const DeviceProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [devices, setDevices] = useState<Device[]>(loadDevicesFromStorage);
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
  const [commandLog, setCommandLog] = useState<CommandLogEntry[]>([]);

  // Save devices to localStorage whenever devices change
  useEffect(() => {
    saveDevicesToStorage(devices);
  }, [devices]);

  // Update selectedDevice when devices change to keep it in sync
  useEffect(() => {
    if (selectedDevice) {
      const updatedDevice = devices.find(d => d.id === selectedDevice.id);
      setSelectedDevice(updatedDevice || null);
    }
  }, [devices, selectedDevice?.id]);

  useEffect(() => {
    const intervals: NodeJS.Timeout[] = [];
    const mqtt = MQTTService.getInstance();

    // --- Incoming MQTT messages handler (unlock, enrol) ---
    const handleMqttMessage = (topic: string, payload: any) => {
      // Log every inbound command (before specific handling)
      setCommandLog(prev => [
        { timestamp: Date.now(), topic, payload },
        ...prev.slice(0, 49),
      ]);

      // Unlock command
      if (topic === 'devices/commands/unlock') {
        const { hardwareId } = payload as { hardwareId: string };
        if (!hardwareId) return;
        setDevices(prev => prev.map(d => {
          if (d.id !== hardwareId) return d;
          // Change lock state locally and push notification event
          const updated: Device = {
            ...d,
            lockState: 'unlocked' as LockState,
          };

          // Auto-relock after 10 seconds
          setTimeout(() => {
            setDevices(prev2 => prev2.map(dd => dd.id === hardwareId ? { ...dd, lockState: 'locked' } : dd));
          }, 10000);

          // Publish door event (UnlockedRemote)
          mqtt.publishDoorEvent('doorlock/1/event', {
            hardwareId,
            eventType: 'UnlockedRemote',
            description: 'Remote unlock command executed',
            mediaUrl: '',
            timestamp: Date.now().toString(),
          });

          return updated;
        }));
      }

      // Enrol start (topic pattern: doorlock/+/enroll/start)
      if (topic.endsWith('/enroll/start')) {
        const { hardwareId, user_fp_id } = payload as { hardwareId: string; user_fp_id: number };
        if (!hardwareId) return;
        
        console.log(`Enroll request received for device ${hardwareId}, user_fp_id: ${user_fp_id}`);
        
        // Update state to in_progress
        setDevices(prev => prev.map(d => {
          if (d.id !== hardwareId) return d;
          console.log(`Setting enroll status to in_progress for device ${d.id}`);
          return { ...d, enrollStatus: 'in_progress' as EnrollStatus };
        }));

        // Simulate asynchronous enrol result
        setTimeout(() => {
          const success = Math.random() < 0.8; // 80% success chance
          const eventType = success ? 'EnrollSuccess' : 'EnrollFailure';
          const description = success ? `FP enroll success id ${user_fp_id}` : `FP enroll failure code 0x02`;

          console.log(`Enroll ${success ? 'succeeded' : 'failed'} for device ${hardwareId}`);
          
          setDevices(prev => prev.map(d => {
            if (d.id !== hardwareId) return d;
            return { ...d, enrollStatus: success ? 'success' : 'failure' };
          }));

          mqtt.publishEnrollStatus('doorlock/1/enroll/status', {
            hardwareId,
            eventType,
            description,
            mediaUrl: '',
            timestamp: Date.now().toString(),
          });
          
          // Reset enroll status after 3 seconds
          setTimeout(() => {
            setDevices(prev => prev.map(d => {
              if (d.id !== hardwareId) return d;
              return { ...d, enrollStatus: 'idle' };
            }));
          }, 3000);
        }, 3000);
      }
    };

    mqtt.addMessageListener(handleMqttMessage);

    devices.forEach(device => {
      if (device.isActive) {
        // Send initial hello
        mqtt.publishHello({
          hardwareId: device.id,
          deviceType: device.deviceType,
        });
        
        // Set up interval for periodic messages
        const interval = setInterval(() => {
          mqtt.publishHello({
            hardwareId: device.id,
            deviceType: device.deviceType,
          });
        }, 60000);
        
        intervals.push(interval);
      }
    });

    // Cleanup function to clear all intervals
    return () => {
      intervals.forEach(interval => clearInterval(interval));
      mqtt.removeMessageListener(handleMqttMessage);
    };
  }, [devices]);

  const addDevice = (deviceId: string) => {
    const newDevice: Device = {
      id: deviceId,
      isActive: false,
      deviceType: 'doorlock',
      lockState: 'locked',
      enrollStatus: 'idle',
    };
    setDevices(prev => [...prev, newDevice]);
  };

  const removeDevice = (deviceId: string) => {
    setDevices(prev => prev.filter(d => d.id !== deviceId));
    if (selectedDevice?.id === deviceId) {
      setSelectedDevice(null);
    }
  };

  const toggleDeviceActive = (deviceId: string) => {
    setDevices(prev =>
      prev.map(d =>
        d.id === deviceId
          ? { ...d, isActive: !d.isActive }
          : d
      )
    );
  };

  const updateDeviceLastMessage = (deviceId: string) => {
    setDevices(prev =>
      prev.map(d =>
        d.id === deviceId
          ? { ...d, lastMessageTime: new Date().toISOString() }
          : d
      )
    );
  };

  return (
    <DeviceContext.Provider
      value={{
        devices,
        addDevice,
        removeDevice,
        toggleDeviceActive,
        updateDeviceLastMessage,
        selectedDevice,
        setSelectedDevice,
        commandLog,
      }}
    >
      {children}
    </DeviceContext.Provider>
  );
};

// Hook must be named function for proper HMR
function useDevices() {
  const context = useContext(DeviceContext);
  if (context === undefined) {
    throw new Error('useDevices must be used within a DeviceProvider');
  }
  return context;
}

// Named exports
export { DeviceProvider };
export { useDevices };
