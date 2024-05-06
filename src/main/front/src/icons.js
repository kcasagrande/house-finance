import { CallSplit, Category, Receipt } from '@mui/icons-material';

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

function OperationIcon(props) {
  return (
    <Receipt {...props} />
  );
}

export {
  BreakdownIcon,
  CategoryIcon,
  OperationIcon
};
