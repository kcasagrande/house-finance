import { FormControl, InputLabel, MenuItem, Select, Stack, TextField } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers';
import CardChooser from './CardChooser';
import BreakdownRepartition from './SearchOperations/BreakdownRepartition';
import Method from './Method';

function SearchOperations() {
  return (
    <Stack direction="row" spacing={2}>
      <TextField label="Search a reference" size="small" />
      <FormControl sx={{ width: 200 }} size="small">
        <InputLabel id="category-label">Category</InputLabel>
        <Select label="Category" labelId="category-label" size="small">
          <MenuItem>Essence</MenuItem>
          <MenuItem>Péage</MenuItem>
        </Select>
      </FormControl>
      <BreakdownRepartition />
      <Method onChange={() => null} />
      <TextField label="Search label" size="small" />
      <DatePicker
        label="Not before"
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
      <DatePicker
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
      <TextField label="Credit" type="number" size="small" />
      <CardChooser operation={{ card: '???' }} />
    </Stack>
  );
}

export default SearchOperations;