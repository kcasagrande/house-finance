import React from 'react';
import { Collapse, IconButton, Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowRight } from '@mui/icons-material';
import Breakdown from './Breakdown';

function Operation(props) {
  const {operation} = props;
  const [open, setOpen] = React.useState(false);
  
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
        <TableCell>{operation.label}</TableCell>
        <TableCell>{operation.operationDate}</TableCell>
        <TableCell>{operation.valueDate}</TableCell>
        <TableCell>{operation.accountDate}</TableCell>
        <TableCell className="euros">{operation.breakdown.map(breakdown => breakdown.credit).reduce((sum, amount) => sum + amount, 0) / 100.0}</TableCell>
        <TableCell>{operation.card}</TableCell>
      </TableRow>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0}} colSpan="8">
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Catégorie</TableCell>
                  <TableCell>Fournisseur</TableCell>
                  <TableCell>Montant</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {operation.breakdown.map((breakdown, index) =>
                  <Breakdown key={'breakdown-' + operation.id + '-' + index} breakdown={breakdown} />
                )}
              </TableBody>
            </Table>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}

export default Operation;