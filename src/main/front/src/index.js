import { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';
import { BrowserRouter, RouterProvider, Routes, Route } from 'react-router-dom';
import Import from './tab/Import';
import Details from './tab/Details';
import OperationsLoader from './tab/OperationsLoader';
import Reports from './tab/Reports';
import reportWebVitals from './reportWebVitals';
import { fetchAccounts, fetchHolders } from './application/fetch-data';
import Account from './business/Account';

function Root() {
  const [initialized, setInitialized] = useState(false);
  const [accounts, setAccounts] = useState([]);
  const [account, setAccount] = useState('');
  const [operations, setOperations] = useState([]);
  const [holders, setHolders] = useState([]);

  useEffect(() => {
    if(!initialized) {
      fetchAccounts()
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

  function onAccountChange(event) {
    setAccount(event.target.value);
    loadOperations([]);
  }

  function loadOperations(_operations) {
    setOperations(_operations);
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App operations={operations} accounts={accounts} account={account} onAccountChange={onAccountChange} />}>
          <Route path="/load" element={<OperationsLoader onLoad={loadOperations} />} />
          <Route path="/import" element={<Import />} />
          <Route path="/details" element={<Details operations={operations} />} />
          <Route path="/reports" element={<Reports operations={operations} holders={holders}/>} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

createRoot(document.getElementById('root')).render(
  <Root />
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
