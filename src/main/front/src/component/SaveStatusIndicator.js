import React from 'react';
import { CheckCircle, Circle, Error, Pending } from '@mui/icons-material';

function SaveStatusIndicator({ state }) {
  switch(state) {
    case 'pending':
      return <Pending />
    break;
    case 'success':
      return <CheckCircle />
    break;
    case 'error':
      return <Error />
    break;
    default:
      return <Circle sx={{opacity: 0}} />
  }
}

export default SaveStatusIndicator;