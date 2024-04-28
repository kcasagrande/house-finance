import { TableCell, TableRow } from '@mui/material';
import amount from '../amount';
import CategoryName from './CategoryName';
import OperationBreakdownSupply from './OperationBreakdownSupply';

function OperationBreakdownRow({category, supplies}) {
  return (
    <TableRow key={category}>
      <TableCell><CategoryName value={category} /></TableCell>
      <TableCell>{amount(supplies.reduce((sum, supply) => sum + supply.credit, 0))}</TableCell>
      <TableCell>{supplies.map(supply => <OperationBreakdownSupply value={supply} />)}</TableCell>
    </TableRow>
  );
}

export default OperationBreakdownRow;