import React from 'react';
import { Collapse, IconButton, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowRight } from '@mui/icons-material';
import Category from './Category';
import OperationBreakdownTable from './OperationBreakdownTable';
import OperationMethod from './OperationMethod';
import amount from '../amount';

function Operation({operation, existingCategories, onChange}) {
  const [open, setOpen] = React.useState(false);
  
  function groupByCategory(supplies) {
    const groups = supplies.reduce((categories, supply) => {
      const key = supply.category ?? '';
      return Object.keys(categories).includes(key) ?
        {...categories, [key]: [supply, ...categories[key]]} :
        {...categories, [key]: [supply]}
    }, {});
    return groups;
  }
  
  return (
    <>
      <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
        <TableCell>
          <IconButton
            aria-label="expand row"
            size="small"
            onClick={() => setOpen(!open)}
          >
            { open ? <KeyboardArrowDown /> : <KeyboardArrowRight /> }
          </IconButton>
        </TableCell>
        <TableCell>{operation.checkNumber}</TableCell>
        <TableCell><OperationMethod method={operation.method} /></TableCell>
        <TableCell>{operation.label}</TableCell>
        <TableCell>{operation.operationDate}</TableCell>
        <TableCell>{operation.valueDate}</TableCell>
        <TableCell>{operation.accountDate}</TableCell>
        <TableCell>{amount(operation.breakdown.map(breakdown => breakdown.credit).reduce((sum, amount) => sum + amount, 0))}</TableCell>
        <TableCell>{operation.card}</TableCell>
      </TableRow>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0}}></TableCell>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0}} colSpan="8">
          <Collapse in={open} timeout="auto" unmountOnExit>
            <OperationBreakdownTable operation={operation} existingCategories={existingCategories} />
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}

export default Operation;