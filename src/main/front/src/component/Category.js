import { Avatar, Chip, TableRow, TableCell } from '@mui/material';

function Supply(props) {
  const { supplier, credit } = props;
  
  return (
    <Chip
      sx={{ marginRight: '1ex' }}
      variant="outlined"
      avatar={<Avatar src={'/avatar/' + supplier + '.png'} />}
      label={credit + ' €'}
    />
  );
}

function Category(props) {
  const { name, supplies } = props;

  return (
    <TableRow>
      <TableCell>{name}</TableCell>
      <TableCell>{supplies.map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit / 100.0}/>)}</TableCell>
    </TableRow>
  );
}

export default Category;