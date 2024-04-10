import './ImportReview.css';
import React from 'react';
import { CircularProgress, Container, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow, Tooltip, Typography } from '@mui/material';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';

function ImportReview({status, rows}) {
  const columns = [
    {
      id: 'typeIcon',
      label: 'Type'
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
      label: 'Credit'
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

  function credit(row) {
    return new Intl.NumberFormat(
      'fr-FR',
      { style: 'currency', currency: 'EUR', minimumFractionDigits: 2 }
    )
      .format(row.credit / 100);
  }

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
      rows={rows
        .map((row) => { return {
          ...row,
          credit: credit(row),
          typeIcon: <OperationType type={row.type} />
        };})
        .map((row) => {
          return (
            <TableRow key={row.id} className={validate(row) ? 'valid' : 'invalid'}>
              {columns.map((column) => {
                const value = row[column.id];
                return (
                  <TableCell key={column.id} align={column.align || 'left'}>
                    {column.format && typeof value === 'number'
                      ? column.format(value)
                      :value
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