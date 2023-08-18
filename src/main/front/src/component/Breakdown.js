import { TableRow, TableCell } from '@mui/material';

function Breakdown(props) {
  const {breakdown} = props;
  
  return (
    <TableRow>
      <TableCell>{breakdown.category}</TableCell>
      <TableCell>{breakdown.supplier}</TableCell>
      <TableCell className="euros">{(breakdown.credit / 100.0).toLocaleString()}</TableCell>
    </TableRow>
  );
}

export default Breakdown;