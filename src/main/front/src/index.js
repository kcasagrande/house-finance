import { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';
import { BrowserRouter, RouterProvider, Routes, Route } from 'react-router-dom';
import Import from './tab/Import';
import Details from './tab/Details';
import Reports from './tab/Reports';
import reportWebVitals from './reportWebVitals';
import { fetchAccounts, fetchHolders } from './application/fetch-data';
import Account from './business/Account';
import Operation from './business/Operation';
import { AccountsContext } from './context/AccountsContext';
import { HoldersContext } from './context/HoldersContext';

function Root() {
  const [initialized, setInitialized] = useState(false);
  const [accounts, setAccounts] = useState([]);
  const [account, _setAccount] = useState(JSON.parse(localStorage.getItem('account')) || '');
  const [operations, _setOperations] = useState((JSON.parse(localStorage.getItem('operations')) || []).map(Operation.fromObject));
  const [holders, setHolders] = useState([]);

  useEffect(() => {
    if(!initialized) {
      fetchAccounts()
        .then(accounts =>
          accounts
            .reduce((_accounts, _account) => {
              return {
                ..._accounts,
                [_account.ibanAsString]: _account
              }
            }, {})
        )
        .then(setAccounts)
        .then(() => setInitialized(true));
    }
  }, [initialized]);
  
  useEffect(() => {
    if(!!account) {
      fetchHolders(account)
        .then(setHolders);
    }
  }, [account]);

  function setOperations(operations) {
    localStorage.setItem('operations', JSON.stringify(operations));
    _setOperations(operations);
  }

  function setAccount(iban) {
    localStorage.setItem('account', JSON.stringify(iban));
    _setAccount(iban);
  }

  return (
    <AccountsContext.Provider value={accounts}>
      <HoldersContext.Provider value={holders}>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<App operations={operations} onOperationsChange={setOperations} account={account} onAccountChange={setAccount} />}>
              <Route path="/import" element={<Import />} />
              <Route path="/details" element={<Details operations={operations} />} />
              <Route path="/reports" element={<Reports operations={operations} holders={holders}/>} />
            </Route>
          </Routes>
        </BrowserRouter>
      </HoldersContext.Provider>
    </AccountsContext.Provider>
  );
}

createRoot(document.getElementById('root')).render(
  <Root />
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
