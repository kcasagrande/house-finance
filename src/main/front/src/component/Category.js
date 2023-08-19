import React from 'react';
import { Autocomplete, Avatar, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TableRow, TableCell, TextField, Tooltip } from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CancelIcon from '@mui/icons-material/Cancel';

function Unsupplied(props) {
  const { credit } = props;
  const [open, setOpen] = React.useState(false);
  const [remaining, setRemaining] = React.useState(0);
  const [error, setError] = React.useState(false);
  
  function handleCreate() {
    console.log("TODO Create breakdown");
    setOpen(true);
  }
  
  function handleClose() {
    setOpen(false);
    setRemaining(0);
  }
  
  function handleChange(event) {
    setError(Math.abs(Number.parseFloat(event.target.value).toFixed(2)) > Math.abs(credit.toFixed(2)));
    setRemaining(credit - Number.parseFloat(event.target.value));
  }
  
  return (
    <>
      <Chip
        sx={{ marginRight: '1ex' }}
        variant="outlined"
        label={credit + ' €'}
        onDelete={handleCreate}
        deleteIcon={<Tooltip title="Ventiler la somme"><AddCircleIcon /></Tooltip>}
      />
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Ventiler la somme</DialogTitle>
        <DialogContent>
          <Stack direction="row" spacing={1} mt={1} mb={1}>
            <TextField label="Montant" type="number" onChange={handleChange} defaultValue={credit} error={error} />
            <TextField disabled={true} label="Montant restant après ventilation" type="number" value={remaining.toFixed(2)} error={error} />
          </Stack>
          <Autocomplete freeSolo forcePopupIcon={true} options={['A', 'B']} renderInput={(parameters) => <TextField {...parameters} label="Catégorie" margin="dense" />} />
          <Autocomplete freeSolo forcePopupIcon={true} options={['A', 'B']} renderInput={(parameters) => <TextField {...parameters} label="Fournisseur" margin="dense" />} />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Annuler</Button>
          <Button onClick={handleClose} autoFocus>Enregistrer</Button>
        </DialogActions>
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
        deleteIcon={<Tooltip title="Supprimer cette ventilation"><CancelIcon /></Tooltip>}
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
      <TableCell sx={ name.length > 0 ? {} : { color: "gray", fontStyle: "italic" } }>{name.length > 0 ? name : 'Non catégorisé'}</TableCell>
      <TableCell>{supplies.map(supply => <Supply key={'supply-' + supply.supplier} supplier={supply.supplier} credit={supply.credit}/>)}</TableCell>
    </TableRow>
  );
}

export default Category;