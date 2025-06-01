import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { BarChart, PieChart, LineChart } from 'react-native-gifted-charts';
import { Dimensions } from 'react-native';

const screenWidth = Dimensions.get('window').width;

export default function DashboardScreen() {
    const successfulEntries = { value: 120, percentage: 90 };
    const suspiciousActivities = { value: 8, percentage: 10 };
    const mostAccessedDoor = 'Front Door';
    const mostUnauthorizedEntryDoor = 'Back Door';
    const guestUsage = [
        { name: 'Living Room', usage: 40 },
        { name: 'Kitchen', usage: 20 },
        { name: 'Garage', usage: 25 },
        { name: 'Front Door', usage: 15 },
    ];

    const barChartData = [
        { value: 40, label: 'Living Room', frontColor: '#ff6384' },
        { value: 20, label: 'Kitchen', frontColor: '#36a2eb' },
        { value: 25, label: 'Garage', frontColor: '#ffcd56' },
        { value: 15, label: 'Front Door', frontColor: '#4bc0c0' },
    ];

    const pieChartData = [
        { value: 40, color: '#ff6384', text: 'Living Room' },
        { value: 20, color: '#36a2eb', text: 'Kitchen' },
        { value: 25, color: '#ffcd56', text: 'Garage' },
        { value: 15, color: '#4bc0c0', text: 'Front Door' },
    ];

    const lineChartData = [
        { value: 2, label: 'Mon' },
        { value: 4, label: 'Tue' },
        { value: 1, label: 'Wed' },
        { value: 5, label: 'Thu' },
        { value: 8, label: 'Fri' },
        { value: 3, label: 'Sat' },
        { value: 2, label: 'Sun' },
    ];

    return (
        <ScrollView style={styles.container}>
            <Text style={styles.title}>Dashboard</Text>

            {/* Card: Successful Entries */}
            <View style={[styles.card, successfulEntries.percentage >= 80 ? styles.goodStat : styles.badStat]}>
                <Text style={styles.cardTitle}>Successful Entries</Text>
                <Text style={styles.cardStat}>{successfulEntries.value}</Text>
                <Text style={styles.cardPercentage}>{successfulEntries.percentage}%</Text>
            </View>

            {/* Card: Suspicious Activities */}
            <View style={[styles.card, suspiciousActivities.value <= 5 ? styles.goodStat : styles.badStat]}>
                <Text style={styles.cardTitle}>Suspicious Activities</Text>
                <Text style={styles.cardStat}>{suspiciousActivities.value}</Text>
                <Text style={styles.cardPercentage}>{suspiciousActivities.percentage}% Increase</Text>
            </View>

            {/* Card: Most Accessed Door */}
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Most Accessed Door</Text>
                <Text style={styles.cardStat}>{mostAccessedDoor}</Text>
            </View>

            {/* Card: Most Unauthorized Entry Door */}
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Most Unauthorized Entry Door</Text>
                <Text style={styles.cardStat}>{mostUnauthorizedEntryDoor}</Text>
            </View>

            {/* Bar Chart: Guests' Device Usage */}
            <View style={styles.chartCard}>
                <Text style={styles.chartTitle}>Guest Device Usage</Text>
                <BarChart
                    data={barChartData}
                    width={screenWidth - 100}
                    height={220}
                    backgroundColor="#1f2937"
                    barBorderRadius={4}
                    frontColor="#4bc0c0"
                    yAxisThickness={0}
                    xAxisThickness={0}
                    hideRules
                    yAxisTextStyle={{ color: '#fff', fontSize: 12 }}
                    xAxisLabelTextStyle={{ color: '#fff', fontSize: 10 }}
                    noOfSections={4}
                    maxValue={50}
                />
            </View>

            {/* Pie Chart: Door Access */}
            <View style={styles.chartCard}>
                <Text style={styles.chartTitle}>Door Access Distribution</Text>
                <PieChart
                    data={pieChartData}
                    radius={80}
                    showText
                    textColor="#fff"
                    textSize={12}
                    showTextBackground
                    textBackgroundColor="#1f2937"
                    textBackgroundRadius={8}
                    strokeColor="#2d3748"
                    strokeWidth={2}
                />
            </View>

            {/* Line Chart: Suspicious Activity Trend */}
            <View style={styles.chartCard}>
                <Text style={styles.chartTitle}>Suspicious Activity Trend</Text>
                <LineChart
                    data={lineChartData}
                    width={screenWidth - 100}
                    height={220}
                    backgroundColor="#1f2937"
                    color="#ffa726"
                    thickness={3}
                    dataPointsColor="#ffa726"
                    dataPointsRadius={6}
                    curved
                    yAxisThickness={0}
                    xAxisThickness={0}
                    hideRules
                    yAxisTextStyle={{ color: '#fff', fontSize: 12 }}
                    xAxisLabelTextStyle={{ color: '#fff', fontSize: 10 }}
                    noOfSections={4}
                    maxValue={10}
                />
            </View>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
        paddingTop: 60,
        backgroundColor: '#1f2937',
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#fff',
        marginBottom: 20,
        textAlign: 'center',
    },
    card: {
        backgroundColor: '#2d3748',
        borderRadius: 10,
        padding: 20,
        marginBottom: 15,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.8,
        shadowRadius: 4,
        elevation: 5,
    },
    goodStat: {
        borderColor: '#4caf50',
        borderWidth: 2,
    },
    badStat: {
        borderColor: '#f44336',
        borderWidth: 2,
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#fff',
        marginBottom: 5,
    },
    cardStat: {
        fontSize: 32,
        fontWeight: 'bold',
        color: '#4bc0c0',
    },
    cardPercentage: {
        fontSize: 16,
        color: '#d1d5db',
    },
    chartCard: {
        backgroundColor: '#2d3748',
        borderRadius: 16,
        paddingVertical: 20,
        marginBottom: 15,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.8,
        shadowRadius: 4,
        elevation: 5,
        overflow: 'visible',
    },
    chartTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#fff',
        marginBottom: 10,
        textAlign: 'left',
        marginLeft: 20,
    },
});
