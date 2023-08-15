import './App.css';
import { useState } from 'react';
import { Fragment } from 'react';

function App() {
  const [operations, setOperations] = useState([]);
  
  function updateOperations() {
    fetch("http://localhost:8080/api/v1/operations?from=2023-01-01&to=2023-12-31")
      .then(response => {
        if(response.ok) {
          return response.json();
        } else {
          throw new Error('Response status is ' + response.status);
        }
      })
      .then(setOperations);
  }

  return (
      <div className="App">
        <header className="App-header">
          <h1>Opérations</h1>
        </header>
        <button onClick={updateOperations}>Actualiser</button>
        <table id="operations">
          <thead>
            <tr>
              <th>Référence</th>
              <th>Libellé</th>
              <th>Catégorie</th>
              <th>Date d'opération</th>
              <th>Date de valeur</th>
              <th>Date comptable</th>
              <th>Fournisseur</th>
              <th>Montant</th>
              <th>Carte</th>
            </tr>
          </thead>
          <tbody>
            {operations.map(operation =>
              <Fragment key={'operation-' + operation.id}>
                <tr className="operation" data-type={operation.type}>
                  <td>{operation.number}</td>
                  <td>{operation.label}</td>
                  <td></td>
                  <td>{operation.operationDate}</td>
                  <td>{operation.valueDate}</td>
                  <td>{operation.accountDate}</td>
                  <td></td>
                  <td>{operation.breakdown.map(breakdown => breakdown.credit).reduce((sum, amount) => sum + amount, 0) / 100.0}</td>
                  <td>{operation.card}</td>
                </tr>
                {operation.breakdown.map((breakdown, index) =>
                  <tr className="breakdown" key={'breakdown-' + operation.id + '-' + index}>
                    <td></td>
                    <td></td>
                    <td>{breakdown.category}</td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td>{breakdown.supplier}</td>
                    <td>{breakdown.credit / 100.0}</td>
                  </tr>
                )}
              </Fragment>
            )}
          </tbody>
        </table>
      </div>
  );
}

export default App;
