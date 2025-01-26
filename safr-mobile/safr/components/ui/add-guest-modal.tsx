import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import CustomModal from './custom-modal';
import ApolloTextInput from './apollo-text-input';
import ApolloButton from './apollo-button';
import StepIndicator from './step-indicator';
import DevicePermissionsCard from './device-permissions-card';
import { Colors } from '@/constants/colors';
import { userService } from '@/services/user-service';
import { deviceService, Device } from '@/services/device-service';
import debounce from 'lodash/debounce';
import {UserDTO} from "@/models/userDTO";

interface DeviceRight {
  deviceId: string;
  rights: Set<string>;
}

interface AddGuestModalProps {
  visible: boolean;
  onClose: () => void;
  onSubmit: (email: string, deviceRights: DeviceRight[]) => Promise<void>;
  homeId: string;
}

const STEPS = ['Search User', 'Configure Access'];

const AddGuestModal: React.FC<AddGuestModalProps> = ({
                                                       visible,
                                                       onClose,
                                                       onSubmit,
                                                       homeId,
                                                     }) => {
  const [step, setStep] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<UserDTO[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null);
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedRights, setSelectedRights] = useState<Map<string, Set<string>>>(new Map());
  const [isLoading, setIsLoading] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const slideAnim = useRef(new Animated.Value(0)).current;

  const fetchDevices = async () => {
    try {
      setIsLoading(true);
      const data = await deviceService.getHomeDevices(homeId);
      setDevices(data);
    } catch (err) {
      setError('Failed to load devices');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (visible) {
      fetchDevices();
    }
  }, [visible]);

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

  const handleSelectUser = (user : UserDTO) => {
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
      await onSubmit(selectedUser.email, deviceRights);
      onClose();
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
    Animated.timing(slideAnim, {
      toValue: -300,
      duration: 300,
      useNativeDriver: true,
    }).start();
  };

  const goToPreviousStep = () => {
    setStep(0);
    Animated.timing(slideAnim, {
      toValue: 0,
      duration: 300,
      useNativeDriver: true,
    }).start();
  };

  return (
      <CustomModal visible={visible} title="Add Guest" onClose={onClose}>
        <StepIndicator steps={STEPS} currentStep={step} />

        <Animated.View
            style={{
              flexDirection: 'row',
              width: '200%',
              transform: [{ translateX: slideAnim }],
            }}
        >
          {/* Step 1: Search User */}
          <View style={styles.stepContainer}>
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

            {error && <Text style={styles.errorText}>{error}</Text>}

            <ApolloButton
                title="Next"
                onPress={goToNextStep}
                disabled={!selectedUser}
                style={styles.nextButton}
            />
          </View>

          {/* Step 2: Configure Access */}
          <ScrollView style={styles.stepContainer}>
            {devices.map((device) => (
                <DevicePermissionsCard
                    key={device.uuid}
                    device={device}
                    selectedRights={selectedRights.get(device.uuid) || new Set()}
                    onToggleRight={(right) => toggleRight(device.uuid, right)}
                />
            ))}

            <View style={styles.actionButtons}>
              <ApolloButton
                  title="Back"
                  onPress={goToPreviousStep}
                  variant="secondary"
                  style={styles.backButton}
              />
              <ApolloButton
                  title="Add Guest"
                  onPress={handleSubmit}
                  style={styles.submitButton}
                  disabled={isLoading}
              />
            </View>
          </ScrollView>
        </Animated.View>
      </CustomModal>
  );
};

const styles = StyleSheet.create({
  stepContainer: {
    width: 300,
    padding: 16,
  },
  input: {
    width: '100%',
    marginBottom: 16,
  },
  spinner: {
    marginVertical: 16,
  },
  userItem: {
    padding: 12,
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 8,
    marginBottom: 8,
  },
  userText: {
    color: Colors.dark.text,
  },
  errorText: {
    color: 'red',
    marginTop: 8,
  },
  nextButton: {
    marginTop: 24,
    alignSelf: 'center',
  },
  actionButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 16,
  },
  backButton: {
    flex: 1,
    marginRight: 8,
  },
  submitButton: {
    flex: 1,
  },
});

export default AddGuestModal;
