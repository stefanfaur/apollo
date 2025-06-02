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

    // Enhanced Bar Chart Data with gradients and more details
    const barChartData = [
        { 
            value: 40, 
            label: 'Living\nRoom',
            frontColor: '#4C9AFF',
            gradientColor: '#1E90FF',
            topLabelComponent: () => (
                <View style={styles.barTopLabel}>
                    <Text style={styles.barTopLabelText}>40%</Text>
                    <Text style={styles.barTopSubtext}>16 devices</Text>
                </View>
            ),
        },
        { 
            value: 20, 
            label: 'Kitchen',
            frontColor: '#FF6B9D',
            gradientColor: '#FF1493',
            topLabelComponent: () => (
                <View style={styles.barTopLabel}>
                    <Text style={styles.barTopLabelText}>20%</Text>
                    <Text style={styles.barTopSubtext}>8 devices</Text>
                </View>
            ),
        },
        { 
            value: 25, 
            label: 'Garage',
            frontColor: '#FFC947',
            gradientColor: '#FFB347',
            topLabelComponent: () => (
                <View style={styles.barTopLabel}>
                    <Text style={styles.barTopLabelText}>25%</Text>
                    <Text style={styles.barTopSubtext}>10 devices</Text>
                </View>
            ),
        },
        { 
            value: 15, 
            label: 'Front\nDoor',
            frontColor: '#4ECDC4',
            gradientColor: '#20B2AA',
            topLabelComponent: () => (
                <View style={styles.barTopLabel}>
                    <Text style={styles.barTopLabelText}>15%</Text>
                    <Text style={styles.barTopSubtext}>6 devices</Text>
                </View>
            ),
        },
    ];

    // Enhanced Pie Chart Data with more details
    const pieChartData = [
        { 
            value: 40, 
            color: '#4C9AFF', 
            focused: true,
            text: '40%',
            label: 'Living Room'
        },
        { 
            value: 20, 
            color: '#FF6B9D', 
            text: '20%',
            label: 'Kitchen'
        },
        { 
            value: 25, 
            color: '#FFC947', 
            text: '25%',
            label: 'Garage'
        },
        { 
            value: 15, 
            color: '#4ECDC4', 
            text: '15%',
            label: 'Front Door'
        },
    ];

    // Enhanced Line Chart Data with more details
    const lineChartData = [
        { value: 2, label: 'Mon', labelTextStyle: { color: '#d1d5db' } },
        { value: 4, label: 'Tue', labelTextStyle: { color: '#d1d5db' } },
        { value: 1, label: 'Wed', labelTextStyle: { color: '#d1d5db' } },
        { value: 5, label: 'Thu', labelTextStyle: { color: '#d1d5db' } },
        { value: 8, label: 'Fri', labelTextStyle: { color: '#d1d5db' } },
        { value: 3, label: 'Sat', labelTextStyle: { color: '#d1d5db' } },
        { value: 2, label: 'Sun', labelTextStyle: { color: '#d1d5db' } },
    ];

    // Enhanced Legend component for pie chart with flowing layout
    const renderLegend = () => {
        const legendData = [
            { label: 'Living Room', color: '#4C9AFF', value: '40%', count: '45 entries' },
            { label: 'Kitchen', color: '#FF6B9D', value: '20%', count: '23 entries' },
            { label: 'Garage', color: '#FFC947', value: '25%', count: '28 entries' },
            { label: 'Front Door', color: '#4ECDC4', value: '15%', count: '17 entries' },
        ];

        return (
            <View style={styles.legendContainer}>
                <View style={styles.legendGrid}>
                    {legendData.map((item, index) => (
                        <View key={index} style={styles.legendItem}>
                            <View style={[styles.legendDot, { backgroundColor: item.color }]} />
                            <View style={styles.legendTextContainer}>
                                <Text style={styles.legendLabel}>{item.label}</Text>
                                <Text style={styles.legendSubtext}>{item.count}</Text>
                            </View>
                            <Text style={styles.legendValue}>{item.value}</Text>
                        </View>
                    ))}
                </View>
            </View>
        );
    };

    return (
        <View style={styles.container}>
            {/* Fixed Header */}
            <View style={styles.fixedHeader}>
                <Text style={styles.title}>Dashboard</Text>
            </View>

            <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={false}>
                {/* Card: Successful Entries */}
                <View style={[styles.card, successfulEntries.percentage >= 80 ? styles.goodStat : styles.badStat]}>
                    <View style={styles.cardHeader}>
                        <Text style={styles.cardTitle}>Successful Entries</Text>
                        <Text style={styles.cardTimeframe}>Last 24h</Text>
                    </View>
                    <Text style={styles.cardStat}>{successfulEntries.value}</Text>
                    <Text style={styles.cardPercentage}>{successfulEntries.percentage}% Success Rate</Text>
                    <Text style={styles.cardDetails}>+12 from yesterday</Text>
                </View>

                {/* Card: Suspicious Activities */}
                <View style={[styles.card, suspiciousActivities.value <= 5 ? styles.goodStat : styles.badStat]}>
                    <View style={styles.cardHeader}>
                        <Text style={styles.cardTitle}>Suspicious Activities</Text>
                        <Text style={styles.cardTimeframe}>Last 7d</Text>
                    </View>
                    <Text style={styles.cardStat}>{suspiciousActivities.value}</Text>
                    <Text style={styles.cardPercentage}>{suspiciousActivities.percentage}% Increase</Text>
                    <Text style={styles.cardDetails}>3 failed attempts detected</Text>
                </View>

                {/* Card: Most Accessed Door */}
                <View style={styles.card}>
                    <View style={styles.cardHeader}>
                        <Text style={styles.cardTitle}>Most Accessed Door</Text>
                        <Text style={styles.cardTimeframe}>Today</Text>
                    </View>
                    <Text style={styles.cardStat}>{mostAccessedDoor}</Text>
                    <Text style={styles.cardDetails}>34 entries • 89% success rate</Text>
                </View>

                {/* Card: Most Unauthorized Entry Door */}
                <View style={styles.card}>
                    <View style={styles.cardHeader}>
                        <Text style={styles.cardTitle}>Security Alert Zone</Text>
                        <Text style={styles.cardTimeframe}>Active</Text>
                    </View>
                    <Text style={styles.cardStat}>{mostUnauthorizedEntryDoor}</Text>
                    <Text style={styles.cardDetails}>5 unauthorized attempts • High priority</Text>
                </View>

                {/* Enhanced Bar Chart: Guest Device Usage */}
                <View style={styles.chartCard}>
                    <View style={styles.chartHeader}>
                        <Text style={styles.chartTitle}>Device Usage by Location</Text>
                        <Text style={styles.chartSubtitle}>Active smart devices distribution</Text>
                        <Text style={styles.chartMeta}>Total: 40 devices • Last updated: 2min ago</Text>
                    </View>
                    <View style={styles.chartWrapper}>
                        <BarChart
                            data={barChartData}
                            width={screenWidth - 140}
                            height={280}
                            barWidth={45}
                            barBorderRadius={16}
                            spacing={20}
                            initialSpacing={15}
                            frontColor={'#4C9AFF'}
                            showGradient={true}
                            yAxisThickness={1}
                            yAxisColor={'#4a5568'}
                            xAxisThickness={1}
                            xAxisColor={'#4a5568'}
                            hideRules={false}
                            rulesColor={'rgba(74, 85, 104, 0.3)'}
                            rulesType={'solid'}
                            yAxisTextStyle={{ color: '#d1d5db', fontSize: 11 }}
                            xAxisLabelTextStyle={{ 
                                color: '#d1d5db', 
                                fontSize: 11, 
                                fontWeight: '600',
                            }}
                            noOfSections={5}
                            maxValue={50}
                            backgroundColor={'transparent'}
                            isAnimated={true}
                            animationDuration={1000}
                            showValuesAsTopLabel={true}
                            topLabelTextStyle={styles.barTopLabelText}
                        />
                    </View>
                    <View style={styles.chartFooter}>
                        <Text style={styles.chartFooterText}>💡 Living Room has highest device activity</Text>
                    </View>
                </View>

                {/* Enhanced Pie Chart: Door Access Distribution */}
                <View style={styles.chartCard}>
                    <View style={styles.chartHeader}>
                        <Text style={styles.chartTitle}>Door Access Distribution</Text>
                        <Text style={styles.chartSubtitle}>Entry points usage patterns</Text>
                        <Text style={styles.chartMeta}>Total entries: 113 • Peak time: 8-10 AM</Text>
                    </View>
                    <View style={styles.pieChartContainer}>
                        <PieChart
                            data={pieChartData}
                            radius={100}
                            innerRadius={60}
                            strokeColor={'#2d3748'}
                            strokeWidth={2}
                            backgroundColor={'transparent'}
                            isAnimated={true}
                            animationDuration={1200}
                            focusOnPress={true}
                            showText={true}
                            textColor={'#ffffff'}
                            textSize={12}
                            showTextBackground={true}
                            textBackgroundColor={'rgba(0,0,0,0.1)'}
                            textBackgroundRadius={20}
                        />
                        {renderLegend()}
                    </View>
                    <View style={styles.chartFooter}>
                        <Text style={styles.chartFooterText}>🚪 Living Room most accessed entry point</Text>
                    </View>
                </View>

                {/* Enhanced Line Chart: Suspicious Activity Trend */}
                <View style={styles.chartCard}>
                    <View style={styles.chartHeader}>
                        <Text style={styles.chartTitle}>Security Incidents Trend</Text>
                        <Text style={styles.chartSubtitle}>Weekly suspicious activity analysis</Text>
                        <Text style={styles.chartMeta}>Avg: 3.6 incidents/day • Trend: ↗️ +15%</Text>
                    </View>
                    <View style={styles.chartWrapper}>
                        <LineChart
                            data={lineChartData}
                            width={screenWidth - 140}
                            height={280}
                            curved={true}
                            curvature={0.3}
                            color={'#FF6B9D'}
                            thickness={5}
                            startFillColor={'rgba(255, 107, 157, 0.3)'}
                            endFillColor={'rgba(255, 107, 157, 0.05)'}
                            startOpacity={0.9}
                            endOpacity={0.1}
                            areaChart={true}
                            hideDataPoints={false}
                            dataPointsColor={'#FF6B9D'}
                            dataPointsRadius={8}
                            dataPointsWidth={3}
                            dataPointsColor1={'#FFFFFF'}
                            dataPointsRadius1={4}
                            focusEnabled={true}
                            showStripOnFocus={true}
                            stripColor={'rgba(255, 107, 157, 0.5)'}
                            stripWidth={3}
                            yAxisThickness={1}
                            yAxisColor={'#4a5568'}
                            xAxisThickness={1}
                            xAxisColor={'#4a5568'}
                            hideRules={false}
                            rulesColor={'rgba(74, 85, 104, 0.3)'}
                            rulesType={'dashed'}
                            yAxisTextStyle={{ color: '#d1d5db', fontSize: 11 }}
                            xAxisLabelTextStyle={{ 
                                color: '#d1d5db', 
                                fontSize: 12, 
                                fontWeight: '600' 
                            }}
                            noOfSections={5}
                            maxValue={10}
                            initialSpacing={15}
                            spacing={35}
                            backgroundColor={'transparent'}
                            hideOrigin={false}
                            isAnimated={true}
                            animationDuration={1400}
                            showVerticalLines={true}
                            verticalLinesColor={'rgba(74, 85, 104, 0.2)'}
                        />
                    </View>
                    <View style={styles.chartFooter}>
                        <Text style={styles.chartFooterText}>⚠️ Friday shows highest security incidents</Text>
                    </View>
                </View>

                <View style={styles.bottomPadding} />
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#1f2937',
    },
    fixedHeader: {
        backgroundColor: '#2d3748',
        paddingTop: 60,
        paddingBottom: 10,
        paddingHorizontal: 20,
        borderBottomWidth: 1,
        borderBottomColor: '#4a5568',
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
        elevation: 5,
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#fff',
        textAlign: 'center',
    },
    subtitle: {
        fontSize: 14,
        color: '#d1d5db',
        textAlign: 'center',
        marginTop: 4,
        fontWeight: '500',
    },
    scrollContent: {
        flex: 1,
        padding: 20,
    },
    card: {
        backgroundColor: '#2d3748',
        borderRadius: 15,
        padding: 20,
        marginBottom: 15,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 8,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 10,
    },
    goodStat: {
        borderColor: '#4caf50',
        borderWidth: 2,
        shadowColor: '#4caf50',
    },
    badStat: {
        borderColor: '#f44336',
        borderWidth: 2,
        shadowColor: '#f44336',
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#fff',
    },
    cardTimeframe: {
        fontSize: 12,
        color: '#9ca3af',
        backgroundColor: 'rgba(156, 163, 175, 0.2)',
        paddingHorizontal: 8,
        paddingVertical: 3,
        borderRadius: 8,
    },
    cardStat: {
        fontSize: 32,
        fontWeight: 'bold',
        color: '#4bc0c0',
        marginVertical: 5,
    },
    cardPercentage: {
        fontSize: 16,
        color: '#d1d5db',
        marginBottom: 5,
    },
    cardDetails: {
        fontSize: 13,
        color: '#9ca3af',
        fontStyle: 'italic',
    },
    chartCard: {
        backgroundColor: '#2d3748',
        borderRadius: 20,
        padding: 20,
        marginBottom: 20,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 6 },
        shadowOpacity: 0.4,
        shadowRadius: 12,
        elevation: 10,
    },
    chartHeader: {
        marginBottom: 20,
    },
    chartTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#fff',
        textAlign: 'center',
    },
    chartSubtitle: {
        fontSize: 14,
        color: '#d1d5db',
        textAlign: 'center',
        marginTop: 4,
    },
    chartMeta: {
        fontSize: 12,
        color: '#9ca3af',
        textAlign: 'center',
        marginTop: 6,
        fontStyle: 'italic',
    },
    chartWrapper: {
        alignItems: 'center',
        paddingVertical: 10,
    },
    chartFooter: {
        marginTop: 15,
        paddingTop: 15,
        borderTopWidth: 1,
        borderTopColor: '#4a5568',
    },
    chartFooterText: {
        fontSize: 13,
        color: '#9ca3af',
        textAlign: 'center',
        fontStyle: 'italic',
    },
    barTopLabel: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        paddingHorizontal: 8,
        paddingVertical: 5,
        borderRadius: 8,
        marginBottom: 6,
        alignItems: 'center',
    },
    barTopLabelText: {
        color: '#585c72',
        fontSize: 12,
        fontWeight: 'bold',
        textAlign: 'center',
    },
    barTopSubtext: {
        color: '#6b7280',
        fontSize: 9,
        textAlign: 'center',
        marginTop: 1,
    },
    pieChartContainer: {
        alignItems: 'center',
    },
    centerLabelContainer: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    centerLabelTitle: {
        fontSize: 14,
        color: '#d1d5db',
        fontWeight: '600',
    },
    centerLabelValue: {
        fontSize: 26,
        color: '#fff',
        fontWeight: 'bold',
        marginTop: 2,
    },
    centerLabelSubtext: {
        fontSize: 11,
        color: '#9ca3af',
        marginTop: 1,
    },
    legendContainer: {
        marginTop: 20,
        paddingHorizontal: 10,
    },
    legendGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    legendItem: {
        flexDirection: 'row',
        alignItems: 'center',
        width: '48%',
        marginBottom: 12,
        paddingVertical: 10,
        paddingHorizontal: 12,
        backgroundColor: 'rgba(255, 255, 255, 0.05)',
        borderRadius: 12,
    },
    legendDot: {
        width: 14,
        height: 14,
        borderRadius: 7,
        marginRight: 10,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.3,
        shadowRadius: 2,
        elevation: 2,
    },
    legendTextContainer: {
        flex: 1,
    },
    legendLabel: {
        fontSize: 13,
        color: '#fff',
        fontWeight: '600',
    },
    legendSubtext: {
        fontSize: 10,
        color: '#9ca3af',
        marginTop: 1,
    },
    legendValue: {
        fontSize: 13,
        color: '#4bc0c0',
        fontWeight: 'bold',
    },
    bottomPadding: {
        height: 20,
    },
});
