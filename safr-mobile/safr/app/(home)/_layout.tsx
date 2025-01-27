import { Stack } from 'expo-router';
import { Colors } from '@/constants/colors';

export default function HomeLayout() {
  return (
    <Stack
      screenOptions={{
        headerShown: false,
        headerStyle: {
          backgroundColor: Colors.dark.background,
        },
        headerTitleStyle: {
          color: Colors.dark.text,
        },
        headerTintColor: Colors.dark.text,
      }}
    />
  );
}
