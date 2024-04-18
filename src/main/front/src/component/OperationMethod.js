import { Tooltip } from '@mui/material';
import BorderColorIcon from '@mui/icons-material/BorderColor';
import BrokenImageIcon from '@mui/icons-material/BrokenImage';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import OutputIcon from '@mui/icons-material/Output';
import SyncAltIcon from '@mui/icons-material/SyncAlt';

function OperationMethod(props) {
  const {method} = props;
  
  const methods = {
    'card': {
      'label': 'Paiement par carte',
      'icon': <CreditCardIcon fontSize="small" />
    },
    'check': {
      'label': 'Chèque',
      'icon': <BorderColorIcon fontSize="small" />
    },
    'debit': {
      'label': 'Prélèvement',
      'icon': <OutputIcon fontSize="small" />
    },
    'transfer': {
      'label': 'Virement',
      'icon': <SyncAltIcon fontSize="small" />
    }
  };

  return (
    <Tooltip title={(methods[method] && methods[method]['label']) || 'Méthode indéterminée'}>
      {(methods[method] && methods[method]['icon']) || <BrokenImageIcon fontSize="small" />}
    </Tooltip>
  );
}

export default OperationMethod;