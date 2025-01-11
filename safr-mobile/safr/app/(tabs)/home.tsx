import React, {useEffect, useState} from 'react';
import { View, Text, Image, StyleSheet, TouchableOpacity, ScrollView, Dimensions } from 'react-native';
import GradientBg from '@/components/ui/gradient-bg';
import { Ionicons } from '@expo/vector-icons';
import globalStyles from '@/constants/global-styles';
import { Colors } from '@/constants/colors';
import DeviceCard from '@/components/ui/device-card';
import DropdownListItem from '@/components/ui/dropdown-list-item';
import {getToken} from "@/utils/secureStore";

export default function HomeScreen() {
  const [hasAnyHomes, setHasAnyHomes] = useState(false);

  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const fetchToken = async () => {
      const storedToken = await getToken();
      console.log("Token is:", storedToken);
      setToken(storedToken);
    };

    fetchToken();
  }, []);

  return (
      <GradientBg theme="dark">
        {hasAnyHomes ? (
            <ScrollView contentContainerStyle={styles.scrollContainer}>
              <DropdownListItem title="Timisoara Apartment" settingsAction={() => console.log('settings1')} startsOpen={true}>
                <DeviceCard
                    imageUri={require('@/assets/images/devices/door-lock.png')}
                    title="Front Door"
                    description="Three-Way Auth"
                    status="warn"
                />
              </DropdownListItem>
              <DropdownListItem title="Dumbravita Apartment" settingsAction={() => console.log('settings2')}>
                <DeviceCard
                    imageUri={require('@/assets/images/devices/door-lock.png')}
                    title="Back Door"
                    description="Face only Auth"
                    status="ok"
                />
              </DropdownListItem>
            </ScrollView>
        ) : (
            <View style={globalStyles.container}>
              {/* Top Image */}
              <Image
                  source={require('@/assets/images/header-images/security-devices.png')}
                  style={styles.image}
              />

              {/* Message */}
              <Text style={styles.message}>No security devices found.</Text>

              {/* Add Device Button */}
              <TouchableOpacity style={styles.addDeviceButton} onPress={() => setHasAnyHomes(true)}>
                <Ionicons name="add" size={20} color="#fff" />
                <Text style={styles.addDeviceText}>Add Device</Text>
              </TouchableOpacity>
            </View>
        )}
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
  },
  addDeviceButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#1f2937',
    paddingVertical: 10,
    paddingHorizontal: 15,
    borderRadius: 10,
    marginTop: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.5,
    shadowRadius: 4,
    elevation: 5,
  },
  addDeviceText: {
    marginLeft: 10,
    fontSize: 16,
    color: '#fff',
  },
  cardContainer: {
    width: Dimensions.get('window').width - 20, // Full screen width minus padding
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
