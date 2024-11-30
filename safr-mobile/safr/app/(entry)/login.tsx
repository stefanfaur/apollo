import React, {useState} from 'react';
import {View, Text, Image, StyleSheet} from 'react-native';
import GradientBackground from "@/components/ui/gradient-background";
import ApolloTextInput from "@/components/ui/apollo-text-input";
import ApolloButton from "@/components/ui/apollo-button";

export default function LoginScreen() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    return (
        <GradientBackground theme="dark">
            <View style={styles.container}>
                {/* Top Image */}
                <Image
                    source={require('@/assets/images/header-images/login.png')}
                    style={styles.image}
                />

                {/* Title */}
                <Text style={styles.title}>Login</Text>

                {/* Inputs */}
                <View style={styles.inputContainer}>
                    <ApolloTextInput
                        placeholder="Username"
                        value={username}
                        onChangeText={setUsername}
                        theme="dark"
                    />
                    <ApolloTextInput
                        placeholder="Password"
                        value={password}
                        onChangeText={setPassword}
                        secureTextEntry
                        theme="dark"
                    />
                </View>

                {/* Submit Button */}
                <ApolloButton title="Submit" onPress={() => console.log('Login')} theme="dark"/>
            </View>
        </GradientBackground>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingTop: 60,
    },
    image: {
        width: 200,
        height: 200,
        marginBottom: 30,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#fff',
        marginBottom: 40,
    },
    inputContainer: {
        width: '80%',
    },
});
