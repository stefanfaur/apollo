import { Tabs } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Colors } from "@/constants/colors";

export default function SettingsTabLayout() {
    return (
        <Tabs
            screenOptions={({ route }) => ({
                tabBarIcon: ({ focused, color, size }) => {
                    let iconName: string;

                    switch (route.name) {
                        case "users-settings":
                            iconName = focused ? "people" : "people-outline";
                            break;
                        case "application-settings":
                            iconName = focused ? "settings" : "settings-outline";
                            break;
                        case "account-settings":
                            iconName = focused ? "person" : "person-outline";
                            break;
                        default:
                            iconName = "ellipse"; // fallback icon
                    }

                    // @ts-ignore
                    return <Ionicons name={iconName} size={size} color={color} />;
                },
                tabBarActiveTintColor: Colors.dark.primary,
                tabBarInactiveTintColor: Colors.dark.icon,
                tabBarStyle: {
                    backgroundColor: Colors.dark.gradientEnd,
                    borderTopWidth: 0,
                    elevation: 10,
                    height: 60,
                },
                tabBarLabelStyle: {
                    fontSize: 12,
                },
                tabBarShowLabel: true,
                headerShown: false,
            })}
        >
            <Tabs.Screen name="users-settings" options={{ title: "Users" }} />
            <Tabs.Screen name="application-settings" options={{ title: "Application" }} />
            <Tabs.Screen name="account-settings" options={{ title: "Account" }} />
        </Tabs>
    );
}
