import configuration from '../Configuration';
import { useEffect, useState } from 'react';
import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';

function AccountChooser({accounts, onChange}) {
  return (
    <FormControl sx={{ width: 325 }}>
      <InputLabel id="account-iban-select-label">Account</InputLabel>
      <Select
        id="account-iban-select"
        labelId="account-iban-select-label"
        label="Account"
        defaultValue={''}
        onChange={event => onChange(event.target.value)}
      >
        {accounts.map((account) =>
          <MenuItem key={account.iban} value={account}>{account.iban} - {account.holder}</MenuItem>
        )}
      </Select>
    </FormControl>
  );
}

export default AccountChooser;