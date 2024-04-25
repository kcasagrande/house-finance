import { Alert, Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, IconButton, SpeedDial, SpeedDialAction, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import amount from '../amount';
import AddIcon from '@mui/icons-material/Add';
import BreakdownIcon from './icon/BreakdownIcon';
import CancelIcon from '@mui/icons-material/Cancel';
import ContrastIcon from '@mui/icons-material/Contrast';
import FlashOnIcon from '@mui/icons-material/FlashOn';
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import SendIcon from '@mui/icons-material/Send';
import AmountField from './AmountField';

function NewCategoryModal({open, onClose, operation, holders = []}) {
  const [categorizedAmount, setCategorizedAmount] = useState(getOperationCredit());
  const [category, setCategory] = useState('');

  function handleAmountChange(value) {
    setCategorizedAmount(value);
  }

  function handleCategoryChange(event, value) {
    setCategory(value);
  }

  function handleClose(event) {
    onClose(event);
  }

  function getOperationCredit() {
    return operation.breakdown.map((breakdown) => breakdown.credit).reduce((sum, element) => sum + element, 0);
  }

  function validateAmount(amount, operationCredit) {
    return (
      Math.abs(operationCredit) - Math.abs(amount) >= 0 &&
      Math.abs(operationCredit - amount) <= Math.abs(operationCredit)
    );
  }

  return (
    <Dialog
      open={open}
      onClose={handleClose}
    >
      <DialogTitle>Assign category</DialogTitle>
      <DialogContent>
        <Stack direction="column" alignItems="center" spacing={2}>
          <Autocomplete
            size="small"
            fullWidth={true}
            loading={true}
            options={['Essence', 'Péage']}
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
              error={!validateAmount(categorizedAmount, getOperationCredit())}
            />
            {
              validateAmount(categorizedAmount, getOperationCredit())
              ? (
                <>
                  <IconButton onClick={() => handleAmountChange(getOperationCredit())}>
                    <KeyboardDoubleArrowLeftIcon />
                  </IconButton>
                  <Alert severity="info" sx={{ minWidth: 250 }}>{amount(getOperationCredit() - categorizedAmount)} will remain.</Alert>
                </>
              ) : (
                <>
                  <IconButton onClick={() => handleAmountChange(getOperationCredit())}>
                    <KeyboardDoubleArrowLeftIcon />
                  </IconButton>
                  <Alert severity="error" sx={{ minWidth: 250 }}>{'Can\'t exceed ' + amount(getOperationCredit()) + '.'}</Alert>
                </>
              )
            }
          </Stack>
          <Divider flexItem textAlign="left" variant="middle">
            <Stack direction="row" alignItems="center" spacing={1}>
              <BreakdownIcon fontSize="small" />
              <Typography variant="overline">Breakdown</Typography>
            </Stack>
          </Divider>
          <Stack direction="column" spacing={2}>
            <SpeedDial
              ariaLabel="Quick breakdown"
              direction="right"
              icon={<FlashOnIcon fontSize="small" />}
              FabProps={{ size: 'small' }}
            >
              <SpeedDialAction name="Equivalent" tooltipTitle="Equivalent" icon={<ContrastIcon fontSize="small" />} />
              {holders.map((holder) =>
                <SpeedDialAction name={'100% ' + holder.name} icon={<ContrastIcon fontSize="small" />} />
              )}
            </SpeedDial>
            <SpeedDial
              ariaLabel="Add an holder"
              direction="right"
              icon={<AddIcon fontSize="small" />}
              FabProps={{ size: 'small' }}
            >
              <SpeedDialAction name="Equivalent" icon={<ContrastIcon fontSize="small" />} />
            </SpeedDial>
          </Stack>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button variant="outlined" color="secondary" onClick={handleClose} startIcon={<CancelIcon />}>Cancel</Button>
        <Button variant="contained" disabled={categorizedAmount === 0 || !validateAmount(categorizedAmount, getOperationCredit()) || !category} startIcon={<SendIcon />}>Submit</Button>
      </DialogActions>
    </Dialog>
  );
}

export default NewCategoryModal;