import { CallSplit, Category } from '@mui/icons-material';

function BreakdownIcon(props) {
  return (
    <CallSplit {...props} sx={{ transform: 'rotate(90deg)' }} />
  );
}

function CategoryIcon(props) {
  return (
    <Category {...props} />
  );
}

export {
  BreakdownIcon,
  CategoryIcon
};
