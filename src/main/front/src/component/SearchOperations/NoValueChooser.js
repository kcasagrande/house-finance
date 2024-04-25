import { InputLabel, Stack, ToggleButton, ToggleButtonGroup, Tooltip } from '@mui/material';
import ExcludeIcon from '@mui/icons-material/HighlightOff';
import IncludeIcon from '@mui/icons-material/CheckCircleOutline';
import OnlyIcon from '@mui/icons-material/ErrorOutline';

function NoValueChooser({id, label, value, onChange}) {
  function handleChange(event, newValue) {
    if(!!newValue) {
      onChange(newValue);
    }
  }

  return (
    <Stack direction="row" alignItems="center" justifyContent="end" spacing={1}>
      <InputLabel id={id + 'label'}>{label}</InputLabel>
      <ToggleButtonGroup
        id={id}
        labelId={id + '-label'}
        exclusive
        size="small"
        value={value}
        onChange={handleChange}
      >
        <Tooltip title="Exclude"><ToggleButton value="exclude"><ExcludeIcon size="small" /></ToggleButton></Tooltip>
        <Tooltip title="Include"><ToggleButton value="include"><IncludeIcon size="small" /></ToggleButton></Tooltip>
        <Tooltip title="Only"><ToggleButton value="only"><OnlyIcon size="small" /></ToggleButton></Tooltip>
      </ToggleButtonGroup>
    </Stack>
  );
}

export default NoValueChooser;