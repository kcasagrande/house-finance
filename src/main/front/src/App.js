import './App.css';
import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box, Drawer, IconButton, Link, MenuList, MenuItem } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import InsightsIcon from '@mui/icons-material/Insights';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import MenuIcon from '@mui/icons-material/Menu';
import TableRowsIcon from '@mui/icons-material/TableRows';
import UploadIcon from '@mui/icons-material/Upload';
import configuration from './Configuration';

function App() {
  const [drawerOpen, setDrawerOpen] = React.useState(false);
  
  return (
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
          sx={{ width: 250 }}
          role="presentation"
          onClick={() => setDrawerOpen(false)}
        >
          <MenuList>
            <MenuItem><Link underline="none" to={`/`}><HomeIcon />&nbsp;Accueil</Link></MenuItem>
            <MenuItem><TableRowsIcon />&nbsp;Traitement de masse</MenuItem>
            <MenuItem><UploadIcon />&nbsp;Import de données</MenuItem>
            <MenuItem><InsightsIcon />&nbsp;Rapports</MenuItem>
            <MenuItem><Link underline="none" to={`/details`}><ManageSearchIcon />&nbsp;Détails</Link></MenuItem>
          </MenuList>
        </Box>
      </Drawer>
      <Outlet />
    </div>
  );
}

export default App;
