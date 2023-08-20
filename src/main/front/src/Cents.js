function centsAsEurosString(cents) {
  if(typeof(cents) === 'string') {
    return (parseCents(cents) / 100).toFixed(2);
  } else {
    return (cents / 100).toFixed(2);
  }
}

function parseCents(centsAsText) {
  return Number.parseInt(centsAsText, 10)
}

function parseEuros(eurosAsText) {
  return Math.round(Number.parseFloat(eurosAsText).toFixed(2) * 100)
}

export {
  centsAsEurosString,
  parseCents,
  parseEuros
};