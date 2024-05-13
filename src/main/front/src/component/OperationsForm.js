import { useContext, useState } from 'react';
import { Alert, Box, Button, Divider, FormControl, FormHelperText, InputLabel, MenuItem, Select, Slider, Stack, SvgIcon, TextField, Typography } from '@mui/material';
import BreakdownRepartition from './SearchOperations/BreakdownRepartition';
import NoValueChooser from './SearchOperations/NoValueChooser';
import DayJsDatePicker from '../widget/DayJsDatePicker';
import { BreakdownIcon, CategoryIcon } from '../icons';
import { CalendarMonth as CalendarIcon, Close as CloseIcon, Search as SearchIcon } from '@mui/icons-material';
import { AccountsContext } from '../context/AccountsContext';

function FieldSet({title, icon, children, ...props}) {
  return (
    <Stack direction="column" alignItems="center" justifyContent="flex-start" spacing={2} useFlexGap {...props}>
      <Divider textAlign="left" variant="middle" flexItem>
        <Stack direction="row" spacing={1} alignItems="center" justifyContent="flex-start">
          <SvgIcon component={icon} fontSize="small" />
          <Typography variant="overline">{title}</Typography>
        </Stack>
      </Divider>
      <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={2}>
        {children}
      </Stack>
    </Stack>
  );
}

function OperationsForm({onSubmit}) {
  const [criteria, setCriteria] = useState({});
  const accounts = useContext(AccountsContext);
  
  function setCriterion(criterion) {
    return (value) => {
      setCriteria({
        ...criteria,
        [criterion]: value
      });
    };
  }
  
  function canSubmit() {
    return !!criteria['account'];
  }
  
  function submit() {
    onSubmit(criteria);
  }
  
  function reset() {
    setCriteria([]);
  }
  
  return (
    <Stack direction="column" spacing={2} alignItems="center" justifyContent="flex-start" useFlexGap flexWrap="wrap">
      <FormControl variant="outlined" size="small">
        <InputLabel id="account-label">Account</InputLabel>
        <Select
          labelId="account-label"
          sx={{
            width: 320
          }}
          size="small"
          label="Account"
          value={criteria['account'] || ''}
          onChange={(event) => setCriterion('account')(event.target.value)}
        >
          {Object.keys(accounts).map((iban) =>
            <MenuItem key={iban} value={iban}>{iban + " - " + accounts[iban].holder}</MenuItem>
          )}
        </Select>
        <FormHelperText required>Required</FormHelperText>
      </FormControl>
      <FieldSet title="Dates" icon={CalendarIcon}>
        <DayJsDatePicker
          value={criteria['from']}
          onChange={setCriterion('from')}
          label="Not before"
          slotProps={{
            field: {
              clearable: true
            },
            textField: {
              size: 'small'
            }
          }}
        />
        <DayJsDatePicker
          value={criteria['to']}
          onChange={setCriterion('to')}
          label="Not after"
          format="YYYY-MM-DD"
          slotProps={{
            field: {
              clearable: true
            },
            textField: {
              size: 'small'
            }
          }}
        />
      </FieldSet>
      <Stack direction="row" alignItems="center" justifyContent="center" spacing={2} useFlexGap>
        <Button variant="contained" startIcon={<SearchIcon />} onClick={submit} disabled={!canSubmit()}>Search</Button>
        <Button variant="outlined" startIcon={<CloseIcon />} onClick={reset}>Reset</Button>
      </Stack>
    </Stack>
  );
}

export default OperationsForm;