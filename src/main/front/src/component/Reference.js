import { Tooltip } from '@mui/material';
import InfoTwoToneIcon from '@mui/icons-material/InfoTwoTone';

function Reference({value}) {
  return (
    <Tooltip title={value}>
      <InfoTwoToneIcon />
    </Tooltip>
  );
}

export default Reference;