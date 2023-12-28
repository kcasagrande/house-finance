import React from 'react';
import { Avatar, Button, Chip, Fab, FormControl, Icon, IconButton, Menu, MenuItem, Select, TableRow, TableCell, Tooltip } from '@mui/material';
import { AddCircle, Balance, Cancel, CheckCircleOutline, Edit, Flaky, PendingOutlined } from '@mui/icons-material';
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
        deleteIcon={<Tooltip title="Ventiler la somme"><AddCircle /></Tooltip>}
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
        deleteIcon={<Tooltip title="Supprimer cette ventilation"><Cancel /></Tooltip>}
      />
    );
  } else {
    return (<Unsupplied credit={credit} account={account} />);
  }
}

function ActionButton(props) {
  const { title, icon, onClick } = props;
  
  return (
    <Tooltip title={title}><IconButton size="small" onClick={onClick}>{icon}</IconButton></Tooltip>
  );
}

function CategoryChooser({ anchor, onSelect, onClose, existingCategories, refreshExistingCategories }) {
  return (
    <Menu
      anchorEl={anchor}
      open={!!anchor}
      onClose={(event, reason) => {
        refreshExistingCategories();
        onClose(event, reason);
      }}
      anchorOrigin={{vertical: 'top', horizontal: 'right'}}
      transformOrigin={{vertical: 'top', horizontal: 'right'}}
    >
      <MenuItem key="title" disabled={true}>Choisissez une catégorie</MenuItem>
      <MenuItem key="new">Nouvelle catégorie…</MenuItem>
      {existingCategories.map((category) => <MenuItem key={"category-" + category} onClick={() => onSelect(category)}>{category}</MenuItem>)}
    </Menu>
  );
}

function Uncategorized({ supplies, account, existingCategories, refreshExistingCategories }) {
  const [ anchor, setAnchor ] = React.useState(null);
  
  function handleClick(event) {
    setAnchor(event.currentTarget);
  }
  
  function handleCategorySelect(category) {
    alert(`Will change category for ${JSON.stringify(category, null, 2)}`);
    setAnchor(null);
  }
  
  return (
    <TableRow>
      <TableCell sx={{ color: "gray", fontStyle: "italic" }}>
        Non catégorisé
        <ActionButton title="Changer la catégorie" icon={<Edit />} onClick={handleClick}>Non catégorisé</ActionButton>
        <CategoryChooser
          anchor={anchor}
          onSelect={handleCategorySelect}
          onClose={() => {setAnchor(null);}}
          existingCategories={existingCategories}
          refreshExistingCategories={refreshExistingCategories}
        />
      </TableCell>
      <TableCell>
        {centsAsEurosString(supplies.map(supply => supply.credit).reduce((sum, credit) => sum + credit, 0)) + ' €'}
        <ActionButton title="Catégoriser la moitié" icon={<Flaky />} />
        <ActionButton title="Catégoriser un montant arbitraire" icon={<PendingOutlined />} />
      </TableCell>
      <TableCell>{supplies.sort((supplyA, supplyB) => supplyA.supplier.localeCompare(supplyB.supplier)).map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit} account={account} />)}</TableCell>
    </TableRow>
  );
}

function Category({ name, supplies, account, existingCategories, refreshExistingCategories }) {
  if(name.length > 0) {
    return (
      <TableRow>
        <TableCell sx={ name.length > 0 ? {} : { color: "gray", fontStyle: "italic" } }>{name.length > 0 ? name : 'Non catégorisé'}</TableCell>
        <TableCell>{centsAsEurosString(supplies.map(supply => supply.credit).reduce((sum, credit) => sum + credit, 0)) + ' €'}</TableCell>
        <TableCell>{supplies.sort((supplyA, supplyB) => supplyA.supplier.localeCompare(supplyB.supplier)).map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit} account={account} />)}</TableCell>
      </TableRow>
    );
  } else {
    return <Uncategorized supplies={supplies} account={account} existingCategories={existingCategories} refreshExistingCategories={refreshExistingCategories} />
  }
}

export default Category;