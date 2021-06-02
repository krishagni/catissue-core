
import ui from '@/global.js';

class Authorization {

  isAllowed(opts) {
    if (ui.currentUser.admin) {
      return true;
    }

    let resources = opts.resources || [opts.resource];
    let allowed = false;
    for (let right of ui.userRights) {
      if (!opts.sites && !opts.cp && resources.indexOf(right.resource) != -1) {
        //
        // For resources whose rights are independent of CP and Site
        //
        allowed = this.isPresent(right.operations, opts.operations);
      } else if ((!opts.sites || !right.site || opts.sites.indexOf(right.site) != -1) &&
        (!opts.cp || !right.cp || right.cp == opts.cp) &&
        (resources.indexOf(right.resource) != -1)) {
        //
        // For resources whose rights are specified based on CP and/or Site
        //
        allowed = this.isPresent(right.operations, opts.operations);
      }

      if (allowed) {
        break;
      }
    }

    return allowed;
  }

  isPresent(main, test) {
    for (let op of test) {
      if (main.indexOf(op) != -1) {
        return true;
      }
    }

    return false;
  }
}

export default new Authorization();
