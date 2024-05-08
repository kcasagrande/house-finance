import OperationRow from '../component/OperationRow';
import { Box, LinearProgress, MenuItem, Paper, Select, Stack, TableContainer, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { useEffect, useState } from 'react';
import configuration from '../Configuration';
import SearchOperations from '../component/SearchOperations';
import { fetchAccounts, fetchCategories, fetchHolders, fetchOperations } from '../application/fetch-data';

function Details() {
  const [initialized, setInitialized] = useState(false);
  const [existingCategories, setExistingCategories] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [account, setAccount] = useState(null);
  const [persons, setPersons] = useState([]);
  const [operations, setOperations] = useState([]);
  
  useEffect(() => {
    if(!initialized) {
      Promise.all([
        refreshAccounts(),
        refreshOperations(),
        refreshExistingCategories()
      ])
        .finally(() => setInitialized(true));
    }
  }, [initialized]);
  
  useEffect(() => {
    if(!!account) {
      refreshPersons(account);
    }
  }, [account]);
  
  function replaceOperation(operation) {
    console.log('Operation replacement not implemented yet.');
    setOperations(operations);
  }
  
  function refreshAccounts() {
    return fetchAccounts()
      .then(setAccounts);
  }
  
  function refreshPersons(account) {
    return fetchHolders(account)
      .then(setPersons);
  }
  
  function refreshOperations() {
    return fetchOperations()
      .then(setOperations);
  }
  
  function refreshExistingCategories() {
    return fetchCategories()
      .then(categories => { return categories.toSorted((a, b) => a.localeCompare(b, configuration.locale)); })
      .then(setExistingCategories);
  }
    
  function LoadingAnimation({initialized}) {
    if(!initialized) {
      return (
        <TableRow>
          <TableCell colSpan={9}>
            <Box sx={{width: '100%'}}>
              <LinearProgress />
            </Box>
          </TableCell>
        </TableRow>
      );
    } else {
      return null;
    }
  }
    
  return (
    <Stack direction="column">
      <Select value={account} onChange={(event) => setAccount(event.target.value)}>
        {accounts.map((account) =>
          <MenuItem key={account.ibanAsString} value={account}>
            {account.ibanAsString + ' - ' + account.holder}
          </MenuItem>
        )}
      </Select>
      <SearchOperations callback={setOperations} />
      <TableContainer component={Paper} id="operations">
        <Table aria-label="collapsible table" size="small">
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
            <LoadingAnimation initialized={initialized} />
            {operations.map(operation =>
              <OperationRow
                key={'operation-' + operation.id}
                operation={operation}
                existingCategories={existingCategories}
                holders={persons}
                onChange={replaceOperation}
              />
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
}

export default Details;