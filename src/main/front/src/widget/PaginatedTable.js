import React from 'react';
import { Paper, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow } from '@mui/material';

function PaginatedTable({rowsPerPageOptions, columns, ready = true, children}) {
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
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TablePagination
              rowsPerPageOptions={(rowsPerPageOptions || []).concat([{label: 'All', value: -1}])}
              count={children.length}
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
                align={column.align || 'left'}
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
              ? children.slice(page * rowsPerPage, (1 + page) * rowsPerPage)
              : children
              )
            : <TableRow key={''}>
                {columns.map((column) => <TableCell key={column.id}><Skeleton /></TableCell>)}
              </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default PaginatedTable;