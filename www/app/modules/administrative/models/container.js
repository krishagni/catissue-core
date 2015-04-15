
angular.module('os.administrative.models.container', ['os.common.models'])
  .factory('Container', function(osModel, $q, $http) {
    var Container = new osModel('storage-containers');

    Container.prototype.getType = function() {
      return 'storage_container';
    }

    Container.prototype.getDisplayName = function() {
      return this.name;
    }

    Container.list = function(opts) {
      var defOpts = {topLevelContainers: true};
      return Container.query(angular.extend(defOpts, opts || {}));
    };

    Container.listForSite = function(siteName, onlyFreeContainers, flatten) {
      var params = {
        site: siteName,
        anyLevelContainers: true,
        onlyFreeContainers: !!onlyFreeContainers
      };

      var ret = Container.query(params);
      if (flatten == true) {
        ret = ret.then(
          function(containers) {
            return Container._flatten(containers, 'childContainers');
          }
        );
      }
        
      return ret;
    };

    Container.flatten = function(containers) {
      return Container._flatten(containers, 'childContainers');
    };

    Container.getByName = function(name) {
      return $http.get(Container.url() + '/byname/' + name).then(
        function(result) {
          return new Container(result.data);
        }
      );
    };

    Container.prototype.getChildContainers = function(anyLevel) {
      return Container.query({parentContainerId: this.$id(), includeChildren: true});
    };

    Container.prototype.getOccupiedPositions = function() {
      return $http.get(Container.url() + '/' + this.$id() + '/occupied-positions').then(
        function(result) {
          return result.data;
        }
      );
    };

    Container.prototype.isSpecimenAllowed = function(cpId, specimenClass, specimenType) {
      var params = {cpId: cpId, specimenClass: specimenClass, specimenType: specimenType};
      return $http.head(Container.url() + '/' + this.$id(), {params: params}).then(
        function(result) {
          return true;
        },

        function(err) {
          return false;
        }
      );
    };

    Container.prototype.assignPositions = function(positions) {
      return $http.post(Container.url() + this.$id() + '/occupied-positions', positions).then(
        function(result) {
          return result.data;
        }
      )
    }

    return Container;
  });