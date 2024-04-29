import { useState } from 'react';
import { IconButton, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import NewBreakdownModal from './NewBreakdownModal';
import OperationBreakdownRow from './OperationBreakdownRow';
import { AddCircle } from '@mui/icons-material';

function OperationBreakdownTable({operation, existingCategories}) {
  const [openNewBreakdownModal, setOpenNewBreakdownModal] = useState(false);

  function groupByCategory(supplies) {
    const groups = supplies.reduce((categories, supply) => {
      const key = supply.category ?? '';
      return {
        ...categories,
        ...{
          [key]: [
            ...categories[key] ?? [],
            {
              supplier: supply.supplier,
              credit: supply.credit,
              comment: supply.comment
            }
          ]
        }
      };
    }, {});
    return groups;
  }
  
  return (
    <Table size="small">
      <TableHead>
        <TableRow key="header">
          <TableCell>
            <NewBreakdownModal open={openNewBreakdownModal} onClose={() => setOpenNewBreakdownModal(false)} operation={operation} categories={existingCategories} />
            <IconButton onClick={() => setOpenNewBreakdownModal(true)}><AddCircle /></IconButton>
            Category
          </TableCell>
          <TableCell>Credit</TableCell>
          <TableCell>Holders</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {
          Object.entries(groupByCategory(operation.breakdown))
            .filter((entry) => !!entry[0])
            .map((entry) =>
              <OperationBreakdownRow category={entry[0]} supplies={entry[1]} />
            )
        }
      </TableBody>
    </Table>
  );
}

export default OperationBreakdownTable;