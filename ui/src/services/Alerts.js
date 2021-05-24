
class Alerts {

  toastSvc = undefined;

  info(message, timeout, id) {
    this.add('info', message, timeout, id);
  }

  success(message, timeout, id) {
    this.add('success', message, timeout, id);
  }

  error(message, timeout, id) {
    this.add('error', message, timeout, id);
  }

  warn(message, timeout, id) {
    this.add('warn', message, timeout, id);
  }

  add(type, message, timeout, id) {
    this.toastSvc.add({severity: type, detail: message, life: timeout < 0 ? undefined : (timeout || 5000), group: id});
  }

  remove(id) {
    this.toastSvc.removeGroup(id);
  }
}

export default new Alerts();
