import React, { useCallback, useState } from 'react';
import { View, Text, Image, StyleSheet, TouchableOpacity, ScrollView, Dimensions } from 'react-native';
import { useRouter } from 'expo-router';
import GradientBg from '@/components/ui/gradient-bg';
import { Ionicons } from '@expo/vector-icons';
import globalStyles from '@/constants/global-styles';
import DropdownListItem from '@/components/ui/dropdown-list-item';
import DeviceCard from '@/components/ui/device-card';
import { HomeDTO } from "@/models/homeDTO";
import { createHome, fetchHomes } from "@/services/home-service";
import { deviceService } from "@/services/device-service";
import { Colors } from "@/constants/colors";
import { useFocusEffect } from '@react-navigation/native';
import AddDeviceForm from "@/components/forms/add-device-form";
import CustomModal from "@/components/ui/custom-modal";
import AddHomeForm from "@/components/forms/add-home-form";

export default function HomeScreen() {
  const [homes, setHomes] = useState<HomeDTO[]>([]);
  const [isAddHomeModalVisible, setAddHomeModalVisible] = useState(false);
  const [isAddDeviceModalVisible, setAddDeviceModalVisible] = useState(false);
  const router = useRouter();
  const [selectedHomeUuid, setSelectedHomeUuid] = useState<string | null>(null);

  // Fetch homes
  const loadHomes = useCallback(async () => {
    try {
      const fetchedHomes = await fetchHomes();
      console.log("Fetched homes:", fetchedHomes);
      setHomes(fetchedHomes);
    } catch (error) {
      console.error("Failed to fetch homes:", error);
    }
  }, []);

  // Refetch homes whenever the screen is focused
  useFocusEffect(
      useCallback(() => {
        loadHomes();
      }, [loadHomes])
  );

  const handleAddHome = async (name: string, address: string) => {
    try {
      await createHome(name, address);
      setAddHomeModalVisible(false);
      await loadHomes();
    } catch (error) {
      console.error("Failed to add home:", error);
    }
  };

  const handleAddDevice = async (name: string, deviceType: string, description: string, hardwareId: string) => {
    if (selectedHomeUuid) {
      try {
        await deviceService.createDeviceInHome(selectedHomeUuid, name, deviceType, description, hardwareId);
        setAddDeviceModalVisible(false);
        await loadHomes();
      } catch (error) {
        console.error("Failed to add device:", error);
      }
    }
  };

  const handleOpenSettings = (homeUuid: string) => {
    router.push({ 
      pathname: "/(home)/[id]/settings",
      params: { id: homeUuid }
    });
  };

  return (
      <GradientBg theme="dark">
        {/* Add Home Modal */}
        <CustomModal
            visible={isAddHomeModalVisible}
            title="Add Home"
            onClose={() => setAddHomeModalVisible(false)}
        >
          <AddHomeForm onSubmit={handleAddHome} visible={isAddHomeModalVisible} />
        </CustomModal>

        {/* Add Device Modal */}
        <CustomModal
            visible={isAddDeviceModalVisible}
            title="Add Device"
            onClose={() => setAddDeviceModalVisible(false)}
        >
          <AddDeviceForm onSubmit={handleAddDevice} visible={isAddDeviceModalVisible} />
        </CustomModal>


        {homes.length > 0 ? (
            <ScrollView contentContainerStyle={styles.scrollContainer}>
              {homes.map((home) => (
                  <DropdownListItem
                      key={home.uuid}
                      title={home.name}
                      startsOpen={true}
                      settingsAction={() => handleOpenSettings(home.uuid)}
                  >
                    {home.devices.map((device) => (
                        <DeviceCard
                            key={device.uuid}
                            imageUri={require('@/assets/images/devices/door-lock.png')}
                            title={device.name}
                            description={device.description}
                            status="ok"
                        />
                    ))}
                    <TouchableOpacity
                        style={styles.addDeviceButton}
                        onPress={() => {
                          setSelectedHomeUuid(home.uuid);
                          setAddDeviceModalVisible(true);
                        }}
                    >
                      <Ionicons name="add" size={20} color="#fff" />
                    </TouchableOpacity>
                  </DropdownListItem>
              ))}
            </ScrollView>
        ) : (
            <View style={globalStyles.container}>
              <Image
                  source={require('@/assets/images/header-images/security-devices.png')}
                  style={styles.image}
              />
              <Text style={styles.message}>No security devices found.</Text>
            </View>
        )}

        {/* Add Home Button (properly placed) */}
        <TouchableOpacity
            style={styles.addHomeButton}
            onPress={() => setAddHomeModalVisible(true)}
        >
          <Ionicons name="add" size={16} color="#fff" />
          <Text style={styles.addHomeButtonText}>Add Home</Text>
        </TouchableOpacity>
      </GradientBg>
  );
}

const styles = StyleSheet.create({
  scrollContainer: {
    padding: 10,
    flexGrow: 1,
    justifyContent: 'flex-start',
    paddingTop: 60,
  },
  image: {
    width: 200,
    height: 200,
    marginBottom: 30,
  },
  message: {
    fontSize: 16,
    color: '#fff',
    marginBottom: 20,
    textAlign: 'center',
  },
  addDeviceButton: {
    alignSelf: 'flex-end',
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#1f2937',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.5,
    shadowRadius: 4,
    elevation: 5,
  },
  addDeviceText: {
    display: 'none',
  },
  addHomeButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    alignSelf: 'center',
    backgroundColor: '#1f2937',
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.5,
    shadowRadius: 4,
    elevation: 5,
    marginTop: 20,
    marginBottom: 20,
  },
  addHomeButtonText: {
    marginLeft: 8,
    fontSize: 16,
    color: '#fff',
  },
  cardContainer: {
    width: Dimensions.get('window').width - 20,
    alignSelf: 'center',
    marginBottom: 10,
    borderRadius: 10,
    backgroundColor: Colors.dark.background,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 5,
  },
});
