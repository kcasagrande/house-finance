import React from 'react';
import { Autocomplete, Avatar, Chip, Dialog, DialogTitle, TableRow, TableCell, TextField } from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';

function Unsupplied(props) {
  const { credit } = props;
  const [open, setOpen] = React.useState(false);
  const [remaining, setRemaining] = React.useState(0);
  
  function handleCreate() {
    console.log("TODO Create breakdown");
    setOpen(true);
  }
  
  function handleClose() {
    setOpen(false);
  }
  
  function handleChange(event) {
    setRemaining(credit - Number.parseFloat(event.target.value));
  }
  
  return (
    <>
      <Chip
        sx={{ marginRight: '1ex' }}
        variant="outlined"
        label={credit + ' €'}
        onDelete={handleCreate}
        deleteIcon={<AddCircleIcon />}
      />
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Ventiler la somme</DialogTitle>
        <TextField label="Montant" type="number" onChange={handleChange} defaultValue={credit} margin="dense" />
        <Autocomplete freeSolo forcePopupIcon={true} options={['A', 'B']} renderInput={(parameters) => <TextField {...parameters} label="Catégorie" margin="dense" />} />
        <Autocomplete freeSolo forcePopupIcon={true} options={['A', 'B']} renderInput={(parameters) => <TextField {...parameters} label="Fournisseur" margin="dense" />} />
        <TextField disabled={true} label="Montant restant après ventilation" type="number" margin="dense" value={remaining} />
      </Dialog>
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
      />
    );
  } else {
    return (<Unsupplied credit={credit / 100.0} />);
  }
}

function Category(props) {
  const { name, supplies } = props;

  return (
    <TableRow>
      <TableCell>{name}</TableCell>
      <TableCell>{supplies.map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit}/>)}</TableCell>
    </TableRow>
  );
}

export default Category;