import React from 'react';
import { Box, Button, List, ListItem, ListItemText, IconButton, Switch, Paper, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useDevices } from '../context/DeviceContext';
import { Device } from '../types';

const DeviceList: React.FC = () => {
  const { 
    devices, 
    addDevice, 
    removeDevice, 
    toggleDeviceActive,
    selectedDevice,
    setSelectedDevice 
  } = useDevices();

  const handleAddDevice = () => {
    const id = `device-${Math.random().toString(36).substr(2, 9)}`;
    addDevice(id);
  };

  const renderDevice = (device: Device) => {
    const isSelected = selectedDevice?.id === device.id;

    return (
      <ListItem
        key={device.id}
        sx={{
          mb: 1,
          bgcolor: isSelected ? 'action.selected' : 'background.paper',
          borderRadius: 1,
          '&:hover': {
            bgcolor: 'action.hover',
          },
        }}
        secondaryAction={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Switch
              checked={device.isActive}
              onChange={() => toggleDeviceActive(device.id)}
              color="primary"
            />
            <IconButton
              edge="end"
              aria-label="delete"
              onClick={() => removeDevice(device.id)}
            >
              <DeleteIcon />
            </IconButton>
          </Box>
        }
        onClick={() => setSelectedDevice(device)}
      >
        <ListItemText
          primary={device.id}
          secondary={
            <>
              <Typography variant="body2" component="span">
                Status: {device.isActive ? 'Active' : 'Inactive'}
              </Typography>
              {device.lastMessageTime && (
                <Typography variant="body2" component="div" color="text.secondary">
                  Last Message: {new Date(device.lastMessageTime).toLocaleTimeString()}
                </Typography>
              )}
            </>
          }
        />
      </ListItem>
    );
  };

  return (
    <Paper sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">Devices</Typography>
        <Button variant="contained" color="primary" onClick={handleAddDevice}>
          Add Device
        </Button>
      </Box>
      <List sx={{ flexGrow: 1, overflow: 'auto' }}>
        {devices.map(renderDevice)}
      </List>
    </Paper>
  );
};

export default DeviceList;
