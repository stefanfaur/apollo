import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Dimensions } from 'react-native';
import CustomModal from './custom-modal';
import { Colors } from '@/constants/colors';
import AdminSection from './admin-section';
import GuestSection from './guest-section';

type Tab = 'admins' | 'guests';

interface HomeSettingsModalProps {
  visible: boolean;
  onClose: () => void;
  homeId: string;
}

const HomeSettingsModal: React.FC<HomeSettingsModalProps> = ({
  visible,
  onClose,
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
    <CustomModal visible={visible} title="Home Settings" onClose={onClose}>
      <View style={styles.container}>
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
        <View style={styles.content}>
          {renderTabContent()}
        </View>
      </View>
    </CustomModal>
  );
};

const styles = StyleSheet.create({
  container: {
    height: Dimensions.get('window').height * 0.7,
    width: '100%',
  },
  tabs: {
    flexDirection: 'row',
    marginBottom: 20,
    borderRadius: 8,
    backgroundColor: Colors.dark.buttonBackground,
    padding: 4,
    marginHorizontal: 4,
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
  content: {
    flex: 1,
    backgroundColor: Colors.dark.background,
    borderRadius: 8,
    marginHorizontal: 4,
  },
});

export default HomeSettingsModal;
