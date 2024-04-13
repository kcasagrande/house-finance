import './ImportReview.css';
import { MenuItem, Select, TableCell, TableRow, TextField } from '@mui/material';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';

function ImportReview({status, operations, onChange}) {
  
  function handleTypeChange(operation, index, newType) {
    onChange(index, {...operation, type: newType});
  }
  
  function handleCardSuffixChange(operation, index, newCardSuffix) {
    onChange(index, {...operation, cardSuffix: newCardSuffix});
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