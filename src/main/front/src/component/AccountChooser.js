import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';

function AccountChooser({accounts, value, onChange}) {
  return (
    <FormControl sx={{ width: 325 }}>
      <InputLabel id="account-iban-select-label">Account</InputLabel>
      <Select
        id="account-iban-select"
        labelId="account-iban-select-label"
        label="Account"
        value={value}
        onChange={event => onChange(event.target.value)}
      >
        {accounts.map((account) =>
          <MenuItem key={account.iban.asString()} value={account}>{account.iban.asString()} - {account.holder}</MenuItem>
        )}
      </Select>
    </FormControl>
  );
}

export default AccountChooser;