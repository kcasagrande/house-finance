import { DatePicker } from '@mui/x-date-pickers';
import dayjs from 'dayjs';

function DayJsDatePicker({value, onChange, referenceDate, width = 200, ...props}) {
  return (
    <DatePicker
      format="YYYY-MM-DD"
      value={!!value ? dayjs(value) : null}
      onChange={(newValue) => onChange(!!newValue ? newValue.format('YYYY-MM-DD') : null)}
      referenceDate={!!referenceDate ? dayjs(referenceDate) : null}
      sx={{
        width: {width}
      }}
      {...props}
    />
  );
}

export default DayJsDatePicker;