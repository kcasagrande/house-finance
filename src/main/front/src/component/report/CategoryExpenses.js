import { Stack } from '@mui/material';
import PieChart from '../../widget/PieChart';

function CategoryExpenses({operations, width=400, height=400}) {
  const debitOperations = operations.filter((operation) => operation.credit <= 0);
  const creditOperations = operations.filter((operation) => operation.credit > 0);
  const expensesByCategory = groupExpensesByCategory(debitOperations);
  
  function trace(value) {
    if(typeof value === 'object' || Array.isArray(value)) {
      console.log(JSON.stringify(value, null, 2));
    } else {
      console.log(value);
    }
    return value;
  }
  
  function groupBreakdownByCategory(breakdown) {
    return breakdown.reduce((categories, entry) => {
      return {
        ...categories,
        [entry.category]: (categories[entry.category] ?? 0) + entry.credit
      };
    }, {});
  }
  
  function groupExpensesByCategory(operations) {
    return operations
      .map((operation) => groupBreakdownByCategory(operation.breakdown))
      .reduce((categories, groupedBreakdownOfOperation) => {
        return {
          ...categories,
          ...(Object.keys(groupedBreakdownOfOperation).reduce((cumul, category) => {
            return {
              ...cumul,
              [category]: (categories[category] ?? 0) + groupedBreakdownOfOperation[category]
            };
          }, {}))
        };
      }, {});
  }
  
  return (
    <Stack direction="column" alignItems="center" spacing={2}>
      <PieChart
        colors={["cyan", "lightblue", "pink", "magenta"]}
        data={Object.entries(expensesByCategory).map((datum) => {
          return {
            name: datum[0],
            value: -datum[1]
          };
        })}
        width={width}
        height={height}
      />
    </Stack>
  );
}

export default CategoryExpenses;