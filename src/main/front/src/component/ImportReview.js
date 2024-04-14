import './ImportReview.css';
import configuration from '../Configuration';
import { useEffect, useState } from 'react';
import { MenuItem, Select, TableCell, TableRow, TextField } from '@mui/material';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';
import CardChooser from './CardChooser';

function ImportReview({account, cards, status, operations, onChange}) {
  function handleTypeChange(operation, index, newType) {
    onChange(index, {...operation, type: newType});
  }
  
  function handleCardChange(operation, index) {
    return (newCard) => onChange(index, {...operation, card: newCard});
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
      id: 'card',
      label: 'Card',
      value: (operation, index) => {
        return (
          <CardChooser
            id={"card-chooser-" + index}
            operation={operation}
            cards={cards}
            onChange={handleCardChange(operation, index)}
          />
        );
      }
    },
    {
      id: 'checkNumber',
      label: 'Check number'
    }
  ];
  
  return (
    <PaginatedTable
      rowsPerPageOptions={[10, 50, 100]}
      columns={columns}
      ready={status === 'ready'}
    >
      {operations
        .map((operation, index) => {
          return (
            <TableRow key={'operation-' + index} className={operation.isValid() ? 'valid' : 'invalid'}>
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
    </PaginatedTable>
  );
}

export default ImportReview;