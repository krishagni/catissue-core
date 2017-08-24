angular.module('os.biospecimen.participant')
  .controller('SpecimensListViewCtrl', function(
    $scope, $state, currentUser, cp, spmnListCfg, sdeConfigured,
    Util, Specimen, SpecimensHolder, DeleteUtil, Alerts, ListPagerOpts) {

    var ctrl = this;

    var pagerOpts, listParams;

    function init() {
      pagerOpts  = new ListPagerOpts({listSizeGetter: getSpecimensCount});
      listParams = {listName: 'specimen-list-view', maxResults: pagerOpts.recordsPerPage + 1};

      ctrl.showAddSpmn = !sdeConfigured,

      $scope.ctx = {
        filtersCfg: angular.copy(spmnListCfg.filters),
        filters: {},
        specimens: {},
        listSize: -1,
        resourceOpts: {
          orderCreateOpts:    $scope.orderCreateOpts,
          shipmentCreateOpts: $scope.shipmentCreateOpts,
          specimenUpdateOpts: $scope.specimenUpdateOpts,
          specimenDeleteOpts: $scope.specimenDeleteOpts
        }
      };

      angular.extend($scope.listViewCtx, {
        listName: 'specimens.list',
        ctrl: ctrl,
        headerActionsTmpl: 'modules/biospecimen/participant/specimens-list-pager.html',
        headerButtonsTmpl: 'modules/biospecimen/participant/specimens-list-ops.html',
        showSearch: (spmnListCfg.filters && spmnListCfg.filters.length > 0)
      });

      loadSpecimens();
      Util.filter($scope, 'ctx.filters', loadSpecimens);
    }

    function loadSpecimens() {
      cp.getListDetail(listParams, getFilters()).then(
        function(specimens) {
          $scope.ctx.specimens = specimens;
          if (listParams.includeCount) {
            $scope.ctx.listSize = specimens.size;
          }

          pagerOpts.refreshOpts(specimens.rows);
        }
      );
    }

    function getSpecimensCount() {
      if (!listParams.includeCount) {
        listParams.includeCount = true;

        return cp.getListSize(listParams, getFilters()).then(
          function(size) {
            $scope.ctx.listSize = size;
            return {count: size};
          }
        );
      } else {
        return {count: $scope.ctx.listSize};
      }
    }

    function getFilters() {
      var filters = [];
      if ($scope.ctx.$listFilters) {
        filters = $scope.ctx.$listFilters.getFilters();
      }

      return filters;
    }

    function gotoView(state, params, msgCode) {
      var selectedSpmns = $scope.ctx.$list.getSelectedItems();
      if (!selectedSpmns || selectedSpmns.length == 0) {
        Alerts.error('specimen_list.' + msgCode);
        return;
      }

      var ids = selectedSpmns.map(function(spmn) { return spmn.hidden.specimenId; });
      Specimen.getByIds(ids).then(
        function(spmns) {
          SpecimensHolder.setSpecimens(spmns);
          $state.go(state, params);
        }
      );
    }

    function createNewList(spmns) {
      SpecimensHolder.setSpecimens(spmns);
      $state.go('specimen-list-addedit', {listId: ''});
    }

    $scope.showSpecimen = function(row) {
      $state.go('specimen', {specimenId: row.hidden.specimenId});
    }

    $scope.loadFilterValues = function(expr) {
      return cp.getExpressionValues(listParams.listName, expr);
    }

    $scope.setListCtrl = function($list) {
      $scope.ctx.$list = $list;
    }

    $scope.setFiltersCtrl = function($listFilters) {
      $scope.ctx.$listFilters = $listFilters;
    }

    this.getSelectedSpecimens = function() {
      var selectedSpmns = $scope.ctx.$list.getSelectedItems();
      if (!selectedSpmns || selectedSpmns.length == 0) {
        return [];
      }

      return selectedSpmns.map(function(spmn) { return {id: spmn.hidden.specimenId}; });
    }

    this.loadSpecimens = loadSpecimens;

    this.pagerOpts = function() {
      return pagerOpts;
    }

    this.addSpecimensToList = function(list) {
      var items = $scope.ctx.$list.getSelectedItems();
      if (!items || items.length == 0) {
        Alerts.error('specimens.no_specimens_for_specimen_list');
        return;
      }

      var spmns = items.map(function(item) { return {id: item.hidden.specimenId}; });
      if (!list) {
        createNewList(spmns);
      } else {
        list.addSpecimens(spmns).then(
          function() {
            var type = list.getListType(currentUser);
            Alerts.success('specimen_list.specimens_added_to_' + type, list);
          }
        );
      }
    }

    init();
  });
