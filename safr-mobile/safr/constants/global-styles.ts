import { StyleSheet } from 'react-native';

const globalStyles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
        paddingHorizontal: 20,
        paddingTop: 150,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 30,
    },
    subtitle: {
        fontSize: 14,
        color: '#d1d5db',
    },
    linkText: {
        color: '#4f46e5',
        fontWeight: 'bold',
    },
});

export default globalStyles;
