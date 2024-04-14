import configuration from '../Configuration';
import { useEffect, useState } from 'react';
import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';

function fetchAccounts(callback) {
  return fetch(configuration.api + "/accounts")
    .then(response => {
      if(response.ok) {
        return response.json();
      } else {
        throw new Error('Response status is ' + response.status);
      }
    })
    .then(callback);
}

function AccountChooser({onChange}) {
  const [ready, setReady] = useState(false);
  const [availableAccounts, setAvailableAccounts] = useState([]);
  
  useEffect(() => {
    if(!ready) {
      fetchAccounts(setAvailableAccounts)
        .then(() => setReady(true));
    }
  }, [ready]);
  
  return (
    <FormControl sx={{ width: 325 }}>
      <InputLabel id="account-iban-select-label">Account</InputLabel>
      <Select
        id="account-iban-select"
        labelId="account-iban-select-label"
        label="Account"
        onChange={event => onChange(event.target.value)}
      >
        {availableAccounts.map((account) =>
          <MenuItem key={account.iban} value={account}>{account.iban} - {account.holder}</MenuItem>
        )}
      </Select>
    </FormControl>
  );
}

export default AccountChooser;