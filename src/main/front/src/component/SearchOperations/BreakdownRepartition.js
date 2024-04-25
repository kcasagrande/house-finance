import { Box, Button, Card, CardContent, Popover, Slider, Stack } from '@mui/material';
import { BreakdownIcon } from '../../icons';
import NoValueChooser from './NoValueChooser';
import { useState } from 'react';

function BreakdownRepartition() {
  const choices = [
    'Provider 1 only', 
    'Provider 1 >> Provider 2',
    'Provider 1 > Provider 2',
    'Provider 1 <=> Provider 2',
    'Provider 1 < Provider 2',
    'Provider 1 << Provider 2',
    'Provider 2 only'
  ];
  const [value, setValue] = useState([0, choices.length - 1]);
  
  const [anchor, setAnchor] = useState(null);
  const [noBreakdown, setNoBreakdown] = useState('include');
  
  function openMenu(event) {
    setAnchor(event.target);
  }
  
  function closeMenu() {
    setAnchor(null);
  }
  
  function handleRangeChange(event, newValue) {
    setValue(newValue);
  }
  
  return (
    <>
      <Button variant="outlined" onClick={openMenu} startIcon={<BreakdownIcon />}>Breakdown</Button>
      <Popover
        onClose={closeMenu}
        open={!!anchor}
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
            <Stack direction="column" alignItems="center">
              <NoValueChooser
                id="no-breakdown"
                label="Operations not broken down"
                value={noBreakdown}
                onChange={setNoBreakdown}
              />
              <Box sx={{ width: 200 }}>
                <Slider
                  id="breakdown"
                  labelId="breakdown-label"
                  size="medium"
                  step={null}
                  valueLabelDisplay="auto"
                  min={0}
                  max={choices.length - 1}
                  value={value}
                  onChange={handleRangeChange}
                  marks={choices.map((label, index) => { return { value: index }; })}
                  valueLabelFormat={(value) => choices[value]}
                />
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </Popover>
    </>
  );
}

export default BreakdownRepartition;