import React, { useState, useEffect } from 'react';
import { View, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { Stack, useRouter, useLocalSearchParams } from 'expo-router';
import ApolloButton from '@/components/ui/apollo-button';
import DevicePermissionsCard from '@/components/ui/device-permissions-card';
import { Colors } from '@/constants/colors';
import { deviceService, Device } from '@/services/device-service';
import { homeAccessService, GuestDeviceRights } from '@/services/home-access-service';

export default function EditGuestPage() {
  const router = useRouter();
  const { id: homeId, guestId } = useLocalSearchParams();
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedRights, setSelectedRights] = useState<Map<string, Set<string>>>(new Map());
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [guestEmail, setGuestEmail] = useState<string>('');

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [devicesData, guestsData] = await Promise.all([
        deviceService.getHomeDevices(homeId as string),
        homeAccessService.getHomeGuests(homeId as string)
      ]);
      
      setDevices(devicesData);
      const guest = guestsData.find(g => g.uuid === guestId);
      if (!guest) throw new Error('Guest not found');
      
      setGuestEmail(guest.email);
      
      // Convert guest rights to our Map format
      const rightsMap = new Map<string, Set<string>>();
      guest.devices.forEach((device: GuestDeviceRights) => {
        rightsMap.set(device.deviceId, new Set(device.rights));
      });
      setSelectedRights(rightsMap);
    } catch (err) {
      setError('Failed to load guest data');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const toggleRight = (deviceId: string, right: string) => {
    setSelectedRights((prev) => {
      const newRights = new Map(prev);
      const rights = new Set(prev.get(deviceId) || []);
      if (rights.has(right)) rights.delete(right);
      else rights.add(right);

      if (rights.size > 0) newRights.set(deviceId, rights);
      else newRights.delete(deviceId);

      return newRights;
    });
  };

  const handleSave = async () => {
    try {
      setIsLoading(true);
      const deviceRights = Array.from(selectedRights.entries()).map(([deviceId, rights]) => ({
        deviceId,
        rights,
      }));
      await homeAccessService.updateGuestDeviceRights(homeId as string, guestId as string, deviceRights);
      router.back();
    } catch (err) {
      setError('Failed to update guest rights');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={Colors.dark.primary} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen
        options={{
          headerShown: true,
          title: `Edit Guest Access`,
          headerTitleStyle: {
            color: Colors.dark.text,
          },
          headerStyle: {
            backgroundColor: Colors.dark.background,
          },
          headerTintColor: Colors.dark.text,
        }}
      />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {devices.map((device) => (
          <DevicePermissionsCard
            key={device.uuid}
            device={device}
            selectedRights={selectedRights.get(device.uuid) || new Set()}
            onToggleRight={(right) => toggleRight(device.uuid, right)}
          />
        ))}
      </ScrollView>
      <View style={styles.buttonContainer}>
        <ApolloButton
          title="Cancel"
          onPress={() => router.back()}
          variant="secondary"
          style={[styles.button, styles.smallButton]}
        />
        <ApolloButton
          title="Save Changes"
          onPress={handleSave}
          style={[styles.button, styles.smallButton]}
          disabled={isLoading}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.dark.background,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.dark.background,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 80,
  },
  buttonContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    flexDirection: 'row',
    padding: 12,
    paddingBottom: 32,
    backgroundColor: Colors.dark.background,
    borderTopWidth: 1,
    borderTopColor: Colors.dark.buttonBackground,
  },
  button: {
    marginVertical: 0,
  },
  smallButton: {
    flex: 1,
    marginHorizontal: 4,
    paddingVertical: 8,
  },
});
