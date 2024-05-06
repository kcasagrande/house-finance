import { Stack, Typography } from '@mui/material';
import OperationsForm from '../component/OperationsForm';
import { fetchOperations } from '../application/fetch-data';

function OperationsLoader({onLoad}) {

  function queryOperations(criteria) {
    fetchOperations(criteria['from'], criteria['to'])
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