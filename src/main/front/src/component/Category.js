import React from 'react';
import { Avatar, Chip, TableRow, TableCell, Tooltip } from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import Breakdown from './Breakdown';
import {centsAsEurosString} from '../Cents';

function Unsupplied(props) {
  const { credit } = props;
  const [breakdownOpen, setBreakdownOpen] = React.useState(false);
  
  function handleCreate() {
    setBreakdownOpen(true);
  }
  
  function handleBreakdownClose() {
    setBreakdownOpen(false);
  }
  
  return (
    <>
      <Chip
        sx={{ marginRight: '1ex' }}
        variant="outlined"
        label={centsAsEurosString(credit) + ' €'}
        onDelete={handleCreate}
        deleteIcon={<Tooltip title="Ventiler la somme"><AddCircleIcon /></Tooltip>}
      />
      <Breakdown open={breakdownOpen} credit={credit} onClose={handleBreakdownClose} />
    </>
  );
}

function Supply(props) {
  const { supplier, credit } = props;
  
  function handleDelete() {
    console.log("TODO Delete breakdown");
  }
  
  if(supplier) {
    return (
      <Chip
        sx={{ marginRight: '1ex' }}
        variant="outlined"
        avatar={<Avatar src={'/avatar/' + supplier + '.png'} />}
        label={(credit / 100.0) + ' €'}
        onDelete={handleDelete}
        deleteIcon={<Tooltip title="Supprimer cette ventilation"><CancelIcon /></Tooltip>}
      />
    );
  } else {
    return (<Unsupplied credit={credit} />);
  }
}

function Category(props) {
  const { name, supplies } = props;

  return (
    <TableRow>
      <TableCell sx={ name.length > 0 ? {} : { color: "gray", fontStyle: "italic" } }>{name.length > 0 ? name : 'Non catégorisé'}</TableCell>
      <TableCell>{supplies.map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit}/>)}</TableCell>
    </TableRow>
  );
}

export default Category;