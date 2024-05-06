import { useEffect, useState } from 'react';
import { Stack } from '@mui/material';
import { fetchOperations } from '../../application/fetch-data';
import PieChart from '../../widget/PieChart';

function CategoryExpenses({operations, width=400, height=400}) {
  const [initialized, setInitialized] = useState(false);
  const [creditOperations, setCreditOperations] = useState([]);
  const [expensesByCategory, setExpensesByCategory] = useState({});
  
  const debitOperations = operations.filter((operation) => operation.credit <= 0);
  
  function trace(value) {
    if(typeof value === 'object' || typeof value === 'array') {
      console.log(JSON.stringify(value, null, 2));
    } else {
      console.log(value);
    }
    return value;
  }
  
  useEffect(() => {
    setExpensesByCategory(groupExpensesByCategory(debitOperations));
  }, [debitOperations]);
  
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