import { InputAdornment, TextField } from '@mui/material';
import { NumericFormat } from 'react-number-format';
import configuration from '../Configuration';

function AmountField({locale, currency, onChange, ...props}) {
  
  function handleValueChange(values, sourceInfo) {
    onChange(Number.parseInt((values.value).replace('.', ''), 10) || 0);
  }
  
  const numberFormat = new Intl.NumberFormat(
    locale || configuration.locale,
    {
      style: 'currency',
      currency: currency || configuration.currency,
      minimumFractionDigits: 2,
      maximumFranctionDigits: 2
    }
  );
  
  const formattedParts = numberFormat.formatToParts(-1000);
  const decimalSeparator = formattedParts
    .filter(part => part.type === 'decimal')[0]
    .value;
  const currencySymbolPosition = (formattedParts.findIndex(part => part.type === 'currency') > 0 ? 'end' : 'start');
  const currencySymbol = formattedParts
    .filter(part => part.type === 'currency')[0]
    .value;
    
  return (
    <NumericFormat
      {...props}
      customInput={TextField}
      InputProps={{
        startAdornment: (currencySymbolPosition === 'start' ? <InputAdornment position="start">{currencySymbol}</InputAdornment> : <></>),
        endAdornment: (currencySymbolPosition === 'end' ? <InputAdornment position="end">{currencySymbol}</InputAdornment> : <></>),
      }}
      allowNegative
      decimalSeparator={decimalSeparator}
      decimalScale={2}
      fixedDecimalScale
      onValueChange={handleValueChange}
    />
  );
}

export default AmountField;