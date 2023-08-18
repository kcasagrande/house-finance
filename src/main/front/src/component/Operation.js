import React from 'react';
import { Collapse, IconButton, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowRight } from '@mui/icons-material';
import Category from './Category';
import OperationType from './OperationType';

function Operation(props) {
  const {operation} = props;
  const [open, setOpen] = React.useState(false);
  
  function groupByCategory(supplies) {
    return supplies.reduce((categories, supply) =>
      Object.keys(categories).includes(supply.category) ?
        {...categories, [supply.category]: [supply, ...categories[supply.category]]} :
        {...categories, [supply.category]: [supply]}
      , {});
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
        <TableCell>{operation.number}</TableCell>
        <TableCell><OperationType type={operation.type} /></TableCell>
        <TableCell>{operation.label}</TableCell>
        <TableCell>{operation.operationDate}</TableCell>
        <TableCell>{operation.valueDate}</TableCell>
        <TableCell>{operation.accountDate}</TableCell>
        <TableCell className="euros">{operation.breakdown.map(breakdown => breakdown.credit).reduce((sum, amount) => sum + amount, 0) / 100.0}</TableCell>
        <TableCell>{operation.card}</TableCell>
      </TableRow>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0}} colSpan="9">
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Catégorie</TableCell>
                  <TableCell>Fournisseurs</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {
                  Object.entries(groupByCategory(operation.breakdown))
                    .map((entry) => <Category key={'category-' + entry[0]} name={entry[0]} supplies={entry[1]} />)
                }
              </TableBody>
            </Table>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}

export default Operation;