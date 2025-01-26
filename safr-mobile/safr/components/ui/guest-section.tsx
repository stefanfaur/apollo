import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';
import ApolloButton from './apollo-button';
import AddGuestModal from './add-guest-modal';
import EditGuestDeviceModal from './edit-guest-device-modal';
import { homeAccessService, GuestDTO } from '@/services/home-access-service';

interface GuestSectionProps {
  homeId: string;
}

interface GuestDevice {
  deviceId: string;
  deviceName: string;
  accessRights: string[];
}

interface GuestWithDevices extends Omit<GuestDTO, 'devices'> {
  devices: GuestDevice[];
}

const GuestSection: React.FC<GuestSectionProps> = ({ homeId }) => {
  const [guests, setGuests] = useState<GuestWithDevices[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showAddGuest, setShowAddGuest] = useState(false);
  const [selectedGuest, setSelectedGuest] = useState<GuestWithDevices | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [removingGuestId, setRemovingGuestId] = useState<string | null>(null);

  const loadGuests = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const fetchedGuests = await homeAccessService.getHomeGuests(homeId);
      // Transform the data to match GuestDevice interface
      const transformedGuests = fetchedGuests.map(guest => ({
        ...guest,
        devices: guest.devices.map(device => ({
          deviceId: device.deviceId,
          deviceName: `Device ${device.deviceId}`, // TODO: Fetch actual device names
          accessRights: device.rights,
        }))
      }));
      setGuests(transformedGuests);
    } catch (err) {
      setError('Failed to load guests');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadGuests();
  }, [homeId]);

  const handleAddGuest = async (email: string, deviceRights: { deviceId: string, rights: Set<string> }[]) => {
    try {
      await homeAccessService.addHomeGuest(homeId, email, deviceRights);
      await loadGuests();
      setShowAddGuest(false);
    } catch (err) {
      throw err;
    }
  };

  const handleUpdateGuestRights = async (deviceRights: { deviceId: string, rights: Set<string> }[]) => {
    if (!selectedGuest) return;

    try {
      await homeAccessService.updateGuestDeviceRights(homeId, selectedGuest.uuid, deviceRights);
      await loadGuests();
      setSelectedGuest(null);
    } catch (err) {
      setError('Failed to update guest permissions');
      console.error(err);
    }
  };

  const handleRemoveGuest = async (guestId: string) => {
    try {
      setRemovingGuestId(guestId);
      setError(null);
      await homeAccessService.removeHomeGuest(homeId, guestId);
      await loadGuests();
    } catch (err) {
      setError('Failed to remove guest');
      console.error(err);
    } finally {
      setRemovingGuestId(null);
    }
  };

  const renderDeviceRights = (devices: GuestDevice[]) => {
    if (devices.length === 0) {
      return <Text style={styles.noDevicesText}>No device access</Text>;
    }

    return (
      <View style={styles.devicesContainer}>
        {devices.map((device) => (
          <View key={device.deviceId} style={styles.deviceItem}>
            <Text style={styles.deviceName}>{device.deviceName}</Text>
            <Text style={styles.deviceRights}>
              {device.accessRights.join(', ')}
            </Text>
          </View>
        ))}
      </View>
    );
  };

  const renderGuestItem = ({ item }: { item: GuestWithDevices }) => (
    <View style={styles.guestItem}>
      <View style={styles.guestHeader}>
        <Text style={styles.guestEmail}>{item.email}</Text>
        <View style={styles.guestActions}>
          <TouchableOpacity 
            onPress={() => setSelectedGuest(item)}
            style={styles.actionButton}
          >
            <Ionicons name="settings-outline" size={20} color={Colors.dark.text} />
          </TouchableOpacity>
          <TouchableOpacity 
            onPress={() => handleRemoveGuest(item.uuid)}
            style={styles.actionButton}
            disabled={removingGuestId === item.uuid}
          >
            {removingGuestId === item.uuid ? (
              <ActivityIndicator size="small" color={Colors.dark.text} />
            ) : (
              <Ionicons name="trash-outline" size={20} color={Colors.dark.text} />
            )}
          </TouchableOpacity>
        </View>
      </View>
      {renderDeviceRights(item.devices)}
    </View>
  );

  if (isLoading && !guests.length) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color={Colors.dark.text} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.spacer} />
        <TouchableOpacity 
          onPress={() => setShowAddGuest(true)}
          style={styles.addButton}
        >
          <Ionicons name="add-circle" size={24} color={Colors.dark.text} />
        </TouchableOpacity>
      </View>
      
      {guests.length > 0 ? (
        <FlatList
          data={guests}
          renderItem={renderGuestItem}
          keyExtractor={(item) => item.uuid}
          style={styles.list}
        />
      ) : !isLoading && (
        <View style={styles.emptyState}>
          <Text style={styles.emptyText}>No guests added yet</Text>
        </View>
      )}

      {error && (
        <Text style={styles.errorText}>{error}</Text>
      )}

      <AddGuestModal
        visible={showAddGuest}
        onClose={() => setShowAddGuest(false)}
        onSubmit={handleAddGuest}
        homeId={homeId}
      />

      {selectedGuest && (
        <EditGuestDeviceModal
          visible={true}
          onClose={() => setSelectedGuest(null)}
          onSubmit={handleUpdateGuestRights}
          homeId={homeId}
          guestId={selectedGuest.uuid}
          currentDevices={selectedGuest.devices}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    marginBottom: 20,
  },
  spacer: {
    flex: 1,
  },
  addButton: {
    padding: 4,
  },
  list: {
    flex: 1,
  },
  guestItem: {
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 8,
    marginBottom: 12,
    padding: 12,
  },
  guestHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  guestEmail: {
    color: Colors.dark.text,
    fontSize: 16,
    fontWeight: 'bold',
  },
  guestActions: {
    flexDirection: 'row',
    gap: 8,
  },
  actionButton: {
    padding: 4,
    minWidth: 28,
    alignItems: 'center',
  },
  devicesContainer: {
    marginTop: 8,
  },
  deviceItem: {
    marginVertical: 4,
  },
  deviceName: {
    color: Colors.dark.text,
    fontSize: 14,
  },
  deviceRights: {
    color: Colors.dark.text,
    fontSize: 12,
    opacity: 0.7,
  },
  noDevicesText: {
    color: Colors.dark.text,
    fontSize: 14,
    fontStyle: 'italic',
    opacity: 0.7,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    color: Colors.dark.text,
    fontSize: 16,
    opacity: 0.7,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
    marginTop: 8,
    marginBottom: 8,
    textAlign: 'center',
  },
});

export default GuestSection;
