
angular.module('os.administrative.user.detail')
  .directive('osVueComponent', function($timeout) {
    return {
      restrict: 'A',

      scope: {
        data: '='
      },

      link: function(scope, element, attrs) {
        const app = Vue.createApp({ 
          template: '<div> Hello from Vue3 {{input}} <button @click="toggle">Toggle</button> </div> ',

          data() {
            return {
              input: scope.data
            }
          },

          methods: {
            toggle: function() {
              var that = this;

              $timeout(
                function() {
                  that.input.activityStatus = that.input.activityStatus == 'Active' ? 'Closed' : 'Active';
                }
              );
            }
          }
        });

        var vm = app.mount(element[0]);
        /*vm.input = scope.data;*/

        scope.$watch('data', function() {
          vm.input = scope.data;
        });
      }
    }
  });
