import React from 'react';
import { Avatar, Chip, TableRow, TableCell, Tooltip } from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import Breakdown from './Breakdown';
import {centsAsEurosString} from '../Cents';

function Unsupplied(props) {
  const { credit, account } = props;
  const [holders, setHolders] = React.useState([]);
  const [breakdownOpen, setBreakdownOpen] = React.useState(false);
  
  function handleCreate() {
    if(account) {
      const url = "http://localhost:8080/api/v1/accounts/" + account + "/holders";
      fetch(url)
        .then(response => {
          if(response.ok) {
            const json = response.json();
            return json;
          } else {
            throw new Error('Response status to fetching "' + url + '" is ' + response.status);
          }
        })
        .then(holders => {
          return holders;
        })
        .then(setHolders);
    }
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
      <Breakdown open={breakdownOpen} credit={credit} holders={holders} onClose={handleBreakdownClose} />
    </>
  );
}

function Supply(props) {
  const { supplier, credit, account } = props;
  
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
    return (<Unsupplied credit={credit} account={account} />);
  }
}

function Category(props) {
  const { name, supplies, account } = props;

  return (
    <TableRow>
      <TableCell sx={ name.length > 0 ? {} : { color: "gray", fontStyle: "italic" } }>{name.length > 0 ? name : 'Non catégorisé'}</TableCell>
      <TableCell>{supplies.map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit} account={account} />)}</TableCell>
    </TableRow>
  );
}

export default Category;