import Operation from '../component/Operation';
import { Paper, TableContainer, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { useEffect, useState } from 'react';

function Operations({operations}) {
  const [initialized, setInitialized] = useState(false);
  const [existingCategories, setExistingCategories] = useState([]);
  
  useEffect(() => {
    if(!initialized) {
      refreshExistingCategories();
      setInitialized(true);
    }
  }, []);
  
  function refreshExistingCategories() {
    return fetch("http://localhost:8080/api/v1/operations/categories")
      .then(response => {
        if(response.ok) {
          const json = response.json();
          return json;
        } else {
          throw new Error('Response status is ' + response.status);
        }
      })
      .then(categories => { return categories.toSorted((a, b) => a.localeCompare(b, 'fr')); })
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