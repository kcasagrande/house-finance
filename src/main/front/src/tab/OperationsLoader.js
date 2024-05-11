import { Stack, Typography } from '@mui/material';
import OperationsForm from '../component/OperationsForm';
import { fetchOperations } from '../application/fetch-data';

function OperationsLoader({accounts, onLoad}) {

  function queryOperations(criteria) {
    fetchOperations(criteria)
      .then(onLoad);
  }

  return (
    <Stack direction="column" alignItems="center" justifyContent="flex-start" spacing={2}>
      <Typography variant="h2">Rechercher des opérations</Typography>
      <OperationsForm onSubmit={queryOperations} />
    </Stack>
  );
}

export default OperationsLoader;