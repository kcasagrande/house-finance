import { Tooltip } from '@mui/material';
import BorderColorIcon from '@mui/icons-material/BorderColor';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import OutputIcon from '@mui/icons-material/Output';
import SyncAltIcon from '@mui/icons-material/SyncAlt';

function OperationType(props) {
  const {type} = props;
  
  const types = {
    'card': {
      'label': 'Paiement par carte',
      'icon': <CreditCardIcon />
    },
    'check': {
      'label': 'Chèque',
      'icon': <BorderColorIcon />
    },
    'debit': {
      'label': 'Prélèvement',
      'icon': <OutputIcon />
    },
    'transfer': {
      'label': 'Virement',
      'icon': <SyncAltIcon />
    }
  };

  return (
    <Tooltip title={types[type]['label']}>
      {types[type]['icon']}
    </Tooltip>
  );
}

export default OperationType;