import configuration from '../Configuration';
import Account from '../business/Account';
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

function fetchOperations(criteria) {
  const queryParameters = Object.entries(criteria)
    .map((entry) => entry[0] + '=' + entry[1])
    .join('&');
  return fetchJson(`/operations?${queryParameters}`)
    .then(operations => operations.map(Operation.fromObject));
}

function fetchCategories() {
  return fetchJson('/operations/categories');
}

function fetchAccounts() {
  return fetchJson('/accounts')
    .then(accounts => accounts.map(Account.fromObject));
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