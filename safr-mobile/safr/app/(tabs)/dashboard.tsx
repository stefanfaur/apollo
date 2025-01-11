import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { BarChart, PieChart, LineChart } from 'react-native-chart-kit';
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

    const barChartData = {
        labels: ['Living Room', 'Kitchen', 'Garage', 'Front Door'],
        datasets: [
            {
                data: [40, 20, 25, 15],
            },
        ],
    };

    const pieChartData = [
        { name: 'Living Room', population: 40, color: '#ff6384', legendFontColor: '#fff', legendFontSize: 12 },
        { name: 'Kitchen', population: 20, color: '#36a2eb', legendFontColor: '#fff', legendFontSize: 12 },
        { name: 'Garage', population: 25, color: '#ffcd56', legendFontColor: '#fff', legendFontSize: 12 },
        { name: 'Front Door', population: 15, color: '#4bc0c0', legendFontColor: '#fff', legendFontSize: 12 },
    ];

    const lineChartData = {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        datasets: [
            {
                data: [2, 4, 1, 5, 8, 3, 2],
                strokeWidth: 2, // Line thickness
            },
        ],
    };

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
                    width={screenWidth - 80} // Adjust width
                    height={220}
                    yAxisLabel=""
                    yAxisSuffix="%"
                    chartConfig={{
                        backgroundColor: '#1f2937',
                        backgroundGradientFrom: '#1f2937',
                        backgroundGradientTo: '#1f2937',
                        color: (opacity = 1) => `rgba(255, 255, 255, ${opacity})`,
                        labelColor: (opacity = 1) => `rgba(255, 255, 255, ${opacity})`,
                        style: { borderRadius: 16 },
                        propsForBackgroundLines: {
                            strokeWidth: 0,
                        },
                    }}
                    style={{
                        marginVertical: 8,
                        borderRadius: 16,
                        alignSelf: 'center',
                    }}
                />

            </View>

            {/* Pie Chart: Door Access */}
            <View style={styles.chartCard}>
                <Text style={styles.chartTitle}>Door Access Distribution</Text>
                <PieChart
                    data={pieChartData}
                    width={screenWidth - 40}
                    height={220}
                    chartConfig={{
                        color: (opacity = 1) => `rgba(255, 255, 255, ${opacity})`,
                    }}
                    accessor="population"
                    backgroundColor="transparent"
                    paddingLeft="15"
                    absolute
                    style={{ marginVertical: 8, borderRadius: 16, alignSelf: 'center' }}
                />
            </View>

            {/* Line Chart: Suspicious Activity Trend */}
            <View style={styles.chartCard}>
                <Text style={styles.chartTitle}>Suspicious Activity Trend</Text>
                <LineChart
                    data={lineChartData}
                    width={screenWidth - 80}
                    height={220}
                    chartConfig={{
                        backgroundColor: '#1f2937',
                        backgroundGradientFrom: '#1f2937',
                        backgroundGradientTo: '#1f2937',
                        color: (opacity = 1) => `rgba(255, 255, 255, ${opacity})`,
                        labelColor: (opacity = 1) => `rgba(255, 255, 255, ${opacity})`,
                        style: { borderRadius: 16 },
                        propsForDots: {
                            r: '6',
                            strokeWidth: '2',
                            stroke: '#ffa726',
                        },
                    }}
                    bezier
                    style={{ marginVertical: 8, borderRadius: 16, alignSelf: 'center' }}
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
