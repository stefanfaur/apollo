import React from 'react';
import { View, Text, Image, StyleSheet, TouchableOpacity } from 'react-native';
import GradientBg from '@/components/ui/gradient-bg';
import { Ionicons } from '@expo/vector-icons';
import globalStyles from "@/constants/global-styles";

export default function HomeScreen() {
  return (
      <GradientBg theme="dark">
        <View style={globalStyles.container}>
          {/* Top Image */}
          <Image
              source={require('@/assets/images/header-images/security-devices.png')}
              style={styles.image}
          />

          {/* Message */}
          <Text style={styles.message}>No security devices found.</Text>

          {/* Add Device Button */}
          <TouchableOpacity style={styles.addDeviceButton} onPress={() => console.log('Add Device')}>
            <Ionicons name="add" size={20} color="#fff" />
            <Text style={styles.addDeviceText}>Add Device</Text>
          </TouchableOpacity>
        </View>
      </GradientBg>
  );
}

const styles = StyleSheet.create({
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
  },
  addDeviceText: {
    marginLeft: 10,
    fontSize: 16,
    color: '#fff',
  },
});
