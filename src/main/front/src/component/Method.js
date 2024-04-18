import { useState } from 'react';
import { FormControl, MenuItem, Select } from '@mui/material';
import OperationMethod from './OperationMethod';

function Method({defaultValue, onChange}) {
  const [value, setValue] = useState(defaultValue);
  return (
    <FormControl size="small">
      <Select
        value={value}
        onChange={(event) => {
          setValue(event.target.value);
          onChange(event.target.value);
        }}
      >
        <MenuItem value="card"><OperationMethod method="card" /></MenuItem>
        <MenuItem value="check"><OperationMethod method="check" /></MenuItem>
        <MenuItem value="debit"><OperationMethod method="debit" /></MenuItem>
        <MenuItem value="transfer"><OperationMethod method="transfer" /></MenuItem>
      </Select>
    </FormControl>
  );
}

export default Method;