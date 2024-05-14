import configuration from '../Configuration';
import { Stack } from '@mui/material';
import CategoryIcon from '@mui/icons-material/Category';

function CategoryName({value, justifyContent = "center", iconAfter = false, nonBreakable = false}) {
  return (
    <Stack direction="row" alignItems="center" justifyContent={justifyContent} spacing={1}>
      {iconAfter ? <></> : (configuration.categoryIcons[value] || <CategoryIcon fontSize="small" />)}
      <span>{nonBreakable ? value.replace(/ +/, '\u00A0') : value}</span>
      {iconAfter ? (configuration.categoryIcons[value] || <CategoryIcon fontSize="small" />) : <></>}
    </Stack>
  );
}

export default CategoryName;