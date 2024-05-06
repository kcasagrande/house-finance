import { Alert, Autocomplete, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, FormControl, IconButton, InputLabel, MenuItem, Select, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import { amount } from '../format';
import AddIcon from '@mui/icons-material/Add';
import { BreakdownIcon } from '../icons';
import CancelIcon from '@mui/icons-material/Cancel';
import CloseIcon from '@mui/icons-material/Close';
import ContrastIcon from '@mui/icons-material/Contrast';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import SendIcon from '@mui/icons-material/Send';
import AmountField from './AmountField';
import CategoryName from './CategoryName';
import Holder from './Holder';

function NewBreakdownModal({open, onClose, operation, categories, holders}) {
  const [categorizedAmount, setCategorizedAmount] = useState(operation.unassignedCredit);
  const [category, setCategory] = useState('');
  const [supplier, setSupplier] = useState('');

  function handleAmountChange(value) {
    setCategorizedAmount(value);
  }

  function handleCategoryChange(event, value) {
    setCategory(value);
  }

  function handleSupplierChange(value) {
    setSupplier(value);
  }

  function handleClose(event) {
    onClose(event);
  }

  function validateAmount(amount, unassignedCredit) {
    return (
      Math.abs(unassignedCredit) - Math.abs(amount) >= 0 &&
      Math.abs(unassignedCredit - amount) <= Math.abs(unassignedCredit)
    );
  }
  
  function modifyOperation() {
    const uncategorized = operation.breakdown.find((entry) => !entry.category);
    return {
      ...operation,
      breakdown: [
        {...(operation.breakdown.filter((entry) => (!!entry.category || !!entry.supplier)))},
        {
          category: category || null,
          credit: categorizedAmount,
          comment: null,
          supplier: supplier || null
        },
        {
          category: null,
          credit: uncategorized.credit - categorizedAmount,
          comment: uncategorized.comment,
          supplier: null
        }
      ]
        .filter((entry) => Object.keys(entry).length !== 0)
        .filter((entry) => entry.credit !== 0)
    };
  }
  
  function canSubmit() {
    return categorizedAmount !== 0 && validateAmount(categorizedAmount, operation.unassignedCredit) && (!!category || !!supplier);
  }
  
  function submit() {
    const newOperation = modifyOperation();
    console.log(JSON.stringify(newOperation, null, 2));
  }

  return (
    <Dialog
      open={open}
      onClose={handleClose}
    >
      <DialogTitle>New breakdown</DialogTitle>
      <DialogContent>
        <Stack direction="column" alignItems="center" spacing={2}>
          <Autocomplete
            size="small"
            fullWidth={true}
            loading={true}
            options={categories}
            renderInput={(parameters) =>
              <TextField
                {...parameters}
                label="Category"
              />
            }
            value={category}
            onChange={handleCategoryChange}
          />
          <Stack direction="row" spacing={1} alignItems="center">
            <AmountField
              size="small"
              label="Amount"
              value={categorizedAmount / 100}
              onChange={handleAmountChange}
              error={!validateAmount(categorizedAmount, operation.unassignedCredit)}
            />
            <IconButton onClick={() => handleAmountChange(operation.unassignedCredit)}>
              <KeyboardDoubleArrowLeftIcon />
            </IconButton>
            {
              validateAmount(categorizedAmount, operation.unassignedCredit)
              ? (
                <Alert severity="info" sx={{ minWidth: 250 }}>{amount(operation.unassignedCredit - categorizedAmount)} will remain.</Alert>
              ) : (
                <Alert severity="error" sx={{ minWidth: 250 }}>{'Can\'t exceed ' + amount(operation.unassignedCredit) + '.'}</Alert>
              )
            }
          </Stack>
          <FormControl fullWidth size="small">
            <InputLabel id='supplier-label' shrink={!!supplier}>Supplier</InputLabel>
            <Select
              labelId='supplier-label'
              label={!!supplier ? 'Supplier' : ''}
              value={supplier}
              onChange={(event) => handleSupplierChange(event.target.value)}
              endAdornment={!!supplier ? <IconButton onClick={(event) => handleSupplierChange('')} sx={{ marginRight: '1.1ex' }}><CloseIcon fontSize="small" /></IconButton> : <></>}
            >
              {holders.map((holder) =>
                <MenuItem key={holder.id} value={holder.id}>{<Holder value={holder} size="small" />}</MenuItem>
              )}
            </Select>
          </FormControl>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button variant="outlined" color="secondary" onClick={handleClose} startIcon={<CancelIcon />}>Cancel</Button>
        <Button variant="contained" disabled={!canSubmit()} startIcon={<SendIcon />} onClick={submit}>Submit</Button>
      </DialogActions>
    </Dialog>
  );
}

export default NewBreakdownModal;