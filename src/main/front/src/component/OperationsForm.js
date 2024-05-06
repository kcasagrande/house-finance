import { useState } from 'react';
import { Box, Button, Divider, FormControl, InputLabel, MenuItem, Select, Slider, Stack, SvgIcon, TextField, Typography } from '@mui/material';
import BreakdownRepartition from './SearchOperations/BreakdownRepartition';
import NoValueChooser from './SearchOperations/NoValueChooser';
import DayJsDatePicker from '../widget/DayJsDatePicker';
import { BreakdownIcon, CategoryIcon } from '../icons';
import { CalendarMonth as CalendarIcon, Close as CloseIcon, Search as SearchIcon } from '@mui/icons-material';

function FieldSet({title, icon, children}) {
  return (
    <Stack direction="column" alignItems="center" justifyContent="flex-start" spacing={2} useFlexGap>
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
  
  function setCriterion(criterion) {
    return (value) => {
      setCriteria({
        ...criteria,
        [criterion]: value
      });
    };
  }
  
  function submit() {
    onSubmit(criteria);
  }
  
  return (
    <Stack direction="column" spacing={2} alignItems="center" justifyContent="flex-start" useFlexGap flexWrap="wrap">
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
        <Button variant="contained" startIcon={<SearchIcon />} onClick={submit}>Search</Button>
        <Button variant="outlined" startIcon={<CloseIcon />} onClick={() => setCriteria({})}>Reset</Button>
      </Stack>
    </Stack>
  );
}

export default OperationsForm;