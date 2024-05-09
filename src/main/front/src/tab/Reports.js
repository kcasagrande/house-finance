import Grid from '@mui/material/Unstable_Grid2';
import CategoryExpenses from '../component/report/CategoryExpenses';
import CategorySupplierTable from '../component/report/CategorySupplierTable';
import SuppliersRepartition from '../component/report/SuppliersRepartition';

function Reports({operations, holders}) {
  const debitOperations = operations.filter((operation) => operation.credit <= 0);
  const creditOperations = operations.filter((operation) => operation.credit > 0);
  
  return (
    <Grid container spacing={2} columns={3}>
      <Grid item xs>
        <CategorySupplierTable operations={debitOperations} holders={holders} />
      </Grid>
      <Grid item xs>
        <CategoryExpenses operations={debitOperations} width={500} />
      </Grid>
      <Grid item xs>
        <SuppliersRepartition operations={debitOperations} categories={['Alimentaire', 'Essence']} holders={holders} width={500} />
      </Grid>
    </Grid>
  );
}

export default Reports;