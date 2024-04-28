import { useState } from 'react';
import { IconButton, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import NewCategoryModal from './NewCategoryModal';
import OperationBreakdownRow from './OperationBreakdownRow';
import { AddCircle } from '@mui/icons-material';

function OperationBreakdownTable({operation, existingCategories}) {
  const [openNewCategoryModal, setOpenNewCategoryModal] = useState(false);

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
            <NewCategoryModal open={openNewCategoryModal} onClose={() => setOpenNewCategoryModal(false)} operation={operation} />
            <IconButton onClick={() => setOpenNewCategoryModal(true)}><AddCircle /></IconButton>
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