import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Colors } from '@/constants/colors';
import AdminSection from './admin-section';
import GuestSection from './guest-section';

type Tab = 'admins' | 'guests';

interface HomeSettingsProps {
  homeId: string;
}

const HomeSettings: React.FC<HomeSettingsProps> = ({
  homeId,
}) => {
  const [activeTab, setActiveTab] = useState<Tab>('admins');

  const renderTabContent = () => {
    switch (activeTab) {
      case 'admins':
        return <AdminSection homeId={homeId} />;
      case 'guests':
        return <GuestSection homeId={homeId} />;
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <View style={styles.tabs}>
          <TouchableOpacity 
            style={[
              styles.tab, 
              activeTab === 'admins' && styles.activeTab
            ]}
            onPress={() => setActiveTab('admins')}
          >
            <Text style={[
              styles.tabText,
              activeTab === 'admins' && styles.activeTabText
            ]}>Administrators</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[
              styles.tab, 
              activeTab === 'guests' && styles.activeTab
            ]}
            onPress={() => setActiveTab('guests')}
          >
            <Text style={[
              styles.tabText,
              activeTab === 'guests' && styles.activeTabText
            ]}>Guests</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.tabContent}>
          {renderTabContent()}
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.dark.background,
  },
  content: {
    flex: 1,
  },
  tabs: {
    flexDirection: 'row',
    marginBottom: 20,
    borderRadius: 8,
    backgroundColor: Colors.dark.buttonBackground,
    padding: 4,
    marginHorizontal: 16,
  },
  tab: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
    borderRadius: 6,
  },
  activeTab: {
    backgroundColor: Colors.dark.primary,
  },
  tabText: {
    color: Colors.dark.text,
    fontSize: 16,
    opacity: 0.7,
  },
  activeTabText: {
    fontWeight: 'bold',
    opacity: 1,
  },
  tabContent: {
    flex: 1,
    backgroundColor: Colors.dark.background,
    borderRadius: 8,
  },
});

export default HomeSettings;
