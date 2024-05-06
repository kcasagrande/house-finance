import React from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';
import { BrowserRouter, RouterProvider, Routes, Route } from 'react-router-dom';
import Import from './tab/Import';
import Details from './tab/Details';
import OperationsLoader from './tab/OperationsLoader';
import Reports from './tab/Reports';
import reportWebVitals from './reportWebVitals';

function Root() {
  const [operations, setOperations] = React.useState([]);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App operations={operations} />}>
          <Route path="/load" element={<OperationsLoader onLoad={setOperations} />} />
          <Route path="/import" element={<Import />} />
          <Route path="/details" element={<Details operations={operations} />} />
          <Route path="/reports" element={<Reports operations={operations} />} />
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
