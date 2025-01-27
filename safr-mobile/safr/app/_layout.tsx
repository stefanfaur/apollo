import { Stack } from 'expo-router';
import { Colors } from '@/constants/colors';

export default function RootLayout() {
  return (
    <Stack>
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="(entry)" options={{ headerShown: false }} />
      <Stack.Screen 
        name="(home)"
        options={{ 
          headerShown: false,
        }}
      />
    </Stack>
  );
}
