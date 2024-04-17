import './ImportReview.css';
import { TableCell, TableRow, TextField } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers';
import PaginatedTable from '../widget/PaginatedTable';
import Reference from './Reference';
import Type from './Type';
import CardChooser from './CardChooser';
import dayjs from 'dayjs';

function ImportReview({cards, operations, onOperationChange}) {

  function modifyOperation(operation, field) {
    return (newValue) => {
      const newOperation = {...operation};
      newOperation[field] = newValue;
      onOperationChange(newOperation);
    }
  }

  function handleCardChange(operation, index) {
    return (newCard) => onOperationChange({...operation, card: newCard});
  }
  
  function handleCheckNumberChange(operation, index) {
    return (newCheckNumber) => onOperationChange({...operation, checkNumber: newCheckNumber});
  }
  
  function handleOperationDateChange(operation, index) {
    return (newOperationDate) => onOperationChange({...operation, operationDate: newOperationDate});
  }
  
  const columns = [
    {
      id: 'reference',
      label: 'Reference',
      align: 'center',
      value: (operation, index) => {
        return (
          <Reference value={operation.reference} />
        );
      }
    },
    {
      id: 'type',
      label: 'Type',
      value: (operation) => {
        return (
          <Type
            defaultValue={operation.type}
            onChange={modifyOperation(operation, 'type')}
          />
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
            onChange={(value) => modifyOperation(operation, 'operationDate')(!!value ? value.format('YYYY-MM-DD') : null)}
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
        if(operation.type === 'card') {
          return (
            <CardChooser
              id={"card-chooser-" + index}
              operation={operation}
              cards={cards}
              onChange={handleCardChange(operation, index)}
            />
          );
        } else {
          return <></>;
        }
      }
    },
    {
      id: 'checkNumber',
      label: 'Check number',
      value: (operation, index) => {
        return (
          <TextField
            defaultValue={operation.checkNumber}
            onKeyPress={(event) => {
              if(event.key === 'Enter') {
                modifyOperation(operation, 'checkNumber')(event.target.value);
              }
            }}
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
      ready={operations.length > 0}
      data={operations}
      format={(operation, index, data) => {
        return (
          <TableRow key={'operation-' + operation.key} className={operation.isValid() ? 'valid' : 'invalid'}>
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
      }}
    />
  );
}

export default ImportReview;