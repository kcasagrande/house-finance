import './ImportReview.css';
import { TableCell, TableRow, TextField } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers';
import PaginatedTable from '../widget/PaginatedTable';
import Reference from './Reference';
import Method from './Method';
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
      id: 'method',
      label: 'Method',
      value: (operation) => {
        return (
          <Method
            defaultValue={operation.method}
            onChange={modifyOperation(operation, 'method')}
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
            referenceDate={dayjs(operation.accountDate)}
            sx={{ width: 200 }}
            slotProps={{
              field: {
                clearable: true
              },
              textField: {
                size: 'small'
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
        if(operation.method === 'card') {
          return (
            <CardChooser
              id={"card-chooser-" + index}
              operation={operation}
              cards={cards}
              onChange={modifyOperation(operation, 'card')}
            />
          );
        } else {
          return (<></>);
        }
      }
    },
    {
      id: 'checkNumber',
      label: 'Check number',
      value: (operation, index) => {
        if(operation.method === 'check') {
          return (
            <TextField
              size="small"
              defaultValue={operation.checkNumber}
              onKeyPress={(event) => {
                if(event.key === 'Enter') {
                  event.target.blur();
                }
              }}
              onBlur={(event) => modifyOperation(operation, 'checkNumber')(event.target.value)}
            />
          );
        } else {
          return (<></>);
        }
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