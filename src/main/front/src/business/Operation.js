class Operation {
  static fromObject(object) {
    Object.defineProperty(
      object,
      'credit',
      {
        get() {
          return this.breakdown.reduce((sum, element) => sum + element.credit, 0);
        }
      }
    );
    Object.defineProperty(
      object,
      'unassignedCredit',
      {
        get() {
          return this.breakdown
            .filter(breakdown => !breakdown.supplier)
            .filter(breakdown => !breakdown.category)
            .reduce((sum, element) => sum + element.credit, 0);
        }
      }
    );
    return object;
  }
}

export default Operation;