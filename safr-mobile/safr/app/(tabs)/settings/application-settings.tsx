import React, { useState } from 'react';
import { View, Text, StyleSheet, Switch, TouchableOpacity } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import GradientBg from '@/components/ui/gradient-bg';
import { Colors } from '@/constants/colors';
import ApolloButton from "@/components/ui/apollo-button";

export default function UsersScreen() {
  const [settings, setSettings] = useState({
    theme: 'dark',
    language: 'en',
    notificationsEnabled: true,
  });

  const handleThemeToggle = () => {
    setSettings((prev) => ({
      ...prev,
      theme: prev.theme === 'dark' ? 'light' : 'dark',
    }));
  };

  const handleLanguageChange = (language: string) => {
    setSettings((prev) => ({ ...prev, language }));
  };

  const handleNotificationToggle = () => {
    setSettings((prev) => ({
      ...prev,
      notificationsEnabled: !prev.notificationsEnabled,
    }));
  };

  const handleSaveSettings = () => {
    console.log('Saved Settings:', settings);
    alert('Settings saved!');
  };

  return (
      <GradientBg>
        <View style={styles.container}>
          <Text style={styles.title}>Application Settings</Text>

          {/* Theme Setting */}
          <View style={[styles.settingItem, styles.elevatedBackground]}>
            <Text style={styles.settingLabel}>Theme</Text>
            <View style={styles.switchContainer}>
              <Text style={styles.settingValue}>
                {settings.theme === 'dark' ? 'Dark' : 'Light'}
              </Text>
              <Switch
                  value={settings.theme === 'dark'}
                  onValueChange={handleThemeToggle}
                  thumbColor={
                    settings.theme === 'dark'
                        ? Colors.dark.primary
                        : Colors.light.primary
                  }
              />
            </View>
          </View>

          {/* Language Setting */}
          <View style={[styles.settingItem, styles.elevatedBackground]}>
            <Text style={styles.settingLabel}>Language</Text>
            <Picker
                selectedValue={settings.language}
                onValueChange={handleLanguageChange}
                style={styles.picker}
            >
              <Picker.Item label="English" value="en" color={"white"} />
              <Picker.Item label="Spanish" value="es" color={"white"}/>
              <Picker.Item label="French" value="fr" color={"white"}/>
              <Picker.Item label="German" value="de" color={"white"}/>
            </Picker>
          </View>

          {/* Notifications Setting */}
          <View style={[styles.settingItem, styles.elevatedBackground]}>
            <Text style={styles.settingLabel}>Notifications</Text>
            <View style={styles.switchContainer}>
              <Text style={styles.settingValue}>
                {settings.notificationsEnabled ? 'Enabled' : 'Disabled'}
              </Text>
              <Switch
                  value={settings.notificationsEnabled}
                  onValueChange={handleNotificationToggle}
                  thumbColor={
                    settings.notificationsEnabled
                        ? Colors.dark.primary
                        : Colors.light.primary
                  }
              />
            </View>
          </View>

          {/* Save Button */}
          <ApolloButton
              title="Save Settings"
              onPress={handleSaveSettings}
              theme="dark"
              style={[styles.saveButton, styles.noShadow]}
          />

        </View>
      </GradientBg>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 60,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
    marginBottom: 30,
  },
  settingItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
    padding: 10,
    borderRadius: 10,
    elevation: 3,
  },
  elevatedBackground: {
    backgroundColor: '#1f2937',
    borderColor: '#2d3748',
    borderWidth: 1,
    shadowColor: '#ffffff',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  settingLabel: {
    fontSize: 16,
    color: '#fff',
    flex: 1,
  },
  settingValue: {
    fontSize: 14,
    color: '#d1d5db',
    marginRight: 10,
  },
  switchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  picker: {
    flex: 1,
    color: '#fff',
    backgroundColor: '#1f2937',
    borderRadius: 10,
  },
  saveButton: {
    paddingVertical: 12,
    paddingHorizontal: 30,
    borderRadius: 25,
    alignSelf: 'center',
    marginTop: 20,
  },
  noShadow: {
    shadowColor: '#ffffff',
    shadowOpacity: 0.2,
    shadowOffset: { width: 0, height: 0 },
    shadowRadius: 2,
    elevation: 0,
  },


});
