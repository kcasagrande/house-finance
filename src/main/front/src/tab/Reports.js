import Grid from '@mui/material/Unstable_Grid2';
import { Stack } from '@mui/material';
import CategoryExpenses from '../component/report/CategoryExpenses';
import CategorySupplierTable from '../component/report/CategorySupplierTable';
import SuppliersRepartition from '../component/report/SuppliersRepartition';

function Reports({operations, holders}) {
  const debitOperations = operations.filter((operation) => operation.credit <= 0);
  const creditOperations = operations.filter((operation) => operation.credit > 0);
  
  return (
    <Stack direction="row" alignItems="flex-start" justifyContent="center" spacing={2} sx={{ padding: '1em' }}>
      <CategorySupplierTable operations={debitOperations} holders={holders}/>
      <Stack direction="column" alignItems="center" justifyContent="flex-start" spacing={2}>
        <CategoryExpenses operations={debitOperations} width={400} />
        <SuppliersRepartition operations={debitOperations} holders={holders} width={400} />
      </Stack>
    </Stack>
  );
}

export default Reports;