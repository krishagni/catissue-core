
angular.module('os.biospecimen.specimen.addderivative', [])
  .controller('AddDerivativeCtrl', function(
    $scope, cp, specimen, cpr, visit, extensionCtxt, hasDict,
    SpecimenUtil, Container, ExtensionsUtil, Alerts) {

    function init() {
      $scope.parentSpecimen = specimen;
      $scope.cpr = cpr;
      $scope.visit = visit;
      $scope.derivative = SpecimenUtil.getNewDerivative($scope);
      $scope.derivative.labelFmt = cpr.derivativeLabelFmt;

      var exObjs = [
        'specimen.lineage', 'specimen.parentLabel', 'specimen.events',
        'specimen.collectionEvent', 'specimen.receivedEvent'
      ];

      if (hasDict) {
        $scope.spmnCtx = {
          obj: {specimen: $scope.derivative}, inObjs: ['specimen'], exObjs: exObjs
        }
      } else {
        $scope.extnOpts = ExtensionsUtil.getExtnOpts($scope.derivative, extensionCtxt);
      }

      $scope.deFormCtrl = {};
    }

    $scope.toggleIncrParentFreezeThaw = function() {
      if ($scope.derivative.incrParentFreezeThaw) {
        if ($scope.parentSpecimen.freezeThawCycles == $scope.derivative.freezeThawCycles) {
          $scope.derivative.freezeThawCycles = $scope.parentSpecimen.freezeThawCycles + 1;
        }
      } else {
        if (($scope.parentSpecimen.freezeThawCycles + 1) == $scope.derivative.freezeThawCycles) {
          $scope.derivative.freezeThawCycles = $scope.parentSpecimen.freezeThawCycles;
        }
      }
    };

    $scope.createDerivative = function() {
      SpecimenUtil.createDerivatives($scope);
    };

    $scope.revertEdit = function () {
      $scope.back();
    }

    init();
  });
