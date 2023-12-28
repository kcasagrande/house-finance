import './App.css';
import React from 'react';
import Operations from './tab/Operations';
import { Box, Button, Drawer, IconButton, List, ListItem } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import UploadIcon from '@mui/icons-material/Upload';
import TableRowsIcon from '@mui/icons-material/TableRows';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import configuration from './Configuration';

function App() {
  const [operations, setOperations] = React.useState([]);
  const [drawerOpen, setDrawerOpen] = React.useState(false);
  
  function updateOperations() {
    fetch(configuration.api + "/operations?from=2023-01-01&to=2023-12-31")
      .then(response => {
        if(response.ok) {
          const json = response.json();
          return json;
        } else {
          throw new Error('Response status is ' + response.status);
        }
      })
      .then(setOperations);
  }

  return (
    <div key="left" className="App">
      <IconButton
         aria-label="open drawer"
         onClick={() => setDrawerOpen(true)}
       >
        <MenuIcon />
      </IconButton>
      <Button variant="contained" onClick={updateOperations}>Actualiser</Button>
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
          <List>
            <ListItem><UploadIcon />&nbsp;Import de données</ListItem>
            <ListItem><TableRowsIcon />&nbsp;Traitement de masse</ListItem>
            <ListItem><ManageSearchIcon />&nbsp;Détails</ListItem>
          </List>
        </Box>
      </Drawer>
      <Operations operations={operations}/>
    </div>
  );
}

export default App;
