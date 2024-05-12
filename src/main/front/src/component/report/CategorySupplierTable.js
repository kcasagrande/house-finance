import { Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import { amount } from '../../format';
import './CategorySupplierTable.css';

function dataReducer(_data, breakdown) {
  return {
    ..._data,
    [breakdown.category || '']: {
      ...(_data?.[breakdown.category || ''] || {}),
      [breakdown.supplier || '']: {
        debit: (_data?.[breakdown.category || '']?.[breakdown.supplier || '']?.debit || 0) + (breakdown.credit <= 0 ? breakdown.credit : 0),
        credit: (_data?.[breakdown.category || '']?.[breakdown.supplier || '']?.credit || 0) + (breakdown.credit > 0 ? breakdown.credit : 0)
      },
      '*': {
        debit: (_data?.[breakdown.category || '']?.['*']?.debit || 0) + (breakdown.credit <= 0 ? breakdown.credit : 0),
        credit: (_data?.[breakdown.category || '']?.['*']?.credit || 0) + (breakdown.credit > 0 ? breakdown.credit : 0)
      }
    },
    '*': {
      ...(_data?.['*'] || {}),
      [breakdown.supplier || '']: {
        debit: (_data?.['*']?.[breakdown.supplier || '']?.debit || 0) + (breakdown.credit <= 0 ? breakdown.credit : 0),
        credit: (_data?.['*']?.[breakdown.supplier || '']?.credit || 0) + (breakdown.credit > 0 ? breakdown.credit : 0)
      },
      '*': {
        debit: (_data?.['*']?.['*']?.debit || 0) + (breakdown.credit <= 0 ? breakdown.credit : 0),
        credit: (_data?.['*']?.['*']?.credit || 0) + (breakdown.credit > 0 ? breakdown.credit : 0)
      }
    }
  };
}

function BalanceCell({balance = { debit: 0, credit: 0 }, ...props}) {
  return (
    <TableCell {...props} align="center">{amount(balance.debit) + '/' + amount(balance.credit)}</TableCell>
  );
}

function CategorySupplierTable({operations, holders}) {
  const data = operations
    .flatMap((operation) => operation.breakdown)
    .reduce(dataReducer, {});
    
  return (
    <Table size="small" padding="none">
      <TableHead>
        <TableRow>
          <TableCell key="#"></TableCell>
          <TableCell className="total" key="*" align="center">TOTAL</TableCell>
          {holders.map((holder) =>
              <TableCell key={holder.id} align="center">{holder.name}</TableCell>
          )}
          <TableCell key="" align="center">Non assigné</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        <TableRow key="*" hover>
          <TableCell className="total" key="#" align="right" variant="head">TOTAL</TableCell>
          <BalanceCell className="total" key={'*-*'} balance={data?.['*']?.['*']} />
          {holders.map((holder) =>
            <BalanceCell className="total" key={'*-' + holder.id} balance={data?.['*']?.[holder.id]} />
          )}
          <BalanceCell className="total" key={'*-'} balance={data?.['*']?.['']} />
        </TableRow>
        {Object.keys(data).toSorted().filter((category) => category !== '' && category !== '*').map((category) =>
          <TableRow key={category} hover>
            <TableCell key="#" align="right" variant="head">{category.replace(' ', "\u00A0")}</TableCell>
            <BalanceCell className="total" key={category + '-*'} balance={data?.[category]?.['*']} />
            {holders.map((holder) =>
              <BalanceCell key={category + '-' + holder.id} balance={data?.[category]?.[holder.id]} />
            )}
            <BalanceCell className="total" key={category + '-'} balance={data?.[category]?.['']} />
          </TableRow>
        )}
        <TableRow key="" hover>
          <TableCell key={''} align="right" variant="head">{'Non\u00A0catégorisé'}</TableCell>
          <BalanceCell className="total" key={'-*'} balance={data?.['']?.['*']} />
          {holders.map((holder) =>
            <BalanceCell key={'-' + holder.id} balance={data?.['']?.[holder.id]} />
          )}
          <BalanceCell key={'-'} balance={data?.['']?.['']} />
        </TableRow>
      </TableBody>
    </Table>
  );
}

export default CategorySupplierTable;