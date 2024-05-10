import { Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import { amount } from '../../format';
import './CategorySupplierTable.css';

function AmountCell({value, total, ...props}) {
  return (
    <TableCell {...props} align="center">{amount(value)}</TableCell>
  );
}

function sum(total, operand) {
  return total + operand;
}

function CategorySupplierTable({operations, holders}) {
  const data = operations
    .flatMap((operation) => operation.breakdown)
    .reduce((_data, breakdown) => {
      return {
        ..._data,
        [breakdown.category || '']: {
          ...(_data?.[breakdown.category || ''] || {}),
          [breakdown.supplier || '']: (_data?.[breakdown.category || '']?.[breakdown.supplier || ''] || 0) + breakdown.credit
        }
      };
    }, {});
    
  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell key=""></TableCell>
          <TableCell className="total" key="TOTAL" align="center">TOTAL</TableCell>
          {holders.map((holder) =>
              <TableCell key={holder.id} align="center">{holder.name}</TableCell>
          )}
          <TableCell key="-" align="center">Non assigné</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        <TableRow hover>
          <TableCell className="total" key={'TOTAL'} align="right" variant="head">TOTAL</TableCell>
          <AmountCell className="total" key={'TOTAL-TOTAL'} value={Object.keys(data).map((category) => Object.keys(data[category]).map((holder) => data[category]?.[holder] || 0).reduce(sum, 0)).reduce(sum, 0)} />
          {holders.map((holder) =>
            <AmountCell className="total" key={'TOTAL-' + holder.id} value={Object.keys(data).map((category) => data?.[category]?.[holder.id] || 0).reduce(sum, 0)} />
          )}
          <AmountCell className="total" key={'TOTAL-'} value={Object.keys(data).map((category) => data?.[category]?.[''] || 0).reduce(sum, 0)} />
        </TableRow>
        {Object.keys(data).toSorted().filter(category => !!category).map((category) =>
          <TableRow hover>
            <TableCell key={category} align="right" variant="head">{category.replace(' ', "\u00A0")}</TableCell>
            <AmountCell className="total" key={category + '-TOTAL'} value={Object.keys(data[category]).map((holder) => data[category]?.[holder] || 0).reduce(sum, 0)} />
            {holders.map((holder) =>
              <AmountCell key={category + '-' + holder.id} value={data?.[category]?.[holder.id] || 0} />
            )}
            <AmountCell key={category + '-'} value={data?.[category]?.[''] || 0} />
          </TableRow>
        )}
        <TableRow hover>
          <TableCell key={''} align="right" variant="head">{'Non\u00A0catégorisé'}</TableCell>
          <AmountCell className="total" key={'-TOTAL'} value={Object.keys(data[''] || {}).map((holder) => data['']?.[holder] || 0).reduce(sum, 0)} />
          {holders.map((holder) =>
            <AmountCell key={'-' + holder.id} value={data?.['']?.[holder.id] || 0} />
          )}
          <AmountCell key={'-'} value={data?.['']?.[''] || 0} />
        </TableRow>
      </TableBody>
    </Table>
  );
}

export default CategorySupplierTable;