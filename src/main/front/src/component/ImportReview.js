import './ImportReview.css';
import { useState } from 'react';
import { CircularProgress, Container, MenuItem, Paper, Select, Stack, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow, Tooltip, Typography } from '@mui/material';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';

function ImportReview({status, rows}) {
  const [operations, setOperations] = useState(rows);
  const columns = [
    {
      id: 'type',
      label: 'Type',
      value: (operation) => {
        return (
          <Select
            value={operation.type}>
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
      value: (operation) => {
        return new Intl.NumberFormat(
          'fr-FR',
          { style: 'currency', currency: 'EUR', minimumFractionDigits: 2 }
        )
         .format(operation.credit / 100);
      }
    },
    {
      id: 'cardSuffix',
      label: 'Card suffix'
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
    <PaginatedTable
      rowsPerPageOptions={[10, 50, 100]}
      columns={columns}
      rows={operations
        .map((operation) => {
          return (
            <TableRow key={operation.id} className={validate(operation) ? 'valid' : 'invalid'}>
              {columns.map((column) => {
                return (
                  <TableCell key={column.id} align={column.align || 'left'}>
                    {column.value
                      ? column.value(operation)
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
  );
}

export default ImportReview;