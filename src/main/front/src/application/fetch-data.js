import configuration from '../Configuration';

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

function fetchOperations() {
  return fetchJson('/operations?from=2023-01-01&to=2023-12-31');
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