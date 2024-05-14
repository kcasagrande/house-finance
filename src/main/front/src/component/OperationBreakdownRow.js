import { TableCell, TableRow } from '@mui/material';
import { amount } from '../format';
import CategoryName from './CategoryName';
import OperationBreakdownSupply from './OperationBreakdownSupply';

function OperationBreakdownRow({category, supplies}) {
  return (
    <TableRow key={category}>
      <TableCell key="category"><CategoryName value={category} /></TableCell>
      <TableCell key="credit">{amount(supplies.reduce((sum, supply) => sum + supply.credit, 0))}</TableCell>
      <TableCell key="holders">{supplies.map(supply => <OperationBreakdownSupply key={'supply-' + supply.supplier} value={supply} />)}</TableCell>
    </TableRow>
  );
}

export default OperationBreakdownRow;