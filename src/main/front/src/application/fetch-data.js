import configuration from '../Configuration';
import Operation from '../business/Operation';

function fetchJson(url) {
  return fetch(configuration.api + url)
    .then((response) => {
      if(response.ok) {
        return response.json();
      } else {
        response.text()
          .then((body) => {
            throw new Error('Response status ' + response.status + ' while fetching ' + url + ':\n' + body);
          });
      }
    });
}

function fetchOperations(from = '2023-01-01', to = '2023-12-31') {
  return fetchJson(`/operations?from=${from}&to=${to}`)
    .then(operations => operations.map(Operation.fromObject));
}

function fetchCategories() {
  return fetchJson('/operations/categories');
}

function fetchAccounts() {
  return fetchJson('/accounts');
}

function fetchHolders(account) {
  return fetchJson('/accounts/' + account + '/holders');
}

export {
  fetchOperations,
  fetchCategories,
  fetchAccounts,
  fetchHolders
};