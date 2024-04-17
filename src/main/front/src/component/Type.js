import { useState } from 'react';
import { MenuItem, Select } from '@mui/material';
import OperationType from './OperationType';

function Type({defaultValue, onChange}) {
  const [value, setValue] = useState(defaultValue);
  return (
    <Select
      value={value}
      onChange={(event) => {
        setValue(event.target.value);
        onChange(event.target.value);
      }}
    >
      <MenuItem value="card"><OperationType type="card" /></MenuItem>
      <MenuItem value="check"><OperationType type="check" /></MenuItem>
      <MenuItem value="debit"><OperationType type="debit" /></MenuItem>
      <MenuItem value="transfer"><OperationType type="transfer" /></MenuItem>
    </Select>
  );
}

export default Type;