import { Avatar, Chip, Tooltip } from '@mui/material';
import { Cancel } from '@mui/icons-material';
import amount from '../amount';

function OperationBreakdownSupply({value}) {
  function handleDelete() {
  }
  
  return (
    <Chip
      sx={{ marginRight: '1ex' }}
      variant="outlined"
      avatar={<Avatar src={'/avatar/' + value.supplier + '.png'} />}
      label={amount(value.credit)}
      onDelete={handleDelete}
      deleteIcon={<Tooltip title="Supprimer cette ventilation"><Cancel /></Tooltip>}
    />
  );
}

export default OperationBreakdownSupply;