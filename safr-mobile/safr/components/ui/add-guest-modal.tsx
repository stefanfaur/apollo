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
import { UserDTO } from '@/models/userDTO';
import { deviceService, Device } from '@/services/device-service';
import debounce from 'lodash/debounce';

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
  // State
  const [step, setStep] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<UserDTO[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null);
  const [devices, setDevices] = useState<Device[]>([]);
  const [selectedRights, setSelectedRights] = useState<Map<string, Set<string>>>(new Map());
  const [isLoading, setIsLoading] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Animation
  const slideAnim = useRef(new Animated.Value(0)).current;
  const stepProgress = useRef(new Animated.Value(0)).current;

  const fetchDevices = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await deviceService.getHomeDevices(homeId);
      setDevices(data);
    } catch (err) {
      setError('Failed to load devices');
      console.error(err);
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
      setError(null);
      const results = await userService.searchUsers(query);
      setSearchResults(results);
    } catch (err) {
      setError('Failed to search users');
      setSearchResults([]);
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
    if (!selectedUser) {
      setError('Please select a user');
      return;
    }

    setError(null);
    setIsLoading(true);

    try {
      const deviceRights: DeviceRight[] = Array.from(selectedRights.entries()).map(
        ([deviceId, rights]) => ({
          deviceId,
          rights,
        })
      );

      await onSubmit(selectedUser.email, deviceRights);
      setSearchQuery('');
      setSelectedUser(null);
      setSelectedRights(new Map());
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add guest');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setSearchQuery('');
    setSearchResults([]);
    setSelectedUser(null);
    setSelectedRights(new Map());
    setError(null);
    onClose();
  };

  const animateToStep = (newStep: number) => {
    Animated.parallel([
      Animated.timing(slideAnim, {
        toValue: -newStep,
        duration: 300,
        useNativeDriver: true,
      }),
      Animated.timing(stepProgress, {
        toValue: newStep,
        duration: 300,
        useNativeDriver: false,
      }),
    ]).start();
  };

  const goToNextStep = () => {
    if (!selectedUser) {
      setError('Please select a user first');
      return;
    }
    setError(null);
    setStep(1);
    animateToStep(1);
  };

  const goToPreviousStep = () => {
    setStep(0);
    animateToStep(0);
  };

  return (
    <CustomModal visible={visible} title="Add Guest" onClose={handleClose}>
      <StepIndicator
        steps={STEPS}
        currentStep={step}
        progress={stepProgress}
      />
      <View style={styles.stepsContainer}>
        <Animated.View 
          style={[
            styles.stepsContent,
            {
              transform: [{
                translateX: Animated.multiply(slideAnim, -300),
              }],
            },
          ]}
        >
          {/* Step 1: Search User */}
          <View style={styles.stepWidth}>
            <View style={styles.searchSection}>
              <View style={styles.searchContainer}>
                <ApolloTextInput
                  value={searchQuery}
                  onChangeText={handleSearchChange}
                  placeholder="Search by email"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  autoCorrect={false}
                />
                {isSearching && (
                  <ActivityIndicator
                    size="small"
                    color={Colors.dark.text}
                    style={styles.searchSpinner}
                  />
                )}
              </View>

              {searchResults.length > 0 && !selectedUser && (
                <View style={styles.searchResults}>
                  {searchResults.map(user => (
                    <TouchableOpacity
                      key={user.uuid}
                      style={styles.userItem}
                      onPress={() => handleSelectUser(user)}
                    >
                      <View style={styles.userItemContent}>
                        <View style={styles.userAvatar}>
                          <Ionicons name="person" size={24} color={Colors.dark.text} />
                        </View>
                        <Text style={styles.userEmail}>{user.email}</Text>
                      </View>
                    </TouchableOpacity>
                  ))}
                </View>
              )}

              {selectedUser && (
                <View style={styles.selectedUserContainer}>
                  <View style={styles.selectedUserContent}>
                    <View style={styles.userAvatar}>
                      <Ionicons name="person" size={24} color={Colors.dark.primary} />
                    </View>
                    <Text style={styles.selectedUserEmail}>{selectedUser.email}</Text>
                  </View>
                </View>
              )}

              {error && <Text style={styles.errorText}>{error}</Text>}

              <ApolloButton
                title="Next"
                onPress={goToNextStep}
                disabled={!selectedUser}
                style={styles.button}
              />
            </View>
          </View>

          {/* Step 2: Configure Permissions */}
          <View style={styles.stepWidth}>
            <ScrollView 
              style={styles.devicesContainer}
              showsVerticalScrollIndicator={true}
              bounces={false}
            >
              {isLoading ? (
                <ActivityIndicator size="large" color={Colors.dark.text} />
              ) : (
                <>
                  {devices.map(device => (
                    <DevicePermissionsCard
                      key={device.uuid}
                      device={device}
                      selectedRights={selectedRights.get(device.uuid) || new Set()}
                      onToggleRight={(right) => toggleRight(device.uuid, right)}
                    />
                  ))}
                </>
              )}

              <View style={styles.buttonsContainer}>
                <ApolloButton
                  title="Back"
                  onPress={goToPreviousStep}
                  style={styles.backButton}
                  variant="secondary"
                />
                <ApolloButton
                  title={isLoading ? "Adding..." : "Add Guest"}
                  onPress={handleSubmit}
                  disabled={!selectedUser || isLoading}
                  style={styles.submitButton}
                />
              </View>
            </ScrollView>
          </View>
        </Animated.View>
      </View>
    </CustomModal>
  );
};

const styles = StyleSheet.create({
  stepWidth: {
    width: '100%',
  },
  stepsContainer: {
    width: '100%',
    overflow: 'hidden',
    height: 400,
  },
  stepsContent: {
    flexDirection: 'row',
    width: '200%',
    height: '100%',
  },
  searchSection: {
    padding: 16,
    height: '100%',
  },
  devicesContainer: {
    height: '100%',
    paddingHorizontal: 16,
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  searchSpinner: {
    marginLeft: 8,
  },
  searchResults: {
    maxHeight: 180,
    marginTop: 8,
    borderRadius: 8,
    backgroundColor: Colors.dark.buttonBackground,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.2,
    shadowRadius: 3,
    elevation: 4,
    borderWidth: 1,
    borderColor: Colors.dark.primary + '20',
  },
  userItem: {
    borderBottomWidth: 1,
    borderBottomColor: Colors.dark.background + '40',
  },
  userItemContent: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
  },
  userAvatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.dark.background,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  userEmail: {
    color: Colors.dark.text,
    fontSize: 16,
    flex: 1,
  },
  selectedUserContainer: {
    marginTop: 16,
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 8,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: Colors.dark.primary,
  },
  selectedUserContent: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
  },
  selectedUserEmail: {
    color: Colors.dark.primary,
    fontSize: 16,
    fontWeight: 'bold',
    flex: 1,
  },
  button: {
    marginTop: 16,
  },
  buttonsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 16,
    marginBottom: 16,
    paddingHorizontal: 16,
  },
  backButton: {
    flex: 1,
    marginRight: 8,
  },
  submitButton: {
    flex: 2,
    marginLeft: 8,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
    marginTop: 16,
    marginBottom: 8,
    textAlign: 'center',
    backgroundColor: '#ff6b6b20',
    padding: 12,
    borderRadius: 8,
  },
});

export default AddGuestModal;
