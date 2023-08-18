import './App.css';
import React from 'react';
import { Button } from '@mui/material';
import Operation from './component/Operation';
import { Paper, TableContainer, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';

function App() {
  const [operations, setOperations] = React.useState([]);
  
  function updateOperations() {
    fetch("http://localhost:8080/api/v1/operations?from=2023-01-01&to=2023-12-31")
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
    <div className="App">
      <header className="App-header">
        <h1>Opérations</h1>
      </header>
      <Button variant="contained" onClick={updateOperations}>Actualiser</Button>
      <TableContainer component={Paper} id="operations">
        <Table aria-label="collapsible table">
          <TableHead>
            <TableRow>
              <TableCell></TableCell>
              <TableCell>Référence</TableCell>
              <TableCell>Libellé</TableCell>
              <TableCell>Date d'opération</TableCell>
              <TableCell>Date de valeur</TableCell>
              <TableCell>Date comptable</TableCell>
              <TableCell>Montant</TableCell>
              <TableCell>Carte</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {operations.map(operation =>
              <Operation key={'operation-' + operation.id} operation={operation} />
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
}

export default App;
