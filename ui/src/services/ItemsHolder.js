
class ItemsHolder {
  itemsMap = {};

  getItems(type) {
    return this.itemsMap[type] || [];
  }

  setItems(type, items) {
    this.itemsMap[type] = items;
  }

  ngSetItems(type, items) {
    let payload = { type: type, items: items };
    window.parent.postMessage({
      op: 'addItems',
      payload: payload,
      requestor: 'vueapp'
    }, '*');
  }
}

export default new ItemsHolder();
