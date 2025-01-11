import React, { useState } from 'react';
import { View, Text, Image, StyleSheet, Alert } from 'react-native';
import GradientBg from '@/components/ui/gradient-bg';
import ApolloTextInput from '@/components/ui/apollo-text-input';
import ApolloButton from '@/components/ui/apollo-button';
import globalStyles from '@/constants/global-styles';
import {AuthService, RegisterRequest} from "@/services/auth/auth-service";

export default function SignupScreen(): JSX.Element {
  const [username, setUsername] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);

  const handleRegister = async () => {
    if (password !== confirmPassword) {
      Alert.alert('Error', 'Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      const requestData: RegisterRequest = { username, email, password };
      const data = await AuthService.register(requestData);
      Alert.alert('Registration Successful', data.message);
    } catch (error: any) {
      Alert.alert('Registration Failed', error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
      <GradientBg theme="dark">
        <View style={globalStyles.container}>
          <Image
              source={require('@/assets/images/header-images/register.png')}
              style={styles.image}
          />
          <Text style={styles.title}>Register</Text>
          <View style={styles.inputContainer}>
            <ApolloTextInput
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
                theme="dark"
            />
            <ApolloTextInput
                placeholder="Email Address"
                value={email}
                onChangeText={setEmail}
                keyboardType="email-address"
                theme="dark"
            />
            <ApolloTextInput
                placeholder="Password"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                theme="dark"
            />
            <ApolloTextInput
                placeholder="Confirm Password"
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                secureTextEntry
                theme="dark"
            />
          </View>
          <ApolloButton
              title={loading ? 'Loading...' : 'Submit'}
              onPress={handleRegister}
              theme="dark"
              disabled={loading}
          />
        </View>
      </GradientBg>
  );
}

const styles = StyleSheet.create({
  image: {
    width: 150,
    height: 150,
    marginBottom: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 30,
  },
  inputContainer: {
    width: '80%',
    marginBottom: 20,
  },
});
