import React, { useState, useCallback, useEffect } from 'react';
import {
  Box,
  Button,
  Paper,
  Typography,
  Card,
  CardMedia,
  Grid,
  CircularProgress,
  Alert,
  Snackbar,
  Chip,
  Switch,
  FormControlLabel,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import { useDevices } from '../context/DeviceContext';
import MQTTService from '../services/mqtt';
import { uploadMedia, loadSampleImages } from '../services/mediaService';
import { SensorEventType } from '../types';

type NotificationType = {
  title: string;
  message: string;
  requiresMedia: boolean;
};

const NOTIFICATION_TYPES: NotificationType[] = [
  { title: 'Door Unlocked', message: 'Door has been unlocked', requiresMedia: false },
  { title: 'Handle Tried', message: 'Someone tried the door handle', requiresMedia: true },
  { title: 'Movement Detected', message: 'Movement detected near the door', requiresMedia: true },
  { title: 'Successful Entry', message: 'Successful entry recorded', requiresMedia: true },
];

const SENSOR_EVENTS = [
  { type: SensorEventType.MOTION_DETECTED, description: 'Motion detected', requiresMedia: true },
  { type: SensorEventType.DOOR_OPENED, description: 'Door opened (Frame)', requiresMedia: true },
  { type: SensorEventType.DOOR_OPENED_2, description: 'Door opened (Handle)', requiresMedia: true },
  { type: SensorEventType.DOOR_OPENED_UNAUTH, description: 'Door opened (Unauthorized)', requiresMedia: true },
  { type: SensorEventType.FINGERPRINT_FAILURE, description: 'Fingerprint authentication failed', requiresMedia: false },
];

const DeviceControl: React.FC = () => {
  const { selectedDevice, updateDeviceLastMessage } = useDevices();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [sampleMedia, setSampleMedia] = useState<File[]>([]);
  const [mqttStatus, setMqttStatus] = useState<'connected' | 'disconnected'>('disconnected');
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [snackbarSeverity, setSnackbarSeverity] = useState<'success' | 'error' | 'warning'>('success');
  const [autoSensorMode, setAutoSensorMode] = useState(false);

  // Monitor MQTT connection status
  useEffect(() => {
    const mqtt = MQTTService.getInstance();
    const client = (mqtt as any).client; // TypeScript workaround to access client
    
    const checkConnection = () => {
      const isConnected = client?.connected;
      if (isConnected !== (mqttStatus === 'connected')) {
        setMqttStatus(isConnected ? 'connected' : 'disconnected');
        setSnackbarMessage(isConnected ? 'MQTT Connected' : 'MQTT Disconnected');
        setSnackbarSeverity(isConnected ? 'success' : 'error');
        setSnackbarOpen(true);
      }
    };

    // Initial check
    checkConnection();

    // Set up event listeners
    client?.on('connect', checkConnection);
    client?.on('close', () => setMqttStatus('disconnected'));
    client?.on('offline', () => setMqttStatus('disconnected'));
    client?.on('error', () => setMqttStatus('disconnected'));

    // Periodic check every 5 seconds
    const interval = setInterval(checkConnection, 5000);

    return () => {
      clearInterval(interval);
      client?.removeListener('connect', checkConnection);
      client?.removeListener('close', checkConnection);
      client?.removeListener('offline', checkConnection);
      client?.removeListener('error', checkConnection);
    };
  }, [mqttStatus]);

  const loadImages = useCallback(async () => {
    try {
      const images = await loadSampleImages();
      setSampleMedia(images);
    } catch (error) {
      showNotification('Failed to load sample images', 'error');
    }
  }, []);

  useEffect(() => {
    loadImages();
  }, [loadImages]);

  const showNotification = (message: string, severity: 'success' | 'error' | 'warning') => {
    setSnackbarMessage(message);
    setSnackbarSeverity(severity);
    setSnackbarOpen(true);
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      setSelectedFile(file);
      showNotification('Media file selected successfully', 'success');
    }
  };

  const sendNotification = async (type: NotificationType) => {
    if (!selectedDevice) return;
    
    if (mqttStatus !== 'connected') {
      showNotification('MQTT not connected. Please wait for connection.', 'error');
      return;
    }

    setIsLoading(true);
    try {
      let mediaUrl = '';
      if (selectedFile) {
        try {
          mediaUrl = await uploadMedia(selectedFile);
          showNotification('Media uploaded successfully', 'success');
        } catch (error) {
          showNotification('Failed to upload media. Please try again.', 'error');
          setIsLoading(false);
          return;
        }
      } else if (type.requiresMedia) {
        showNotification('This event type recommends media, but proceeding without it.', 'warning');
      }

      const mqtt = MQTTService.getInstance();
      mqtt.publishNotification({
        hardwareId: selectedDevice.id,
        title: type.title,
        message: type.message,
        mediaUrl: mediaUrl || undefined,
      });

      updateDeviceLastMessage(selectedDevice.id);
      setSelectedFile(null);
      showNotification(`${type.title} notification sent successfully`, 'success');
    } catch (error) {
      console.error('Error sending notification:', error);
      showNotification('Failed to send notification. Please try again.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSampleImageSelect = (file: File) => {
    setSelectedFile(file);
    showNotification('Sample image selected', 'success');
  };

  const sendSensorEvent = async (sensorEvent: typeof SENSOR_EVENTS[0]) => {
    if (!selectedDevice) return;
    
    if (mqttStatus !== 'connected') {
      showNotification('MQTT not connected. Please wait for connection.', 'error');
      return;
    }

    setIsLoading(true);
    try {
      let mediaUrl = '';
      if (selectedFile) {
        try {
          mediaUrl = await uploadMedia(selectedFile);
          showNotification('Media uploaded successfully', 'success');
        } catch (error) {
          showNotification('Failed to upload media. Please try again.', 'error');
          setIsLoading(false);
          return;
        }
      } else if (sensorEvent.requiresMedia) {
        showNotification('This sensor event recommends media, but proceeding without it.', 'warning');
      }

      const mqtt = MQTTService.getInstance();
      console.log("MEDIA URL", mediaUrl);
      mqtt.publishSensorNotification({
        hardwareId: selectedDevice.id,
        title: sensorEvent.description,
        message: sensorEvent.description,
        mediaUrl: mediaUrl || undefined,
        timestamp: Date.now().toString(),
      });

      updateDeviceLastMessage(selectedDevice.id);
      setSelectedFile(null);
      showNotification(`${sensorEvent.description} event sent successfully`, 'success');
    } catch (error) {
      console.error('Error sending sensor event:', error);
      showNotification('Failed to send sensor event. Please try again.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // Auto sensor simulation
  useEffect(() => {
    if (!autoSensorMode || !selectedDevice || mqttStatus !== 'connected') return;

    const interval = setInterval(() => {
      // Randomly trigger sensor events (10% chance per check)
      if (Math.random() < 0.1) {
        const randomEvent = SENSOR_EVENTS[Math.floor(Math.random() * SENSOR_EVENTS.length)];
        sendSensorEvent(randomEvent);
      }
    }, 5000); // Check every 5 seconds

    return () => clearInterval(interval);
  }, [autoSensorMode, selectedDevice, mqttStatus]);

  // Monitor enroll status changes for notifications
  useEffect(() => {
    if (selectedDevice?.enrollStatus === 'in_progress') {
      showNotification('Fingerprint enrollment in progress...', 'warning');
    } else if (selectedDevice?.enrollStatus === 'success') {
      showNotification('Fingerprint enrollment successful!', 'success');
    } else if (selectedDevice?.enrollStatus === 'failure') {
      showNotification('Fingerprint enrollment failed!', 'error');
    }
  }, [selectedDevice?.enrollStatus]);

  if (!selectedDevice) {
    return (
      <Paper sx={{ p: 2, height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography>Select a device to control</Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, height: '100%', overflow: 'auto' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h6">
          Device Control - {selectedDevice.id}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {mqttStatus === 'connected' ? (
            <Alert
              icon={<CheckCircleIcon fontSize="small" />}
              severity="success"
              sx={{ py: 0 }}
            >
              MQTT Connected
            </Alert>
          ) : (
            <Alert
              icon={<ErrorIcon fontSize="small" />}
              severity="error"
              sx={{ py: 0 }}
            >
              MQTT Disconnected
            </Alert>
          )}
          {selectedDevice.lockState === 'unlocked' ? (
            <Chip label="Unlocked" color="warning" size="small" />
          ) : (
            <Chip label="Locked" color="success" size="small" />
          )}
          {selectedDevice.enrollStatus && selectedDevice.enrollStatus !== 'idle' && (
            <Chip label={`Enroll: ${selectedDevice.enrollStatus}`} color="info" size="small" />
          )}
        </Box>
      </Box>

      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 1 }}>
          Quick Actions
        </Typography>
        <Grid container spacing={2}>
          {NOTIFICATION_TYPES.map((type) => (
            <Grid item xs={12} sm={6} key={type.title}>
              <Button
                variant="contained"
                fullWidth
                onClick={() => sendNotification(type)}
                disabled={mqttStatus !== 'connected'}
              >
                {type.title}
              </Button>
            </Grid>
          ))}
        </Grid>
      </Box>

      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 1 }}>
          Sensor Events
        </Typography>
        <Grid container spacing={2}>
          {SENSOR_EVENTS.map((event) => (
            <Grid item xs={12} sm={6} key={event.type}>
              <Button
                variant="outlined"
                fullWidth
                onClick={() => sendSensorEvent(event)}
                disabled={mqttStatus !== 'connected'}
              >
                {event.description}
              </Button>
            </Grid>
          ))}
        </Grid>
      </Box>

      <Box sx={{ mb: 3 }}>
        <FormControlLabel
          control={
            <Switch
              checked={autoSensorMode}
              onChange={(e) => setAutoSensorMode(e.target.checked)}
              disabled={mqttStatus !== 'connected'}
            />
          }
          label="Auto Sensor Simulation"
        />
        <Typography variant="caption" display="block" color="text.secondary">
          Randomly triggers sensor events every 5 seconds when enabled
        </Typography>
      </Box>

      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 1 }}>
          Media Selection
        </Typography>
        <input
          type="file"
          accept="image/*,video/*"
          onChange={handleFileChange}
          style={{ display: 'none' }}
          id="media-upload"
        />
        <label htmlFor="media-upload">
          <Button variant="outlined" component="span" fullWidth sx={{ mb: 2 }}>
            Upload Media
          </Button>
        </label>

        <Typography variant="subtitle2" sx={{ mb: 1 }}>
          Sample Videos
        </Typography>
        <Grid container spacing={1}>
          {sampleMedia.map((file, index) => (
            <Grid item xs={4} key={index}>
              <Card 
                sx={{ 
                  cursor: 'pointer',
                  border: selectedFile === file ? '2px solid primary.main' : 'none',
                  position: 'relative'
                }}
                onClick={() => handleSampleImageSelect(file)}
              >
                {file.type.startsWith('video/') ? (
                  <Box sx={{ position: 'relative' }}>
                    <video
                      width="100%"
                      height="100"
                      style={{ objectFit: 'cover' }}
                      muted
                    >
                      <source src={URL.createObjectURL(file)} type={file.type} />
                    </video>
                    <Typography 
                      variant="caption" 
                      sx={{ 
                        position: 'absolute', 
                        bottom: 4, 
                        left: 4, 
                        color: 'white',
                        backgroundColor: 'rgba(0,0,0,0.7)',
                        px: 1,
                        borderRadius: 1
                      }}
                    >
                      {file.name.replace('.mp4', '')}
                    </Typography>
                  </Box>
                ) : (
                  <CardMedia
                    component="img"
                    height="100"
                    image={URL.createObjectURL(file)}
                    alt={`Sample ${index + 1}`}
                  />
                )}
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>

      {selectedFile && (
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle1">Selected Media Preview</Typography>
          <Card>
            <CardMedia
              component={selectedFile.type.startsWith('video/') ? 'video' : 'img'}
              height="400"
              image={URL.createObjectURL(selectedFile)}
              title="Selected media"
            />
          </Card>
        </Box>
      )}

      {isLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <CircularProgress />
        </Box>
      )}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setSnackbarOpen(false)}
          severity={snackbarSeverity}
          sx={{ width: '100%' }}
        >
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </Paper>
  );
};

export default DeviceControl;
