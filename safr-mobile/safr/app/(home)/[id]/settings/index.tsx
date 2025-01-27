import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Stack, useLocalSearchParams } from 'expo-router';
import { Colors } from '@/constants/colors';
import HomeSettings from '@/components/ui/home-settings';

export default function HomeSettingsPage() {
  const { id: homeId } = useLocalSearchParams();

  return (
    <View style={styles.container}>
      <Stack.Screen
        options={{
          headerShown: true,
          title: 'Home Settings',
          headerTitleStyle: {
            color: Colors.dark.text,
          },
          headerStyle: {
            backgroundColor: Colors.dark.background,
          },
          headerTintColor: Colors.dark.text,
        }}
      />
      <HomeSettings homeId={homeId as string} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.dark.background,
  }
});
