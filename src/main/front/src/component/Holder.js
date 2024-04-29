import { Avatar, Stack } from '@mui/material';

function Holder({value, size = "medium", ...props}) {
  const sizes = {
    small: '1.1em',
    large: '2em'
  };
  
  return (
    <Stack direction="row" spacing={1} alignItems="center">
      <Avatar alt={value.name} src={'/avatar/' + value.id + '.png'} sx={{...props.sx, ...(sizes[size] ? { width: sizes[size], height: sizes[size] } : {})}}/>
      <div>{value.name}</div>
    </Stack>
  );
}

export default Holder;