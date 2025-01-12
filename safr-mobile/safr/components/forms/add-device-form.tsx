import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, StyleSheet, Alert } from 'react-native';
import ApolloButton from '@/components/ui/apollo-button';

interface AddDeviceFormProps {
    onSubmit: (name: string, deviceType: string, description: string, hardwareId: string) => void;
    visible: boolean;
}

const AddDeviceForm: React.FC<AddDeviceFormProps> = ({ onSubmit, visible }) => {
    const [name, setName] = useState('');
    const [deviceType, setDeviceType] = useState('');
    const [description, setDescription] = useState('');
    const [hardwareId, setHardwareId] = useState('');

    // reset form fields when the modal becomes visible
    useEffect(() => {
        if (visible) {
            setName('');
            setDeviceType('');
            setDescription('');
            setHardwareId('');
        }
    }, [visible]);

    const handleSubmit = () => {
        if (!name.trim() || !hardwareId.trim()) {
            Alert.alert('Validation Error', 'Please fill in all required fields.');
            return;
        }
        onSubmit(name.trim(), deviceType.trim(), description.trim(), hardwareId.trim());
    };

    return (
        <View style={styles.container}>
            <Text style={styles.label}>Device Name</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter device name"
                value={name}
                onChangeText={setName}
                placeholderTextColor="#888"
            />
            {/*<Text style={styles.label}>Device Type (auto-filled after pairing)</Text>*/}
            {/*<TextInput*/}
            {/*    style={[styles.input, styles.inputDisabled]}*/}
            {/*    placeholder="Enter device type"*/}
            {/*    value={name}*/}
            {/*    onChangeText={setName}*/}
            {/*    placeholderTextColor="#6c6262"*/}
            {/*    editable={false}*/}
            {/*/>*/}
            <Text style={styles.label}>Description (Optional)</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter description"
                value={description}
                onChangeText={setDescription}
            />
            <Text style={styles.label}>Hardware ID</Text>
            <TextInput
                style={styles.input}
                placeholder="Enter hardware ID"
                value={hardwareId}
                onChangeText={setHardwareId}
            />
            <ApolloButton title="Add Device" onPress={handleSubmit} />
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
    labelDisabled: {
        fontSize: 14,
        color: '#6c6262',
        marginBottom: 5,
    },
    input: {
        backgroundColor: '#1f2937',
        color: '#fff',
        padding: 10,
        borderRadius: 5,
        marginBottom: 15,
    },
    inputDisabled: {
        backgroundColor: '#141c25',
    },
});

export default AddDeviceForm;
