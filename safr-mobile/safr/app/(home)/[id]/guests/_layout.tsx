import { Stack } from 'expo-router';
import { Colors } from '@/constants/colors';

export default function GuestsLayout() {
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
        name="add"
        options={{
          headerShown: false,
          title: 'Add Guest'
        }}
      />
      <Stack.Screen
        name="edit/[guestId]"
        options={{
          headerShown: false,
          title: 'Edit Guest'
        }}
      />
    </Stack>
  );
}
