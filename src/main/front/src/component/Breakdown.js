import React from 'react';
import { Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormLabel, FormControlLabel, Radio, RadioGroup, Stack, TextField } from '@mui/material';
import { centsAsEurosString, parseEuros } from '../Cents'

function Breakdown(props) {
  const { credit, open, onClose } = props;
  const [error, setError] = React.useState(false);
  const [amount, setAmount] = React.useState(credit);
  const [manual, setManual] = React.useState(false);
  const [manualAmount, setManualAmount] = React.useState(credit);
  
  function handleCommit(event) {
    alert("will save " + centsAsEurosString(amount) + " €");
    onClose();
  }
  
  function setAmountWithValidation(newAmount) {
    const isError = Math.abs(newAmount) > Math.abs(credit) || Math.sign(newAmount) !== Math.sign(credit);
    setError(isError);
    setAmount(newAmount);
  }
  
  function handleModeChange(event) {
    switch(event.target.value) {
      case "full":
        setManual(false);
        setAmountWithValidation(credit);
        break;
      case "half":
        setManual(false);
        setAmountWithValidation(credit / 2);
        break;
      default:
        setManual(true);
        setAmountWithValidation(manualAmount);
    }
  }
  
  function reset() {
    setManual(false);
    setAmountWithValidation(credit);
    setManualAmount(credit);
  }
  
  function handleClose() {
    reset();
    onClose();
  }
  
  function handleManualChange(event) {
    const newManualAmount = parseEuros(event.target.value);
    setManualAmount(newManualAmount);
    setAmountWithValidation(newManualAmount);
  }
  
  return (
    <Dialog open={open} onClose={handleClose}>
      <DialogTitle>Ventiler la somme</DialogTitle>
      <DialogContent>
        <FormControl>
          <FormLabel>Montant</FormLabel>
            <RadioGroup
              defaultValue="full"
              onChange={handleModeChange}
            >
              <FormControlLabel value="full" control={<Radio />} label={"Totalité : " + centsAsEurosString(credit) + " €"} />
              <FormControlLabel value="half" control={<Radio />} label={"Moitié : " + centsAsEurosString(credit / 2) + " €"} />
              <Stack direction="row">
                <FormControlLabel value="other" control={<Radio />} label="Autre montant :" />
                <TextField type="number" variant="standard" disabled={!manual} defaultValue={centsAsEurosString(credit)} onChange={handleManualChange} error={error} />
              </Stack>
            </RadioGroup>
            <TextField InputProps={{readOnly: true}} label="Reste à ventiler (indicatif)" type="text" value={centsAsEurosString(credit - amount)} error={error} />
        </FormControl>
        <Autocomplete freeSolo forcePopupIcon={true} options={['A', 'B']} renderInput={(parameters) => <TextField {...parameters} label="Catégorie" margin="dense" />} />
        <Autocomplete freeSolo forcePopupIcon={true} options={['A', 'B']} renderInput={(parameters) => <TextField {...parameters} label="Fournisseur" margin="dense" />} />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Annuler</Button>
        <Button onClick={handleCommit} autoFocus disabled={error}>Enregistrer</Button>
      </DialogActions>
    </Dialog>
  );
}

export default Breakdown;