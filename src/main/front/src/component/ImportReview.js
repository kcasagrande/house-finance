import './ImportReview.css';
import { MenuItem, Select, TableCell, TableRow, TextField, Tooltip } from '@mui/material';
import InfoTwoToneIcon from '@mui/icons-material/InfoTwoTone';
import { DatePicker } from '@mui/x-date-pickers';
import PaginatedTable from '../widget/PaginatedTable';
import OperationType from './OperationType';
import CardChooser from './CardChooser';
import dayjs from 'dayjs';

function ImportReview({account, cards, status, operations, onChange}) {
  function handleTypeChange(operation, index, newType) {
    onChange(index, {...operation, type: newType});
  }
  
  function handleCardChange(operation, index) {
    return (newCard) => onChange(index, {...operation, card: newCard});
  }
  
  function handleCheckNumberChange(operation, index) {
    return (newCheckNumber) => onChange(index, {...operation, checkNumber: newCheckNumber});
  }
  
  function handleOperationDateChange(operation, index) {
    return (newOperationDate) => onChange(index, {...operation, operationDate: newOperationDate});
  }
  
  const columns = [
    {
      id: 'reference',
      label: 'Reference',
      align: 'center',
      value: (operation, index) => {
        return (
          <Tooltip title={operation.reference}>
            <InfoTwoToneIcon />
          </Tooltip>
        );
      }
    },
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
      label: 'Label',
      align: 'left'
    },
    {
      id: 'operationDate',
      label: 'Operation date',
      value: (operation, index) => {
        return (
          <DatePicker
            label="Operation date"
            format="YYYY-MM-DD"
            value={operation.operationDate ? dayjs(operation.operationDate) : null}
            onChange={handleOperationDateChange(operation, index)}
            sx={{ width: 200 }}
            slotProps={{
              field: {
                clearable: true
              }
            }}
          />
        );
      }
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
            sx={{
              display: (operation.type === 'card' ? 'inline-flex' : 'none')
            }}
          />
        );
      }
    },
    {
      id: 'checkNumber',
      label: 'Check number',
      value: (operation, index) => {
        return (
          <TextField
            value={operation.checkNumber}
            onChange={handleCheckNumberChange(operation, index)}
            sx={{
              display: (operation.type === 'check' ? 'inline-flex' : 'none')
            }}
          />
        );
      }
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