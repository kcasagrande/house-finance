import React from 'react';
import { Collapse, IconButton, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowRight } from '@mui/icons-material';
import Category from './Category';
import OperationMethod from './OperationMethod';
import { centsAsEurosString } from '../Cents'

function Operation({operation, existingCategories, refreshExistingCategories}) {
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
        <TableCell>{operation.number}</TableCell>
        <TableCell><OperationMethod method={operation.method} /></TableCell>
        <TableCell>{operation.label}</TableCell>
        <TableCell>{operation.operationDate}</TableCell>
        <TableCell>{operation.valueDate}</TableCell>
        <TableCell>{operation.accountDate}</TableCell>
        <TableCell className="euros">{centsAsEurosString(operation.breakdown.map(breakdown => breakdown.credit).reduce((sum, amount) => sum + amount, 0))}</TableCell>
        <TableCell>{operation.card}</TableCell>
      </TableRow>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0}} colSpan="9">
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Catégorie</TableCell>
                  <TableCell>Montant</TableCell>
                  <TableCell>Fournisseurs</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {
                  Object.entries(groupByCategory(operation.breakdown))
                    .map((entry) =>
                      <Category
                        key={'category-' + entry[0]}
                        name={entry[0]}
                        supplies={entry[1]}
                        account={operation.account}
                        existingCategories={existingCategories}
                        refreshExistingCategories={refreshExistingCategories}
                      />
                    )
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