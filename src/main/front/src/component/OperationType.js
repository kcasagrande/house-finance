import BorderColorIcon from '@mui/icons-material/BorderColor';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import OutputIcon from '@mui/icons-material/Output';
import SyncAltIcon from '@mui/icons-material/SyncAlt';

function OperationType(props) {
  const {type} = props;
  
  const types = {
    'card': <CreditCardIcon />,
    'check': <BorderColorIcon />,
    'debit': <OutputIcon />,
    'transfer': <SyncAltIcon />
  };

  return (
    types[type]
  );
}

export default OperationType;