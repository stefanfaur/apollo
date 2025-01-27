import { Stack, useLocalSearchParams } from 'expo-router';
import { Colors } from '@/constants/colors';

export default function IdLayout() {
  const { id } = useLocalSearchParams();

  return (
    <Stack
      screenOptions={{
        headerStyle: {
          backgroundColor: Colors.dark.background,
        },
        headerTitleStyle: {
          color: Colors.dark.text,
        },
        headerTintColor: Colors.dark.text,
      }}
    >
      <Stack.Screen
        name="guests"
        options={{
          headerShown: false
        }}
      />
      <Stack.Screen
        name="settings/index"
        options={{
          headerShown: false,
          title: 'Home Settings'
        }}
      />
    </Stack>
  );
}
