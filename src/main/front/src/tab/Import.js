import configuration from '../Configuration';
import React from 'react';
import { Box, Button, CircularProgress, Container, Input, Stack, Step, StepButton, StepLabel, Stepper, TextField } from '@mui/material';
import { FileUpload } from '@mui/icons-material';
import ImportReview from '../component/ImportReview';

function Import() {
  const [status, setStatus] = React.useState('pending');
  const [account, setAccount] = React.useState('');
  const [selectedFile, setSelectedFile] = React.useState('');
  const [operations, setOperations] = React.useState({});
  const fileInput = React.useRef();

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
      setStatus('loading');
      const formData = new FormData();
      formData.append('statement', file);
      return fetch(
        configuration.api + "/statements?account=" + account,
        {
          'method': 'POST',
          'body': formData
        }
      )
      .then(response => response.json())
      .then(json => setOperations(json))
      .then(() => setStatus('reviewing'));
    }
  }

  return (
    <Container fixed>
      <Stack direction="column" spacing={2} useFlexGap={true}>
        <Stack direction="row" alignItems="flex-end" justifyContent="center" spacing={2} useFlexGap={true}>
          <TextField variant="standard" label="Account IBAN" onBlur={event => changeAccount(event.target.value)}></TextField>
          <input ref={fileInput} type="file" accept="text/csv,.csv" onChange={event => changeSelectedFile(event.target.files[0])} style={{display: 'none'}} />
          <Button variant="outlined" onClick={() => fileInput.current.click()}>Select file</Button>
          <TextField variant="standard" InputProps={{readOnly: true}} value={selectedFile.name}></TextField>
        </Stack>
        <Stack direction="row" alignItems="flex-end" justifyContent="center" spacing={2} useFlexGap={true}>
          <Button variant="outlined" disabled={status !== 'reviewing'}>Submit</Button>
          <Button variant="outlined" disabled={status !== 'reviewing'}>Reset</Button>
        </Stack>
        {status === 'loading'
        ? <Container>
            <Stack direction="column" alignItems="center">
              <CircularProgress />
            </Stack>
          </Container>
        : <></>
        }
        {status === 'reviewing'
        ? <ImportReview operations={operations} />
        : <></>
        }
      </Stack>
    </Container>
  );
}

export default Import;