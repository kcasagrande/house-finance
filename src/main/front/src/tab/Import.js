import configuration from '../Configuration';
import { useEffect, useState, useRef } from 'react';
import { Box, Button, CircularProgress, Container, FormControl, Input, InputLabel, MenuItem, Select, Stack, Step, StepButton, StepLabel, Stepper, TextField } from '@mui/material';
import { FileUpload } from '@mui/icons-material';
import AccountChooser from '../component/AccountChooser';
import FileChooser from '../component/FileChooser';
import ImportReview from '../component/ImportReview';

function Import() {
  const [status, setStatus] = useState('initializing');
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [selectedFile, setSelectedFile] = useState('');
  const [parsedRows, setParsedRows] = useState([]);

  function changeSelectedFile(file) {
    setSelectedFile(file);
    process(selectedAccount, file);
  }
  
  function changeSelectedAccount(account) {
    setSelectedAccount(account);
    process(account, selectedFile);
  }
  }

  function process(account, file) {
    if(!!account && !!file) {
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
          <AccountChooser onChange={changeSelectedAccount} />
          <FileChooser onChange={changeSelectedFile} />
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