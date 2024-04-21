import { useEffect, useState } from 'react';
import { IconButton, InputAdornment, TextField } from '@mui/material';
import CancelIcon from '@mui/icons-material/Cancel';

function CheckNumberInput({operation, onChange}) {
  const [number, setNumber] = useState(operation.checkNumber);
  
  useEffect(() => {
    onChange(number);
  }, [number, onChange]);
  
  return (
    <TextField
      size="small"
      label="Check number"
      value={number}
      type="number"
      onChange={(event) => setNumber(event.target.value)}
      onKeyPress={(event) => {
        if(event.key === 'Enter') {
          event.target.blur();
        }
      }}
      onBlur={(event) => setNumber(event.target.value)}
      InputProps={{
        endAdornment: <InputAdornment position="end">
          <IconButton size="small" onClick={(event) => {
              setNumber('');
              event.target.value = '';
            }}>
            <CancelIcon fontSize="inherit" />
          </IconButton>
        </InputAdornment>
      }}
    />
  );
}

export default CheckNumberInput;