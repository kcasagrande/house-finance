import OperationRow from '../component/OperationRow';
import { Box, LinearProgress, MenuItem, Paper, Select, Stack, TableContainer, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { useContext, useEffect, useState } from 'react';
import configuration from '../Configuration';
import SearchOperations from '../component/SearchOperations';
import { fetchAccounts, fetchCategories, fetchHolders, fetchOperations } from '../application/fetch-data';
import { HoldersContext } from '../context/HoldersContext';

function Details({operations, replaceOperation}) {
  const holders = useContext(HoldersContext);
  const existingCategories = [...new Set(operations.flatMap((operation) => operation.breakdown).map((breakdown) => breakdown.category))];
  
  return (
    <Stack direction="column">
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
            {operations.map(operation =>
              <OperationRow
                key={'operation-' + operation.id}
                operation={operation}
                existingCategories={existingCategories}
                holders={holders}
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