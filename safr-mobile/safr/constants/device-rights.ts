import { Ionicons } from '@expo/vector-icons';

export interface DeviceRight {
    id: string;
    label: string;
    icon: keyof typeof Ionicons.glyphMap;
}

export const DEVICE_RIGHTS: DeviceRight[] = [
    { 
        id: 'VIEW_IMAGES',
        label: 'View Images',
        icon: 'image-outline'
    },
    {
        id: 'VIEW_LIVE_STREAM',
        label: 'Live Stream',
        icon: 'videocam-outline'
    },
    {
        id: 'VIEW_NOTIFICATIONS',
        label: 'Notifications',
        icon: 'notifications-outline'
    },
    {
        id: 'UNLOCK_DOOR',
        label: 'Unlock',
        icon: 'lock-open-outline'
    },
    {
        id: 'VIEW_DEVICE_INFO',
        label: 'Info',
        icon: 'information-circle-outline'
    },
    {
        id: 'VIEW_DEVICE_SETTINGS',
        label: 'Settings',
        icon: 'settings-outline'
    }
];
