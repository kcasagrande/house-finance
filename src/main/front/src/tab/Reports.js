import CategoryExpenses from '../component/report/CategoryExpenses';
import SuppliersRepartition from '../component/report/SuppliersRepartition';

function Reports({operations, holders}) {
  const debitOperations = operations.filter((operation) => operation.credit <= 0);
  const creditOperations = operations.filter((operation) => operation.credit > 0);
  
  return (
    <>
      <CategoryExpenses operations={debitOperations} />
      <SuppliersRepartition operations={debitOperations} categories={['Alimentaire', 'Essence']} holders={holders} />
    </>
  );
}

export default Reports;