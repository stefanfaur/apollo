import React, { useState } from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import GradientBg from "@/components/ui/gradient-bg";
import ApolloTextInput from "@/components/ui/apollo-text-input";
import ApolloButton from "@/components/ui/apollo-button";
import globalStyles from "@/constants/global-styles";

export default function SignupScreem() {
  const [username, setUsername] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  return (
      <GradientBg theme="dark">
        <View style={globalStyles.container}>
          {/* Top Image */}
          <Image
              source={require('@/assets/images/header-images/register.png')} // Update this path based on your actual image
              style={styles.image}
          />

          {/* Title */}
          <Text style={styles.title}>Register</Text>

          {/* Inputs */}
          <View style={styles.inputContainer}>
            <ApolloTextInput
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
                theme="dark"
            />
            <ApolloTextInput
                placeholder="Name"
                value={name}
                onChangeText={setName}
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

          {/* Submit Button */}
          <ApolloButton title="Submit" onPress={() => console.log('Register')} theme="dark" />
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
    width: '80%', // Maintain consistency
    marginBottom: 20,
  },
});
