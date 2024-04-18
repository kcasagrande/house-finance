import { Tooltip } from '@mui/material';
import HideSourceIcon from '@mui/icons-material/HideSource';
import InfoIcon from '@mui/icons-material/Info';

function Reference({value}) {
  if(!!value) {
    return (
      <Tooltip title={value}>
        <InfoIcon />
      </Tooltip>
    );
  } else {
    return (
      <HideSourceIcon color="disabled"/>
    );
  }
}

export default Reference;