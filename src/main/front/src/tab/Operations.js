import Operation from '../component/Operation';
import { Paper, TableContainer, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { useEffect, useState } from 'react';
import configuration from '../Configuration';

function Operations() {
  const [initialized, setInitialized] = useState(false);
  const [existingCategories, setExistingCategories] = useState([]);
  const [operations, setOperations] = useState([]);
  
  useEffect(() => {
    if(!initialized) {
      refreshOperations();
      refreshExistingCategories();
      setInitialized(true);
    }
  }, []);
  
  function refreshOperations() {
    return fetch(configuration.api + "/operations?from=2023-01-01&to=2023-12-31")
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
  
  function refreshExistingCategories() {
    return fetch(configuration.api + "/operations/categories")
      .then(response => {
        if(response.ok) {
          const json = response.json();
          return json;
        } else {
          throw new Error('Response status is ' + response.status);
        }
      })
      .then(categories => { return categories.toSorted((a, b) => a.localeCompare(b, configuration.locale)); })
      .then(setExistingCategories);
  }
    
  return (
    <TableContainer component={Paper} id="operations">
      <Table aria-label="collapsible table">
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            <TableCell>Référence</TableCell>
            <TableCell>Type</TableCell>
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
            <Operation
              key={'operation-' + operation.id}
              operation={operation}
              existingCategories={existingCategories}
              refreshExistingCategories={refreshExistingCategories}
            />
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default Operations;