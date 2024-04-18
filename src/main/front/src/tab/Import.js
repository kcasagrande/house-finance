import configuration from '../Configuration';
import { useEffect, useState } from 'react';
import { Button, LinearProgress, Stack, Typography } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import AccountChooser from '../component/AccountChooser';
import FileChooser from '../component/FileChooser';
import ImportReview from '../component/ImportReview';

function Import() {
  const [accounts, setAccounts] = useState([]);
  const [cards, setCards] = useState([]);
  const [operations, setOperations] = useState([]);
  const [account, setAccount] = useState(null);
  const [selectedFile, setSelectedFile] = useState('');
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if(!ready) {
      fetch(configuration.api + "/accounts")
        .then(response => {
          if(response.ok) {
            return response.json();
          } else {
            throw new Error('Response status is ' + response.status);
          }
        })
        .then(json => {
          return json.map(account => {
            return {
              ...account,
              iban: {
                ...account.iban,
                asString() {
                  return account.iban.countryCode + account.iban.checkDigits + account.iban.bban;
                }
              }
            };
          });
        })
        .then(setAccounts)
        .then(() => setReady(true));
    }
  }, [ready]);
  
  useEffect(() => {
    if(!!account) {
      fetch(configuration.api + "/accounts/" + account.iban.asString() + "/cards")
        .then(response => {
          if(response.ok) {
            return response.json();
          } else {
            throw new Error('Response status is ' + response.status);
          }
        })
        .then(setCards);
    }
  }, [account]);

  useEffect(() => {
    if(!!account && !!selectedFile) {
      process(account, selectedFile);
    }
  }, [account, selectedFile]);

  function changeSelectedFile(file) {
    setSelectedFile(file);
  }
  
  function changeSelectedAccount(account) {
    setAccount(account);
  }

  function replaceOperation(newOperation) {
    setOperations(operations.toSpliced(newOperation.key, 1, newOperation));
  }

  function process(account, file) {
    if(!!account && !!file) {
      const formData = new FormData();
      formData.append('statement', file);
      fetch(
        configuration.api + "/statements?account=" + account.iban.asString(),
        {
          'method': 'POST',
          'body': formData
        }
      )
      .then(response => response.json())
      .then(json => json.map((operation, index) => {
        return {
          key: index,
          ...operation,
          isValidCardOperation() {
            return (
              this.method === 'card' &&
              this.card &&
              this.operationDate
            );
          },
          isValidCheckOperation() {
            return (
              this.method === 'check' &&
              this.checkNumber &&
              this.operationDate
            );
          },
          isValidDebitOperation() {
            return (
              this.method === 'debit' &&
              this.operationDate
            );
          },
          isValidTransferOperation() {
            return (
              this.method === 'transfer' &&
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
      .then(_operations => setOperations(_operations));
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
    <Stack direction="column" spacing={2} useFlexGap={true}>
      <Stack direction="row" alignItems="flex-end" justifyContent="center" spacing={2} useFlexGap={true}>
        <AccountChooser accounts={accounts} onChange={changeSelectedAccount} />
        <FileChooser onChange={changeSelectedFile} />
      </Stack>
      <Stack direction="row" alignItems="center" justifyContent="center" spacing={2} useFlexGap={true}>
        <Button variant="outlined" disabled={operations.length <= 0 || validOperations().length !== operations.length}>Submit</Button>
        <Button variant="outlined">Reset</Button>
      </Stack>
      <Progress valid={validOperations().length} total={operations.length} />
      <ImportReview cards={cards} operations={operations} onOperationChange={replaceOperation} />
    </Stack>
  );
}

export default Import;