import configuration from './Configuration';

function amount(value, currency, locale) {
  return new Intl.NumberFormat(
    locale || configuration.locale,
    {
      style: 'currency',
      currency: (currency || configuration.currency),
      minimumFractionDigits: 2
    }
  )
   .format(value / 100);
}

export default amount;