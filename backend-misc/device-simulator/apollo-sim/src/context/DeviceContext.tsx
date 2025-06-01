import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Device } from '../types';
import MQTTService from '../services/mqtt';

interface DeviceContextType {
  devices: Device[];
  addDevice: (deviceId: string) => void;
  removeDevice: (deviceId: string) => void;
  toggleDeviceActive: (deviceId: string) => void;
  updateDeviceLastMessage: (deviceId: string) => void;
  selectedDevice: Device | null;
  setSelectedDevice: (device: Device | null) => void;
}

const DeviceContext = createContext<DeviceContextType | undefined>(undefined);

const DeviceProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);

  useEffect(() => {
    const intervals: NodeJS.Timeout[] = [];
    const mqtt = MQTTService.getInstance();

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
    };
  }, [devices]);

  const addDevice = (deviceId: string) => {
    const newDevice: Device = {
      id: deviceId,
      isActive: false,
      deviceType: 'doorlock',
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
