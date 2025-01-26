import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/colors';
import ApolloButton from './apollo-button';
import AddAdminModal from './add-admin-modal';
import { homeAccessService, AdminDTO } from '@/services/home-access-service';

interface AdminSectionProps {
  homeId: string;
}

const AdminSection: React.FC<AdminSectionProps> = ({ homeId }) => {
  const [admins, setAdmins] = useState<AdminDTO[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showAddAdmin, setShowAddAdmin] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [removingAdminId, setRemovingAdminId] = useState<string | null>(null);

  const loadAdmins = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const fetchedAdmins = await homeAccessService.getHomeAdmins(homeId);
      setAdmins(fetchedAdmins);
    } catch (err) {
      setError('Failed to load administrators');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadAdmins();
  }, [homeId]);

  const handleAddAdmin = async (email: string) => {
    try {
      await homeAccessService.addHomeAdmin(homeId, email);
      await loadAdmins();
      setShowAddAdmin(false);
    } catch (err) {
      throw err;
    }
  };

  const handleRemoveAdmin = async (adminId: string) => {
    try {
      setRemovingAdminId(adminId);
      setError(null);
      await homeAccessService.removeHomeAdmin(homeId, adminId);
      await loadAdmins();
    } catch (err) {
      setError('Failed to remove administrator');
      console.error(err);
    } finally {
      setRemovingAdminId(null);
    }
  };

  const renderAdminItem = ({ item }: { item: AdminDTO }) => (
    <View style={styles.adminItem}>
      <Text style={styles.adminEmail}>{item.email}</Text>
      <TouchableOpacity 
        onPress={() => handleRemoveAdmin(item.uuid)}
        style={styles.removeButton}
        disabled={removingAdminId === item.uuid}
      >
        {removingAdminId === item.uuid ? (
          <ActivityIndicator size="small" color={Colors.dark.text} />
        ) : (
          <Ionicons name="trash-outline" size={20} color={Colors.dark.text} />
        )}
      </TouchableOpacity>
    </View>
  );

  if (isLoading && !admins.length) {
    return (
      <View style={styles.centerContainer}>
        <ActivityIndicator size="large" color={Colors.dark.text} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.spacer} />
        <TouchableOpacity 
          onPress={() => setShowAddAdmin(true)}
          style={styles.addButton}
        >
          <Ionicons name="add-circle" size={24} color={Colors.dark.text} />
        </TouchableOpacity>
      </View>
      
      {admins.length > 0 ? (
        <FlatList
          data={admins}
          renderItem={renderAdminItem}
          keyExtractor={(item) => item.uuid}
          style={styles.list}
        />
      ) : !isLoading && (
        <View style={styles.emptyState}>
          <Text style={styles.emptyText}>No administrators added yet</Text>
        </View>
      )}

      {error && (
        <Text style={styles.errorText}>{error}</Text>
      )}
      
      <AddAdminModal
        visible={showAddAdmin}
        onClose={() => setShowAddAdmin(false)}
        onSubmit={handleAddAdmin}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    marginBottom: 20,
  },
  spacer: {
    flex: 1,
  },
  addButton: {
    padding: 4,
  },
  list: {
    flex: 1,
  },
  adminItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 12,
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 8,
    marginBottom: 8,
  },
  adminEmail: {
    color: Colors.dark.text,
    fontSize: 16,
  },
  removeButton: {
    padding: 4,
    minWidth: 28,
    alignItems: 'center',
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    color: Colors.dark.text,
    fontSize: 16,
    opacity: 0.7,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
    marginTop: 8,
    marginBottom: 8,
    textAlign: 'center',
  },
});

export default AdminSection;
