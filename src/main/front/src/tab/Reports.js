import Grid from '@mui/material/Unstable_Grid2';
import { Stack } from '@mui/material';
import CategoryExpenses from '../component/report/CategoryExpenses';
import CategorySupplierTable from '../component/report/CategorySupplierTable';
import SuppliersRepartition from '../component/report/SuppliersRepartition';

function Reports({operations, holders}) {
  const debitOperations = operations.filter((operation) => operation.credit <= 0);
  const creditOperations = operations.filter((operation) => operation.credit > 0);
  
  return (
    <Grid container columns={4} columnSpacing={2} sx={{ padding: '1em' }}>
      <Grid item xs={1}>
        <CategorySupplierTable operations={debitOperations} holders={holders}/>
      </Grid>
      <Grid item xs={3}>
        <Stack direction="row" alignItems="center" justifyContent="center" spacing={2}>
          <CategoryExpenses operations={debitOperations} width={400} />
          <SuppliersRepartition operations={debitOperations} holders={holders} width={400} />
        </Stack>
      </Grid>
    </Grid>
  );
}

export default Reports;