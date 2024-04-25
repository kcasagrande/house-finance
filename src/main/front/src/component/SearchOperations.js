import { useEffect, useState } from 'react';
import { Button, FormControl, InputLabel, MenuItem, Select, Stack, TextField } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers';
import CloseIcon from '@mui/icons-material/Close';
import SearchIcon from '@mui/icons-material/Search';
import dayjs from 'dayjs';
import configuration from '../Configuration';
import DayJsDatePicker from '../widget/DayJsDatePicker';
import CardChooser from './CardChooser';
import BreakdownRepartition from './SearchOperations/BreakdownRepartition';
import Categories from './SearchOperations/Categories';
import Method from './Method';

function SearchOperations({callback, error = console.log}) {
  const [criteria, setCriteria] = useState({});
  
  function setCriterion(key) {
    return (value) => {
      const criterion = {};
      criterion[key] = value;
      setCriteria({...criteria, ...criterion});
    };
  }
  
  function resetCriteria() {
    setCriteria({});
  }
  
  function toQueryParams(_criteria) {
    function queryParam(param, key) {
      return (!!_criteria[key] ? param + '=' + _criteria[key] : undefined);
    }
    return [
      queryParam('from', 'from'),
      queryParam('to', 'to')
    ]
      .filter((criterion) => !!criterion)
      .join('&');
  }
  
  function submit() {
    return fetch(configuration.api + '/operations?' + toQueryParams(criteria))
      .then((response) => {
        if(response.ok) {
          return response.json();
        } else {
          return response.text()
            .then((text) => {
              throw new Error(text);
            });
        }
      })
      .then(callback)
      .catch(error);
  }
  
  return (
    <Stack direction="row" spacing={2} alignItems="center" justifyContent="start" useFlexGap flexWrap="wrap">
      <TextField label="Search a reference" size="small" value={criteria['reference'] || ''} onChange={(event) => setCriterion('reference')(event.target.value)}/>
      <FormControl sx={{ width: 200 }} size="small">
        <InputLabel id="category-label">Category</InputLabel>
        <Select label="Category" labelId="category-label" size="small">
          <MenuItem>Essence</MenuItem>
          <MenuItem>Péage</MenuItem>
        </Select>
      </FormControl>
      <Categories />
      <BreakdownRepartition />
      <Method onChange={() => null} />
      <TextField label="Search label" size="small" />
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
      <TextField label="Credit" type="number" size="small" />
      <CardChooser operation={{ card: '???' }} />
      <Button variant="contained" startIcon={<SearchIcon />} onClick={submit}>Search</Button>
      <Button variant="outlined" startIcon={<CloseIcon />} onClick={resetCriteria}>Reset</Button>
    </Stack>
  );
}

export default SearchOperations;