import React from 'react';
import { Paper, Skeleton, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, ToggleButton } from '@mui/material';
import BrokenImageIcon from '@mui/icons-material/BrokenImage';

function PaginatedTable({rowsPerPageOptions, columns, data, format = ((datum, index, data) => datum), ready = true}) {
  const [displayInvalidsOnly, setDisplayInvalidsOnly] = React.useState(false);
  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(rowsPerPageOptions ? rowsPerPageOptions[0] : -1);
  
  function handlePageChange(event, newPage) {
    setPage(newPage);
  }
  
  function handleRowsPerPageChange(event) {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  }

  return (
    <>
      <Stack direction="row" alignItems="center" justifyContent="center">
        <ToggleButton
          selected={displayInvalidsOnly}
          onChange={() => {
            setDisplayInvalidsOnly(!displayInvalidsOnly);
          }}
          color="primary"
          value="invalidOnly"
        >
          <BrokenImageIcon /> Display invalid operations only
        </ToggleButton>
      </Stack>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TablePagination
                rowsPerPageOptions={(rowsPerPageOptions || []).concat([{label: 'All', value: -1}])}
                count={data.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handlePageChange}
                onRowsPerPageChange={handleRowsPerPageChange}
              />
            </TableRow>
            <TableRow>
              {columns.map((column) =>
                <TableCell
                  key={column.id}
                  align={column.align || 'center'}
                  style={{ minWidth: column.minWidth || 'auto' }}
                 >
                   {column.label}
                 </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {(ready
              ? (rowsPerPage > 0
                ? data.filter(operation => !(displayInvalidsOnly && operation.isValid())).slice(page * rowsPerPage, (1 + page) * rowsPerPage).map(format)
                : data.filter(operation => !(displayInvalidsOnly && operation.isValid())).map(format)
                )
              : <TableRow key={''}>
                  {columns.map((column) => <TableCell key={column.id}><Skeleton /></TableCell>)}
                </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}

export default PaginatedTable;