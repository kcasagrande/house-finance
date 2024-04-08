import React from 'react';
import { Paper, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow } from '@mui/material';

function PaginatedTable({rowsPerPageOptions, columns, rows}) {
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
              count={rows.length}
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
          {(rowsPerPage > 0
              ? rows.slice(page * rowsPerPage, (1 + page) * rowsPerPage)
              : rows
            ).map((row) => {
              return (
                <TableRow key={row.id}>
                  {columns.map((column) => {
                    const value = row[column.id];
                    return (
                      <TableCell key={column.id} align={column.align || 'left'}>
                        {column.format && typeof value === 'number'
                          ? column.format(value)
                          :value
                        }
                      </TableCell>
                    );
                  })}
                </TableRow>
              );
            })
          }
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default PaginatedTable;