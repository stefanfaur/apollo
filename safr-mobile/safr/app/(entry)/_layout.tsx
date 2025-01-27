import { Stack } from "expo-router";

export default function HomeLayout() {
    return (
        <Stack
            screenOptions={{
                headerTransparent: true,
                headerTintColor: "#fff",
                headerTitleStyle: {
                    fontWeight: "600",
                    fontSize: 18,
                },
                headerBackTitle: "",
                headerLargeTitle: false,
                headerStyle: {
                    backgroundColor: 'transparent',
                },
                headerShadowVisible: false,
            }}
        >
            <Stack.Screen
                name="index"
                options={{
                    headerShown: false
                }}
            />
            <Stack.Screen
                name="login"
                options={{
                    title: "Login",
                    headerShown: false
                }}
            />
            <Stack.Screen
                name="signup"
                options={{
                    title: "Sign Up",
                    headerShown: false
                }}
            />
        </Stack>
    );
}