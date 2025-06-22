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
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import { useDevices } from '../context/DeviceContext';
import MQTTService from '../services/mqtt';
import { uploadMedia, loadSampleImages } from '../services/mediaService';

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

const DeviceControl: React.FC = () => {
  const { selectedDevice, updateDeviceLastMessage } = useDevices();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [sampleImages, setSampleImages] = useState<File[]>([]);
  const [mqttStatus, setMqttStatus] = useState<'connected' | 'disconnected'>('disconnected');
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [snackbarSeverity, setSnackbarSeverity] = useState<'success' | 'error'>('success');

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
      setSampleImages(images);
    } catch (error) {
      showNotification('Failed to load sample images', 'error');
    }
  }, []);

  useEffect(() => {
    loadImages();
  }, [loadImages]);

  const showNotification = (message: string, severity: 'success' | 'error') => {
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
      if (type.requiresMedia && selectedFile) {
        try {
          mediaUrl = await uploadMedia(selectedFile);
          showNotification('Media uploaded successfully', 'success');
        } catch (error) {
          showNotification('Failed to upload media. Please try again.', 'error');
          setIsLoading(false);
          return;
        }
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
                disabled={type.requiresMedia && !selectedFile || mqttStatus !== 'connected'}
              >
                {type.title}
              </Button>
            </Grid>
          ))}
        </Grid>
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
          Sample Images
        </Typography>
        <Grid container spacing={1}>
          {sampleImages.map((file, index) => (
            <Grid item xs={4} key={index}>
              <Card 
                sx={{ 
                  cursor: 'pointer',
                  border: selectedFile === file ? '2px solid primary.main' : 'none'
                }}
                onClick={() => handleSampleImageSelect(file)}
              >
                <CardMedia
                  component="img"
                  height="100"
                  image={URL.createObjectURL(file)}
                  alt={`Sample ${index + 1}`}
                />
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
