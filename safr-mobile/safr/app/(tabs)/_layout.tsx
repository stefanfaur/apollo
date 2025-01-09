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
                        case "notifications":
                            iconName = focused ? "notifications" : "notifications-outline";
                            break;
                        case "settings":
                            iconName = focused ? "settings" : "settings-outline";
                            break;
                        default:
                            iconName = "ellipse"; // fallback icon
                    }

                    // @ts-ignore no clue why this is throwing an error
                    // nevertheless, it works just fine
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
            <Tabs.Screen name="notifications" options={{ title: "Events" }} />
            {/* Settings Tab points to nested settings navigator */}
            <Tabs.Screen name="settings" options={{ title: "Settings" }} />
        </Tabs>
    );
}
