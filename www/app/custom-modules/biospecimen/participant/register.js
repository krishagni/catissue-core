
angular.module('openspecimen')
  .controller('CustomRegParticipantCtrl', 
    function($scope, $state, $stateParams, $http, 
             PvManager, FileSvc, 
             ApiUrls, ApiUtil, Alerts) {

      $scope.cpId = $stateParams.cpId

      function loadPvs() {
        PvManager.loadPvs($scope, 'gender');
        PvManager.loadPvs($scope, 'anatomicSite');
        PvManager.loadSites($scope, 'hospitals');

        $scope.diagnoses = PvManager.getClinicalDiagnoses($scope.cpId);
      };

      function init() {
        $scope.cpr = {
          participant: {}
        };

        $scope.patientSmokingHistory = { };

        $scope.visit = { };

        $scope.blood = { };

        $scope.bloodExtn = { };

        $scope.frozenTissue = { };
 
        $scope.consent = {
          files: '',
          filename: ''
        };

        loadPvs();

        $scope.$watch('consent.files', function(newFile) {
          if (!newFile) {
            $scope.removeConsent();
            return;
          }

          uploadFile(newFile).then(
            function(result) {
              $scope.consent.filename = result.data.filename;
            }
          );
        });
      };

      function uploadFile(file) {
        return FileSvc.upload(ApiUrls.getUrl('form-files'), file);
      };

      $scope.removeConsent = function() {
        $scope.consent = {
          files: '',
          filename: ''
        };
      };


      function getPayload() {
        var visit = angular.copy($scope.visit);
        visit.visitSite = visit.visitSite.name;

        return {
          cpr: $scope.cpr,
          patientSmokingHistory: $scope.patientSmokingHistory,
          visit: visit,
          blood: $scope.blood,
          bloodExtn: $scope.bloodExtn,
          frozenTissue: $scope.frozenTissue
        };
      };

      $scope.isInteracted = function(field) {
        return $scope.submitted || field.$dirty;
      };

      $scope.collectSpecimens = function(form) {
        $scope.submitted = true;
        if (form.$invalid) {
          return;
        }

        var url = ApiUrls.getUrl() + '/rest/ng/demo/participant-specimens';
        $http.post(url, getPayload())
          .then(ApiUtil.processResp, ApiUtil.processResp)
          .then(function(result) {
            if (result.status == 'ok') {
              $state.go('participant-detail.overview', {cprId: result.data.cpr.id});
              return;
            }

            Alerts.error("Error collecting specimens");
          });
      };

      init();
    }
  );