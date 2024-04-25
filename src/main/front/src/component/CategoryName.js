import configuration from '../Configuration';
import { Stack } from '@mui/material';
import CategoryIcon from '@mui/icons-material/Category';

function CategoryName({value}) {
  return (
    <Stack direction="row" alignItems="center" spacing={1}>
      {configuration.categoryIcons[value] || <CategoryIcon fontSize="small" />}<span>{value}</span>
    </Stack>
  );
}

export default CategoryName;