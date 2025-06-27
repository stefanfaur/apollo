import { Grid, Box, Typography, CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { DeviceProvider } from './context/DeviceContext';
import DeviceList from './components/DeviceList';
import DeviceControl from './components/DeviceControl';
import CommandLog from './components/CommandLog';
import './App.css';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#2196f3',
    },
    secondary: {
      main: '#f50057',
    },
  },
});

function App() {

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <DeviceProvider>
        <Box sx={{ maxWidth: 1200, mx: 'auto', p: 4 }}>
          <Box sx={{ mb: 4 }}>
            <Typography variant="h3" sx={{ color: 'white', mb: 1, fontWeight: 'bold' }}>
              Device Simulator
            </Typography>
            <Typography variant="subtitle1" sx={{ color: 'text.secondary' }}>
              Interactive IoT Device Simulator for Apollo
            </Typography>
          </Box>
          <Grid container spacing={3} sx={{ height: 'calc(100vh - 300px)' }}>
            <Grid item xs={12} md={4}>
              <DeviceList />
            </Grid>
            <Grid item xs={12} md={8} container direction="column" spacing={2}>
              <Grid item xs>
                <DeviceControl />
              </Grid>
              <Grid item xs>
                <CommandLog />
              </Grid>
            </Grid>
          </Grid>
        </Box>
      </DeviceProvider>
    </ThemeProvider>
  );
}

export default App;
