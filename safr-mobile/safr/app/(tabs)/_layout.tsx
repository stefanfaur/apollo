import { Tabs } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Colors } from "@/constants/colors";

export default function TabLayout() {
    return (
        <Tabs
            screenOptions={({ route }) => ({
                tabBarIcon: ({ focused, color, size }) => {
                    let iconName: string;

                    switch (route.name) {
                        case "home":
                            iconName = focused ? "home" : "home-outline";
                            break;
                        case "dashboard":
                            iconName = focused ? "analytics" : "analytics-outline";
                            break;
                        case "devices":
                            iconName = focused ? "hardware-chip" : "hardware-chip-outline";
                            break;
                        case "users":
                            iconName = focused ? "people" : "people-outline";
                            break;
                        case "notifications":
                            iconName = focused ? "notifications" : "notifications-outline";
                            break;
                        case "settings":
                            iconName = focused ? "settings" : "settings-outline";
                            break;
                        default:
                            iconName = "ellipse"; // fallback icon
                    }

                    // @ts-ignore - no idea why this is throwing an error
                    // nevertheless, it works fine
                    return <Ionicons name={iconName} size={size} color={color} />;
                },
                tabBarActiveTintColor: Colors.dark.primary,
                tabBarInactiveTintColor: Colors.dark.icon,
                tabBarStyle: {
                    backgroundColor: Colors.dark.gradientEnd,
                    borderTopWidth: 0,
                    elevation: 10,
                },
                tabBarLabelStyle: {
                    fontSize: 12,
                },
                tabBarShowLabel: true,
                headerShown: false, // hide top header
            })}
        >
            <Tabs.Screen name="home" options={{ title: "Home" }} />
            <Tabs.Screen name="dashboard" options={{ title: "Dash" }} />
            <Tabs.Screen name="devices" options={{ title: "Devices" }} />
            <Tabs.Screen name="users" options={{ title: "Users" }} />
            <Tabs.Screen name="notifications" options={{ title: "Events" }} />
            <Tabs.Screen name="settings" options={{ title: "Settings" }} />
        </Tabs>
    );
}
