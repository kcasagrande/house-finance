import Operation from '../component/Operation';
import { Paper, TableContainer, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';

function Operations(props) {
  const {operations} = props;
  
  return (
    <TableContainer component={Paper} id="operations">
      <Table aria-label="collapsible table">
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            <TableCell>Référence</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Libellé</TableCell>
            <TableCell>Date d'opération</TableCell>
            <TableCell>Date de valeur</TableCell>
            <TableCell>Date comptable</TableCell>
            <TableCell>Montant</TableCell>
            <TableCell>Carte</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {operations.map(operation =>
            <Operation key={'operation-' + operation.id} operation={operation} />
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default Operations;