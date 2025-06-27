import React from 'react';
import { Paper, Typography, List, ListItem, ListItemText } from '@mui/material';
import { useDevices } from '../context/DeviceContext';

const CommandLog: React.FC = () => {
  const { commandLog } = useDevices();

  return (
    <Paper sx={{ p: 2, height: '100%', overflow: 'auto' }}>
      <Typography variant="h6" sx={{ mb: 2 }}>Backend Commands</Typography>
      <List>
        {commandLog.length === 0 && (
          <ListItem><ListItemText primary="No commands received yet" /></ListItem>
        )}
        {commandLog.map((entry, idx) => (
          <ListItem key={idx} divider>
            <ListItemText
              primary={new Date(entry.timestamp).toLocaleTimeString() + ' - ' + entry.topic}
              secondary={JSON.stringify(entry.payload)}
            />
          </ListItem>
        ))}
      </List>
    </Paper>
  );
};

export default CommandLog; 