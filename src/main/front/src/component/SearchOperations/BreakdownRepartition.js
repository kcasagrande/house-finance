import { Box, Button, Card, CardContent, InputLabel, Popover, Slider, Stack, ToggleButton, ToggleButtonGroup, Tooltip } from '@mui/material';
import CallSplitIcon from '@mui/icons-material/CallSplit';
import ExcludeIcon from '@mui/icons-material/HighlightOff';
import IncludeIcon from '@mui/icons-material/CheckCircleOutline';
import ExclusiveIcon from '@mui/icons-material/ErrorOutline';
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
  const [open, setOpen] = useState(false);
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
  
  function handleNoBreakdownChange(event, newValue) {
    if(!!newValue) {
      setNoBreakdown(newValue);
    }
  }
  
  return (
    <>
      <Button size="small" variant="outlined" onClick={openMenu}><CallSplitIcon sx={{ transform: 'rotate(90deg)' }} />Breakdown</Button>
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
              <Stack direction="row" alignItems="center" justifyContent="end" spacing={1}>
                <InputLabel id="no-breakdown-label">Operations not broken down</InputLabel>
                <ToggleButtonGroup
                  id="no-breakdown"
                  labelId="no-breakdown-label"
                  exclusive
                  size="small"
                  value={noBreakdown}
                  onChange={handleNoBreakdownChange}
                >
                  <Tooltip title="Exclude"><ToggleButton value="exclude"><ExcludeIcon size="small" /></ToggleButton></Tooltip>
                  <Tooltip title="Include"><ToggleButton value="include"><IncludeIcon size="small" /></ToggleButton></Tooltip>
                  <Tooltip title="Only"><ToggleButton value="exclusive"><ExclusiveIcon size="small" /></ToggleButton></Tooltip>
                </ToggleButtonGroup>
              </Stack>
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