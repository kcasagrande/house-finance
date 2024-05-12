import configuration from '../Configuration';
import './Amount.css';

function Amount({value, currency, locale, ...props}) {
  const parts = new Intl.NumberFormat(
    locale || configuration.locale,
    {
      style: 'currency',
      currency: (currency || configuration.currency),
      minimumFractionDigits: 2
    }
  )
    .formatToParts(value / 100);
  
  return (
    <span {...props}>
      {parts.map((part) => <span className={"currency-" + part.type}>{part.value}</span>)}
    </span>
  );
}

export default Amount;