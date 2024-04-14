import configuration from '../Configuration';
import { useState } from 'react';
import { Button, Container, LinearProgress, Stack, Typography } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import AccountChooser from '../component/AccountChooser';
import FileChooser from '../component/FileChooser';
import ImportReview from '../component/ImportReview';

function Import() {
  const [status, setStatus] = useState('initializing');
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [availableCards, setAvailableCards] = useState([]);
  const [selectedFile, setSelectedFile] = useState('');
  const [operations, setOperations] = useState([]);

  function changeSelectedFile(file) {
    setSelectedFile(file);
    process(selectedAccount, file);
  }
  
  function changeSelectedAccount(account) {
    setSelectedAccount(account);
    fetch(configuration.api + "/accounts/" + account.iban + "/cards")
      .then(response => {
        if(response.ok) {
          return response.json();
        } else {
          throw new Error('Response status is ' + response.status);
        }
      })
      .then(setAvailableCards)
      .then(() => process(account, selectedFile));
  }

  function replaceOperation(index, newOperation) {
    setOperations(operations.toSpliced(index, 1, newOperation));
  }

  function process(account, file) {
    if(!!account && !!file) {
      setStatus('parsing');
      const formData = new FormData();
      formData.append('statement', file);
      return fetch(
        configuration.api + "/statements?account=" + account.iban,
        {
          'method': 'POST',
          'body': formData
        }
      )
      .then(response => response.json())
      .then(json => json.map(operation => {
        return {
          ...operation,
          isValidCardOperation() {
            return (
              this.type === 'card' &&
              this.card &&
              this.operationDate
            );
          },
          isValidCheckOperation() {
            return (
              this.type === 'check' &&
              this.checkNumber &&
              this.operationDate
            );
          },
          isValidDebitOperation() {
            return (
              this.type === 'debit' &&
              this.operationDate
            );
          },
          isValidTransferOperation() {
            return (
              this.type === 'transfer' &&
              this.operationDate
            );
          },
          isValid() {
            return (
              this.isValidCardOperation()
              || this.isValidCheckOperation()
              || this.isValidDebitOperation()
              || this.isValidTransferOperation()
            );
          }
        };
      }))
      .then(_operations => setOperations(_operations))
      .then(() => setStatus('ready'));
    }
  }

  function validOperations() {
    return operations.filter(operation => operation.isValid());
  }

  function Progress({valid, total}) {
    return (
      <>
        <Stack direction="row" alignItems="center" justifyContent="center" spacing={2} useFlexGap={true}>
           {(valid === total ? <CheckCircleIcon sx={{ color: 'green' }} /> : <ErrorIcon sx={{ color: 'red' }} />)}
           <Typography variant="button">{valid} / {total}</Typography>
        </Stack>
        <LinearProgress variant="determinate" value={valid * 100 / total} />
      </>
    );
  }

  return (
    <Container fixed>
      <Stack direction="column" spacing={2} useFlexGap={true}>
        <Stack direction="row" alignItems="flex-end" justifyContent="center" spacing={2} useFlexGap={true}>
          <AccountChooser onChange={changeSelectedAccount} />
          <FileChooser onChange={changeSelectedFile} />
        </Stack>
        <Stack direction="row" alignItems="center" justifyContent="center" spacing={2} useFlexGap={true}>
          <Button variant="outlined" disabled={status !== 'reviewing'}>Submit</Button>
          <Button variant="outlined" disabled={status !== 'reviewing'}>Reset</Button>
        </Stack>
        {(status === 'ready' ? <Progress valid={validOperations().length} total={operations.length} /> : <></>)}
        <ImportReview account={selectedAccount && selectedAccount.iban} cards={availableCards} status={status} operations={operations} onChange={replaceOperation} />
      </Stack>
    </Container>
  );
}

export default Import;