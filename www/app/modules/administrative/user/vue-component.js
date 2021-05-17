
angular.module('os.administrative.user.detail')
  .directive('osVueComponent', function($timeout) {
    return {
      restrict: 'EA',

      scope: {
        component: '=',

        data: '='
      },

      link: function(scope, element, attrs) {
        const app = Vue.createApp({ 
          template: '<component :is="component" v-bind="input"> </component>',

          data: function() {
            return {
              component: scope.component,

              input: scope.data

            }
          },

          methods: {
          },

          watch: {
            input: function() {
              alert('changed');
            }
          },

          mounted: function() {
            console.log(this.input);
          },

          unmounted: function() {
            alert('destroyed');
          }
        });

        // dynamic
        app.component('user-list', UserList);

        var vm = app.mount(element[0]);
        scope.$watch('data', function(newVal) {
          vm.input = angular.extend({}, newVal);
        }, true);

        scope.$on('$destroy', function() {
          alert('unmount');
          app.unmount();
        });
      }
    }
  });
