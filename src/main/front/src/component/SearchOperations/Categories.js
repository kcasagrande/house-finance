import { Button, Card, CardContent, Popover, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import NoValueChooser from './NoValueChooser';
import { CategoryIcon } from '../../icons';

function Categories() {
  const [anchor, setAnchor] = useState(null);
  const [includeUncategorized, setIncludeUncategorized] = useState('include');
  
  function open(event) {
    setAnchor(event.target);
  }
  
  function close() {
    setAnchor(null);
  }
  
  return (
    <>
      <Button
        variant="outlined"
        onClick={open}
        startIcon={<CategoryIcon />}
      >
        Categories
      </Button>
      <Popover
        open={!!anchor}
        onClose={close}
        anchorEl={anchor}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center'
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center'
        }}
      >
        <Card>
          <CardContent>
            <NoValueChooser
              id="uncategorized-operations"
              label="Uncategorized operations"
              value={includeUncategorized}
              onChange={setIncludeUncategorized}
            />
          </CardContent>
        </Card>
      </Popover>
    </>
  );
}

export default Categories;