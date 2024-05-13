import './App.css';
import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { Alert, AppBar, Box, Button, Drawer, FormControl, IconButton, InputLabel, List, ListItem, ListItemButton, ListItemIcon, ListItemText, MenuItem, Select, Stack, Toolbar, Tooltip, Typography } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import InsightsIcon from '@mui/icons-material/Insights';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import TableRowsIcon from '@mui/icons-material/TableRows';
import UploadIcon from '@mui/icons-material/Upload';
import { OperationIcon } from './icons';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import OperationsForm from './component/OperationsForm';
import { fetchHolders, fetchOperations } from './application/fetch-data';
import 'dayjs/locale/fr';

function App({account, onAccountChange, operations, onOperationsChange}) {
  const [menuDrawerOpen, setMenuDrawerOpen] = React.useState(false);
  const [operationsDrawerOpen, setOperationsDrawerOpen] = React.useState(false);
  
  function handleSubmit(criteria) {
    fetchOperations(criteria)
      .then(onOperationsChange);
    onAccountChange(criteria['account']);
  }
  
  return (
    <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="fr">
      <AppBar position="sticky">
        <Toolbar component={Stack} direction="row" alignItems="center" justifyContent="space-between">
          <Tooltip title="Open main menu">
            <IconButton
              aria-label="open menu drawer"
              onClick={() => setMenuDrawerOpen(true)}
            >
              <MenuIcon />
            </IconButton>
          </Tooltip>
          <Stack direction="row" alignItems="center" justifyContent="flex-end" spacing={2}>
            <Typography>
              Currently working on {operations.length} operations.
            </Typography>
            <Tooltip title="Load operations">
              <IconButton
                aria-label="open operations drawer"
                onClick={() => setOperationsDrawerOpen(true)}
              >
                <OperationIcon />
              </IconButton>
            </Tooltip>
          </Stack>
        </Toolbar>
      </AppBar>
      <Drawer
        anchor="left"
        open={menuDrawerOpen}
        onClose={() => setMenuDrawerOpen(false)}
      >
        <Box
          sx={{ width: 300 }}
          role="presentation"
          onClick={() => setMenuDrawerOpen(false)}
        >
          <List>
            <ListItem disablePadding>
              <ListItemButton component={Link} to={`/`}>
                <ListItemIcon>
                  <HomeIcon />
                </ListItemIcon>
                <ListItemText primary="Accueil" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton component={Link}>
                <ListItemIcon>
                  <TableRowsIcon />
                </ListItemIcon>
                <ListItemText primary="Traitement de masse" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton component={Link} to={`/import`}>
                <ListItemIcon>
                  <UploadIcon />
                </ListItemIcon>
                <ListItemText primary="Import de données" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton component={Link} to={`/reports`}>
                <ListItemIcon>
                  <InsightsIcon />
                </ListItemIcon>
                <ListItemText primary="Rapports" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton component={Link} to={`/details`}>
                <ListItemIcon>
                  <ManageSearchIcon />
                </ListItemIcon>
                <ListItemText primary="Détails" />
              </ListItemButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>
      <Outlet />
      <Drawer
        anchor="right"
        open={operationsDrawerOpen}
        onClose={() => setOperationsDrawerOpen(false)}
      >
        <Stack
          direction="column"
          alignItems="center"
          justifyContent="flex-start"
          spacing={2}
          sx={{
            margin: '1em'
          }}
        >
          <Typography variant="h2">Search operations</Typography>
          <OperationsForm onSubmit={handleSubmit} />
        </Stack>
      </Drawer>
    </LocalizationProvider>
  );
}

export default App;
