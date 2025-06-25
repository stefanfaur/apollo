import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Stack, useRouter, useLocalSearchParams } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import ApolloTextInput from '@/components/ui/apollo-text-input';
import ApolloButton from '@/components/ui/apollo-button';
import StepIndicator from '@/components/ui/step-indicator';
import DevicePermissionsCard from '@/components/ui/device-permissions-card';
import { Colors } from '@/constants/colors';
import { userService } from '@/services/user-service';
import { deviceService, Device } from '@/services/device-service';
import { homeAccessService } from '@/services/home-access-service';
import debounce from 'lodash/debounce';
import { UserDTO } from "@/models/userDTO";

interface DeviceRight {
  deviceId: string;
  rights: Set<string>;
}

const STEPS = ['Search User', 'Configure Access'];
const ERROR_COLOR = '#ff6b6b';

export default function AddGuestPage() {
  const router = useRouter();
  const { id: homeId } = useLocalSearchParams();
  const [step, setStep] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<UserDTO[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null);
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedRights, setSelectedRights] = useState<Map<string, Set<string>>>(new Map());
  const [isLoading, setIsLoading] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchDevices = async () => {
    try {
      setIsLoading(true);
      const data = await deviceService.getHomeDevices(homeId as string);
      setDevices(data);
    } catch (err) {
      setError('Failed to load devices');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDevices();
  }, []);

  const searchUsers = async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      setIsSearching(true);
      const results = await userService.searchUsers(query);
      setSearchResults(results);
    } catch (err) {
      setError('Failed to search users');
    } finally {
      setIsSearching(false);
    }
  };

  const debouncedSearch = useCallback(
    debounce((query: string) => searchUsers(query), 500),
    []
  );

  const handleSearchChange = (text: string) => {
    setSearchQuery(text);
    setSelectedUser(null);
    debouncedSearch(text);
  };

  const handleSelectUser = (user: UserDTO) => {
    setSelectedUser(user);
    setSearchQuery(user.email);
    setSearchResults([]);
  };

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

  const handleSubmit = async () => {
    if (!selectedUser) {
      setError('Please select a user.');
      return;
    }

    try {
      setIsLoading(true);
      const deviceRights = Array.from(selectedRights.entries()).map(([deviceId, rights]) => ({
        deviceId,
        rights,
      }));
      await homeAccessService.addHomeGuest(homeId as string, selectedUser.email, deviceRights);
      router.back();
    } catch (err) {
      setError('Failed to add guest.');
    } finally {
      setIsLoading(false);
    }
  };

  const goToNextStep = () => {
    if (!selectedUser) {
      setError('Please select a user first.');
      return;
    }
    setError(null);
    setStep(1);
  };

  const goToPreviousStep = () => {
    setStep(0);
  };

  const renderSearchStep = () => (
    <View style={styles.stepContainer}>
      <ScrollView contentContainerStyle={styles.searchScrollContent}>
        <ApolloTextInput
          value={searchQuery}
          onChangeText={handleSearchChange}
          placeholder="Search by email"
          autoCapitalize="none"
          autoCorrect={false}
          style={styles.input}
        />

        {isSearching && <ActivityIndicator style={styles.spinner} />}

        {searchResults.map((user: UserDTO) => (
          <TouchableOpacity
            key={user.uuid}
            style={styles.userItem}
            onPress={() => handleSelectUser(user)}
          >
            <Text style={styles.userText}>{user.email}</Text>
          </TouchableOpacity>
        ))}

        {error && (
          <View style={styles.errorContainer}>
            <Ionicons name="alert-circle" size={20} color={ERROR_COLOR} />
            <Text style={styles.errorText}>{error}</Text>
          </View>
        )}
      </ScrollView>

      <View style={styles.buttonContainerSingle}>
        <ApolloButton
          title="Next"
          onPress={goToNextStep}
          disabled={!selectedUser}
          style={styles.button}
        />
      </View>
    </View>
  );

  const renderDevicesStep = () => (
    <View style={styles.stepContainer}>
      <ScrollView contentContainerStyle={styles.devicesScrollContent}>
        {devices.map((device) => (
          <DevicePermissionsCard
            key={device.uuid}
            device={device}
            selectedRights={selectedRights.get(device.uuid) || new Set()}
            onToggleRight={(right) => toggleRight(device.uuid, right)}
          />
        ))}
      </ScrollView>

      <View style={styles.buttonContainerDouble}>
        <ApolloButton
          title="Back"
          onPress={goToPreviousStep}
          variant="secondary"
          style={[styles.button, styles.smallButton]}
        />
        <ApolloButton
          title="Add Guest"
          onPress={handleSubmit}
          style={[styles.button, styles.smallButton]}
          disabled={isLoading}
        />
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <Stack.Screen
        options={{
          headerShown: true,
          title: `Add Guest (${step + 1}/${STEPS.length})`,
          headerTitleStyle: {
            color: Colors.dark.text,
          },
          headerStyle: {
            backgroundColor: Colors.dark.background,
          },
          headerTintColor: Colors.dark.text,
        }}
      />
      <View style={styles.content}>
        <StepIndicator steps={STEPS} currentStep={step} />
        {step === 0 ? renderSearchStep() : renderDevicesStep()}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.dark.background,
  },
  content: {
    flex: 1,
  },
  stepContainer: {
    flex: 1,
    position: 'relative',
  },
  searchScrollContent: {
    padding: 16,
    paddingBottom: 80,
  },
  devicesScrollContent: {
    padding: 16,
    paddingBottom: 80,
  },
  input: {
    marginBottom: 16,
  },
  spinner: {
    marginVertical: 8,
  },
  userItem: {
    padding: 12,
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 8,
    marginBottom: 8,
  },
  userText: {
    color: Colors.dark.text,
    fontSize: 14,
  },
  errorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: ERROR_COLOR + '20',
    padding: 12,
    borderRadius: 8,
    marginBottom: 16,
  },
  errorText: {
    color: ERROR_COLOR,
    fontSize: 14,
    marginLeft: 8,
  },
  buttonContainerSingle: {
    position: 'absolute',
    bottom: 20,
    left: 0,
    right: 0,
    padding: 16,
    backgroundColor: Colors.dark.background,
    borderTopWidth: 1,
    borderTopColor: Colors.dark.buttonBackground,
  },
  buttonContainerDouble: {
    position: 'absolute',
    bottom: 20,
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
