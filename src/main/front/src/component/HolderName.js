import { useContext } from 'react';
import { Avatar, Stack } from '@mui/material';
import { HoldersContext } from '../context/HoldersContext';

function HolderName({id, justifyContent="center", size = "1em", nonBreakable = false}) {
  const holders = useContext(HoldersContext);
  const name = holders[id]?.name || 'Unknown person';
  
  return (
    <Stack direction="row" alignItems="center" justifyContent={justifyContent} spacing={1}>
      <Avatar sx={{ width: size, height: size }} src={'/avatar/' + id + '.png'} alt={name} />
      <span>{ nonBreakable ? name.replace(/ +/, '\u00A0') : name }</span>
    </Stack>
  );
}

export default HolderName;