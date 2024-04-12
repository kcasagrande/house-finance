import './ImportReview.css';
import { useState } from 'react';
import { CircularProgress, Container, LinearProgress, MenuItem, Paper, Select, Stack, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow, TextField, Tooltip, Typography } from '@mui/material';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';

function ImportReview({status, rows}) {
  const [operations, setOperations] = useState(rows);
  
  function handleTypeChange(operation, index, newType) {
    setOperations(operations.toSpliced(index, 1, {...operation, type: newType}));
  }
  
  function handleCardSuffixChange(operation, index, newCardSuffix) {
    setOperations(operations.toSpliced(index, 1, {...operation, cardSuffix: newCardSuffix}));
  }
  
  const columns = [
    {
      id: 'type',
      label: 'Type',
      value: (operation, index) => {
        return (
          <Select
            defaultValue={operation.type}
            onChange={(event) => handleTypeChange(operation, index, event.target.value)}
            >
            <MenuItem value="card"><OperationType type="card" /></MenuItem>
            <MenuItem value="check"><OperationType type="check" /></MenuItem>
            <MenuItem value="debit"><OperationType type="debit" /></MenuItem>
            <MenuItem value="transfer"><OperationType type="transfer" /></MenuItem>
          </Select>
        );
      }
    },
    {
      id: 'reference',
      label: 'Reference'
    },
    {
      id: 'label',
      label: 'Label'
    },
    {
      id: 'operationDate',
      label: 'Operation date'
    },
    {
      id: 'valueDate',
      label: 'Value date'
    },
    {
      id: 'accountDate',
      label: 'Account date'
    },
    {
      id: 'credit',
      label: 'Credit',
      value: (operation, index) => {
        return new Intl.NumberFormat(
          'fr-FR',
          { style: 'currency', currency: 'EUR', minimumFractionDigits: 2 }
        )
         .format(operation.credit / 100);
      }
    },
    {
      id: 'cardSuffix',
      label: 'Card suffix',
      value: (operation, index) => {
        return (
          <TextField
            variant="standard"
            defaultValue={operation.cardSuffix}
            onChange={(event) => handleCardSuffixChange(operation, index, event.target.value)}
            disabled={operation.type !== 'card'}
          />
        );
      }
    },
    {
      id: 'checkNumber',
      label: 'Check number'
    }
  ];

  function validate(row) {
    return isValidCardOperation(row)
      || isValidCheckOperation(row)
      || isValidDebitOperation(row)
      || isValidTransferOperation(row);
  }
  
  function isValidCardOperation(row) {
    return (row.type === 'card') &&
      (row.cardSuffix) &&
      (row.operationDate);
  }
  
  function isValidCheckOperation(row) {
    return (row.type === 'check') &&
      (row.checkNumber) &&
      (row.operationDate);
  }
  
  function isValidDebitOperation(row) {
    return (row.type === 'debit');
  }

  function isValidTransferOperation(row) {
    return (row.type === 'transfer');
  }
  
  return (
    <>
      <LinearProgress variant="determinate" value={operations.filter(validate).length * 100 / operations.length} />
      <PaginatedTable
        rowsPerPageOptions={[10, 50, 100]}
        columns={columns}
        rows={operations
          .map((operation, index) => {
            return (
              <TableRow key={'operation-' + index} className={validate(operation) ? 'valid' : 'invalid'}>
                {columns.map((column) => {
                  return (
                    <TableCell key={column.id} align={column.align || 'left'}>
                      {column.value
                        ? column.value(operation, index)
                        :operation[column.id]
                      }
                    </TableCell>
                  );
                })}
              </TableRow>
            );
          })
        }
      />
    </>
  );
}

export default ImportReview;