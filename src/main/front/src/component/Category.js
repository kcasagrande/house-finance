import React from 'react';
import { Autocomplete, Avatar, Chip, IconButton, Stack, TableRow, TableCell, TextField, Tooltip } from '@mui/material';
import { AddCircle, Cancel, Flaky, PendingOutlined } from '@mui/icons-material';
import Breakdown from './Breakdown';
import CategoryName from './CategoryName';
import SaveStatusIndicator from './SaveStatusIndicator';
import {centsAsEurosString} from '../Cents';
import configuration from '../Configuration';

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

function CategoryChooser({ account, anchor, existingCategories, refreshExistingCategories, category, operation }) {
  const [value, setValue] = React.useState(category);
  const [isOpen, setOpen] = React.useState(false);
  const [saveState, setSaveState] = React.useState(undefined);
  const [openNewCategoryModal, setOpenNewCategoryModal] = React.useState(false);
  const [holders, setHolders] = React.useState([]);
  
  React.useEffect(() => {
    if(!!account) {
      const url = configuration.api + "/accounts/" + account + "/holders";
      fetch(url)
        .then(response => {
          if(response.ok) {
            return response.json();
          } else {
            throw new Error('Response status to fetching "' + url + '" is ' + response.status);
          }
        })
        .then(value => {
          console.log(JSON.stringify(value, null, 2));
          return value;
        })
        .then(setHolders);
    }
  }, [account]);
  
  function save(setSaveState) {
    setSaveState('pending');
    return new Promise((resolve, reject) => {
      setTimeout(() => { resolve(); }, 2000);
    });
  }
  
  function clear(setSaveState) {
    return new Promise((resolve, reject) => {
      setTimeout(() => { setSaveState(undefined); resolve(); }, 2000);
    });
  }
  
  return (
    <Stack direction="row" alignItems="center" spacing={2}>
      <Autocomplete
        freeSolo
        autoComplete
        options={existingCategories}
        forcePopupIcon={true}
        renderInput={(parameters) => <TextField {...parameters} label={(!isOpen && !value)?"Non catégorisé":"Catégorie"} />}
        onOpen={() => {
          setOpen(true);
        }}
        onClose={() => {
          setOpen(false);
        }}
        onChange={(event, newCategory, reason) => {
          setValue(newCategory);
          save(setSaveState)
            .then(() => { setSaveState('success'); })
            .then(() => { clear(setSaveState); });
        }}
        sx={{width: 300}}
      />
      <SaveStatusIndicator state={saveState} />
    </Stack>
  );
}

function Uncategorized({ supplies, account, existingCategories, refreshExistingCategories, operation }) {
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
        <CategoryChooser
          account={account}
          anchor={anchor}
          existingCategories={existingCategories}
          refreshExistingCategories={refreshExistingCategories}
          operation={operation}
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

function Category({ name, supplies, account, existingCategories, refreshExistingCategories, operation }) {
  if(name.length > 0) {
    return (
      <TableRow>
        <TableCell sx={ name.length > 0 ? {} : { color: "gray", fontStyle: "italic" } }><CategoryName value={name || 'Non catégorisé'} /></TableCell>
        <TableCell>{centsAsEurosString(supplies.map(supply => supply.credit).reduce((sum, credit) => sum + credit, 0)) + ' €'}</TableCell>
        <TableCell>{supplies.sort((supplyA, supplyB) => supplyA.supplier.localeCompare(supplyB.supplier)).map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit} account={account} />)}</TableCell>
      </TableRow>
    );
  } else {
    return <Uncategorized supplies={supplies} account={account} existingCategories={existingCategories} refreshExistingCategories={refreshExistingCategories} operation={operation} />
  }
}

export default Category;