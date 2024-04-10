import configuration from '../Configuration';
import { useEffect, useState, useRef } from 'react';
import { Box, Button, CircularProgress, Container, FormControl, Input, InputLabel, MenuItem, Select, Stack, Step, StepButton, StepLabel, Stepper, TextField } from '@mui/material';
import { FileUpload } from '@mui/icons-material';
import ImportReview from '../component/ImportReview';

function Import() {
  const [status, setStatus] = useState('initializing');
  const [availableAccounts, setAvailableAccounts] = useState([]);
  const [account, setAccount] = useState('');
  const [selectedFile, setSelectedFile] = useState('');
  const [parsedRows, setParsedRows] = useState([]);
  const fileInput = useRef();

  useEffect(() => {
    if(status === 'initializing') {
      fetchAccounts()
        .finally(() => setStatus('pending'));
    }
  }, []);

  function fetchAccounts() {
    return fetch(configuration.api + "/accounts")
      .then(response => {
        if(response.ok) {
          return response.json()
        } else {
          throw new Error('Response status is ' + response.status);
        }
      })
      .then(setAvailableAccounts);
  }

  function changeSelectedFile(file) {
    setSelectedFile(file);
    process(account, file);
  }
  
  function changeAccount(iban) {
    setAccount(iban);
    process(iban, selectedFile);
  }

  function process(iban, file) {
    if(!!iban && !!file) {
      setStatus('parsing');
      const formData = new FormData();
      formData.append('statement', file);
      return fetch(
        configuration.api + "/statements",
        {
          'method': 'POST',
          'body': formData
        }
      )
      .then(response => response.json())
      .then(json => setParsedRows(json))
      .then(() => setStatus('reviewing'));
    }
  }

  return (
    <Container fixed>
      <Stack direction="column" spacing={2} useFlexGap={true}>
        <Stack direction="row" alignItems="flex-end" justifyContent="center" spacing={2} useFlexGap={true}>
          <FormControl sx={{ width: 325 }}>
            <InputLabel id="account-iban-select-label">Account IBAN</InputLabel>
            <Select id="account-iban-select" labelId="account-iban-select-label" label="Account IBAN" onChange={event => changeAccount(event.target.value.iban)}>
              {availableAccounts.map((account) =>
                <MenuItem value={account}>{account.iban} - {account.holder}</MenuItem>
              )}
            </Select>
          </FormControl>
          <input ref={fileInput} type="file" accept="text/csv,.csv" onChange={event => changeSelectedFile(event.target.files[0])} style={{display: 'none'}} />
          <Button variant="outlined" onClick={() => fileInput.current.click()}>Select file</Button>
          <TextField variant="standard" InputProps={{readOnly: true}} value={selectedFile.name}></TextField>
        </Stack>
        <Stack direction="row" alignItems="flex-end" justifyContent="center" spacing={2} useFlexGap={true}>
          <Button variant="outlined" disabled={status !== 'reviewing'}>Submit</Button>
          <Button variant="outlined" disabled={status !== 'reviewing'}>Reset</Button>
        </Stack>
        {status === 'parsing'
        ? <Container>
            <Stack direction="column" alignItems="center">
              <CircularProgress />
            </Stack>
          </Container>
        : <></>
        }
        {status === 'reviewing'
        ? <ImportReview rows={parsedRows} />
        : <></>
        }
      </Stack>
    </Container>
  );
}

export default Import;