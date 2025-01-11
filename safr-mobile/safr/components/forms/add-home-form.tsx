import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, StyleSheet, Alert } from 'react-native';
import ApolloButton from '@/components/ui/apollo-button';

interface AddHomeFormProps {
    onSubmit: (name: string, address: string) => void;
    visible: boolean;
}

const AddHomeForm: React.FC<AddHomeFormProps> = ({ onSubmit, visible }) => {
    const [name, setName] = useState('');
    const [address, setAddress] = useState('');

    // reset state when modal becomes visible
    useEffect(() => {
        if (visible) {
            setName('');
            setAddress('');
        }
    }, [visible]);

    const handleSubmit = () => {
        if (!name.trim() || !address.trim()) {
            Alert.alert('Validation Error', 'Please fill in all fields.');
            return;
        }
        onSubmit(name.trim(), address.trim());
    };

    return (
        <View style={styles.container}>
            <Text style={styles.label}>Home Name</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter home name"
                value={name}
                onChangeText={setName}
            />
            <Text style={styles.label}>Address</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter address"
                value={address}
                onChangeText={setAddress}
            />
            <ApolloButton title="Add Home" onPress={handleSubmit} />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        width: '100%',
    },
    label: {
        fontSize: 14,
        color: '#fff',
        marginBottom: 5,
    },
    input: {
        backgroundColor: '#1f2937',
        color: '#fff',
        padding: 10,
        borderRadius: 5,
        marginBottom: 15,
    },
});

export default AddHomeForm;
