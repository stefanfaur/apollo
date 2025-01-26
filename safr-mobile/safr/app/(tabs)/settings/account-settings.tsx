import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Alert, TouchableOpacity } from 'react-native';
import GradientBg from '@/components/ui/gradient-bg';
import ApolloButton from '@/components/ui/apollo-button';
import { userService } from "@/services/user-service";
import { router } from "expo-router";
import {UserDTO} from "@/models/userDTO";

export default function AccountSettingsScreen() {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUserInfo = async () => {
      try {
        const userInfo = await userService.fetchUserInfo();
        setUser(userInfo);
      } catch (error) {
        console.error("Failed to fetch user info:", error);
        Alert.alert("Error", "Failed to load user info.");
      } finally {
        setLoading(false);
      }
    };

    loadUserInfo();
  }, []);

  const handleLogout = async () => {
    try {
      await userService.logoutUser();
      Alert.alert("Logout Successful", "You have been logged out.");
      router.replace("/(entry)"); // Redirect to entry screen
    } catch (error) {
      console.error("Failed to logout:", error);
      Alert.alert("Error", "Failed to log out.");
    }
  };

  if (loading) {
    return (
        <GradientBg>
          <View style={styles.loadingContainer}>
            <Text style={styles.loadingText}>Loading...</Text>
          </View>
        </GradientBg>
    );
  }

  return (
      <GradientBg>
        <View style={styles.container}>
          {/* User Info */}
          <View style={styles.userInfoContainer}>
            <Text style={styles.username}>{user?.username}</Text>
            <Text style={styles.email}>{user?.email}</Text>
          </View>

          {/* Logout Button */}
          <ApolloButton
              title="Logout"
              onPress={handleLogout}
              theme="dark"
              style={styles.logoutButton}
          />
        </View>
      </GradientBg>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 16,
    color: '#fff',
  },
  userInfoContainer: {
    marginBottom: 40,
    alignItems: 'center',
  },
  username: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 5,
  },
  email: {
    fontSize: 16,
    color: '#d1d5db',
  },
  logoutButton: {
    paddingVertical: 12,
    paddingHorizontal: 60,
    borderRadius: 25,
  },
});
