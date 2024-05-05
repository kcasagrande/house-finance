import './App.css';
import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { Box, Drawer, IconButton, List, ListItem, ListItemButton, ListItemIcon, ListItemText } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import InsightsIcon from '@mui/icons-material/Insights';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import MenuIcon from '@mui/icons-material/Menu';
import TableRowsIcon from '@mui/icons-material/TableRows';
import UploadIcon from '@mui/icons-material/Upload';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import 'dayjs/locale/fr';

function App() {
  const [drawerOpen, setDrawerOpen] = React.useState(false);
  
  return (
    <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="fr">
      <div key="left" className="App">
        <IconButton
           aria-label="open drawer"
           onClick={() => setDrawerOpen(true)}
         >
          <MenuIcon />
        </IconButton>
        <Drawer
          anchor="left"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
        >
          <Box
            sx={{ width: 300 }}
            role="presentation"
            onClick={() => setDrawerOpen(false)}
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
      </div>
    </LocalizationProvider>
  );
}

export default App;
