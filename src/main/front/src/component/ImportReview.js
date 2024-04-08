import React from 'react';
import { CircularProgress, Container, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow, Tooltip, Typography } from '@mui/material';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';

function ImportReview({status, operations}) {
  const columns = [
    {
      id: 'type',
      label: 'Type'
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
      id: 'amount',
      label: 'Amount'
    }
  ];

  function amount(operation) {
    return new Intl.NumberFormat(
      'fr-FR',
      { style: 'currency', currency: 'EUR', minimumFractionDigits: 2 }
    )
      .format(operation.breakdown[0].credit / 100);
  }

  return (
    <PaginatedTable
      rowsPerPageOptions={[10, 50, 100]}
      columns={columns}
      rows={operations
        .map((operation) => { return {
          ...operation,
          amount: amount(operation),
          type: <OperationType type={operation.type} />
        };})
      }
    />
  );
}

export default ImportReview;