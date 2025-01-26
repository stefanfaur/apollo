import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import CustomModal from './custom-modal';
import ApolloButton from './apollo-button';
import ApolloTextInput from './apollo-text-input';
import { Colors } from '@/constants/colors';
import { userService } from '@/services/user-service';
import debounce from 'lodash/debounce';
import {UserDTO} from "@/models/userDTO";

interface AddAdminModalProps {
  visible: boolean;
  onClose: () => void;
  onSubmit: (email: string) => Promise<void>;
}

const AddAdminModal: React.FC<AddAdminModalProps> = ({
  visible,
  onClose,
  onSubmit,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<UserDTO[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const searchUsers = async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      const results = await userService.searchUsers(query);
      setSearchResults(results);
    } catch (err) {
      setError('Failed to search users');
      setSearchResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Debounce the search to avoid too many API calls
  const debouncedSearch = useCallback(
    debounce((query: string) => searchUsers(query), 500),
    []
  );

  const handleSearchChange = (text: string) => {
    setSearchQuery(text);
    setSelectedUser(null);
    debouncedSearch(text);
  };

  const handleSelectUser = (user: UserDTO) => {
    setSelectedUser(user);
    setSearchQuery(user.email);
    setSearchResults([]);
  };

  const handleSubmit = async () => {
    if (!selectedUser) return;

    try {
      setError(null);
      setIsSubmitting(true);
      await onSubmit(selectedUser.email);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add admin');
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setSearchQuery('');
    setSearchResults([]);
    setSelectedUser(null);
    setError(null);
    onClose();
  };

  const renderUserItem = ({ item }: { item: UserDTO }) => (
    <TouchableOpacity
      style={styles.userItem}
      onPress={() => handleSelectUser(item)}
    >
      <Text style={styles.userEmail}>{item.email}</Text>
    </TouchableOpacity>
  );

  return (
    <CustomModal visible={visible} title="Add Administrator" onClose={handleClose}>
      <View style={styles.container}>
        <View style={styles.searchContainer}>
          <ApolloTextInput
            placeholder="Search by email"
            value={searchQuery}
            onChangeText={handleSearchChange}
            autoCapitalize="none"
            keyboardType="email-address"
          />
          {isLoading && (
            <ActivityIndicator
              size="small"
              color={Colors.dark.text}
              style={styles.searchSpinner}
            />
          )}
        </View>

        {searchResults.length > 0 && !selectedUser && (
          <FlatList
            data={searchResults}
            renderItem={renderUserItem}
            keyExtractor={(item) => item.uuid}
            style={styles.resultsList}
          />
        )}

        {error && <Text style={styles.errorText}>{error}</Text>}

        <ApolloButton
          title={isSubmitting ? "Adding..." : "Add Administrator"}
          onPress={handleSubmit}
          disabled={!selectedUser || isSubmitting}
          style={styles.submitButton}
        />
      </View>
    </CustomModal>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 16,
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  searchSpinner: {
    marginLeft: 8,
  },
  resultsList: {
    maxHeight: 200,
    marginTop: 8,
    borderRadius: 8,
    backgroundColor: Colors.dark.buttonBackground,
  },
  userItem: {
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: Colors.dark.background,
  },
  userEmail: {
    color: Colors.dark.text,
    fontSize: 16,
  },
  submitButton: {
    marginTop: 20,
  },
  errorText: {
    color: '#ff6b6b',
    fontSize: 14,
    marginTop: 8,
    textAlign: 'center',
  },
});

export default AddAdminModal;
