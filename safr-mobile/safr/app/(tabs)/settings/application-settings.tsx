import { View, Text, StyleSheet } from 'react-native';

export default function UsersScreen() {
  return (
    <View style={styles.container}>
      <Text>Application Settings</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});