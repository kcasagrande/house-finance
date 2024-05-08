class Account {
  static fromObject(object) {
    Object.defineProperty(
      object,
      'ibanAsString',
      {
        get() {
          return this.iban.countryCode + this.iban.checkDigits + this.iban.bban;
        }
      }
    );
    return object;
  }
}

export default Account;