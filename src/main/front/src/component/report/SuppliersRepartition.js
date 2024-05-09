import { Stack } from '@mui/material';
import PieChart from '../../widget/PieChart';

function SuppliersRepartition({operations, holders, width=400, height=400}) {
  const breakdown = operations.flatMap(operation => operation.breakdown);
  const groups = breakdown
    .reduce((_groups, element) => {
      return {
        ..._groups,
        [element.supplier]: (_groups[element.supplier] || 0) + element.credit
      }
    }, {});
  
  return (
    <Stack direction="column" alignItems="center" spacing={2}>
      <PieChart
        colors={["cyan", "lightblue", "pink", "magenta"]}
        data={Object.entries(groups).map((datum) => {
          return {
            name: holders.find(holder => holder.id === datum[0])?.name || 'Non assigné',
            value: -datum[1]
          };
        })}
        width={width}
        height={height}
      />
    </Stack>
  );
}

export default SuppliersRepartition;