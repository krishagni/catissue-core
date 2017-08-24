
angular.module('os.administrative.order.addedit', ['os.administrative.models', 'os.biospecimen.models'])
  .controller('OrderAddEditCtrl', function(
    $scope, $state, $translate, order, spmnRequest,
    Institute, Specimen, SpecimensHolder, Site, DistributionProtocol, DistributionOrder, SpecimenList, Alerts, Util, SpecimenUtil) {
    
    var ignoreQtyWarning = false;

    function init() {
      $scope.input = {};
      $scope.order = order;
      $scope.skipSpecimensTab = (!!order.specimenList && !!order.specimenList.id);

      order.request = spmnRequest;
      if (!!spmnRequest) {
        order.requester = spmnRequest.requestor;
        order.instituteName = spmnRequest.requestorInstitute;
      }

      $scope.dpList = [];
      $scope.instituteNames = [];
      $scope.siteList = [];
      $scope.userFilterOpts = {};

      $scope.input = {allItemStatus: false, noQtySpmnsPresent: false};

      if (!spmnRequest) {
        loadDps();
        loadInstitutes();
      }

      setUserAndSiteList(order);

      if (!$scope.skipSpecimensTab) {
        if (!order.id && angular.isArray(SpecimensHolder.getSpecimens())) {
          order.orderItems = getOrderItems(SpecimensHolder.getSpecimens());
          SpecimensHolder.setSpecimens(null);
        } else if (!!order.id) {
          loadOrderItems();
        }
      }

      if (!order.executionDate) {
        order.executionDate = new Date();
      }
      
      setHeaderStatus();
      setNoQtySpmnPresent();
    }

    function loadDps(name) {
      var filterOpts = {activityStatus: 'Active', query: name, excludeExpiredDps: true};
      if (!!spmnRequest) {
        filterOpts.receivingInstitute = spmnRequest.requestorInstitute;
      }

      DistributionProtocol.query(filterOpts).then(
        function(dps) {
          $scope.dpList = dps;
        }
      );
    }
    
    function loadInstitutes () {
      Institute.query().then(
        function (institutes) {
          $scope.instituteNames = Institute.getNames(institutes);
        }
      );
    }

    function loadOrderItems() {
      order.getOrderItems().then(
        function(orderItems) {
          order.orderItems = orderItems;

          if (!!spmnRequest) {
            order.orderItems = getOrderItemsFromReq(spmnRequest.items, order.orderItems);
          }
        }
      );
    }
    
    function setUserFilterOpts(institute) {
      $scope.userFilterOpts = {institute: institute};
    }

    function setUserAndSiteList(order) {
      var instituteName = order.instituteName;
      setUserFilterOpts(instituteName);
    }

    function getOrderItems(specimens) {
      return specimens.filter(
        function(specimen) {
          return specimen.activityStatus == 'Active';
        }
      ).map(
        function(specimen) {
          return {
            specimen: specimen,
            quantity: specimen.availableQty,
            status: 'DISTRIBUTED_AND_CLOSED'
          }
        }
      );
    }

    function getOrderItemsFromReq(reqItems, orderItems) {
      var orderItemsMap = {};
      angular.forEach(orderItems,
        function(orderItem) {
          orderItemsMap[orderItem.specimen.id] = orderItem;
        }
      );

      var items = [];
      angular.forEach(reqItems,
        function(reqItem) {
          var orderItem = orderItemsMap[reqItem.specimen.id];
          if (orderItem) {
            orderItem.specimen.selected = true;
          } else if (reqItem.status == 'PENDING') {
            orderItem = {
              specimen: reqItem.specimen,
              quantity: reqItem.specimen.availableQty,
              status: reqItem.selected ? 'DISTRIBUTED_AND_CLOSED' : 'DISTRIBUTED',
            }

            reqItem.specimen.selected = reqItem.selected;
          }

          if (orderItem) {
            items.push(orderItem);
          }
        }
      );

      return items;
    }

    function areItemQuantitiesValid(order, callback) {
      if (ignoreQtyWarning) {
        return true;
      }

      var moreQtyItems = order.orderItems.filter(
        function(item) {
          return angular.isNumber(item.specimen.availableQty) && item.specimen.availableQty < item.quantity;
        }
      );

      if (moreQtyItems.length > 0) {
        showInsufficientQtyWarning(moreQtyItems, callback);
        return false;
      }

      return true;
    }

    function showInsufficientQtyWarning(items, callback) {
      Util.showConfirm({
        ok: function () {
          ignoreQtyWarning =  true;
          callback();
        },
        title: "common.warning",
        isWarning: true,
        confirmMsg: "orders.errors.insufficient_qty",
        input: {
          count: items.length
        }
      });
    }

    function saveOrUpdate(order) {
      if (!areItemQuantitiesValid(order, function() { saveOrUpdate(order); })) {
        return;
      }

      order.siteId = undefined;

      var orderClone = angular.copy(order);
      if (!!spmnRequest) {
        var items = [];
        angular.forEach(orderClone.orderItems,
          function(item) {
            if (!item.specimen.selected) {
              return;
            }

            delete item.specimen.selected;
            items.push(item);
          }
        );
        orderClone.orderItems = items;
      }

      orderClone.$saveOrUpdate().then(
        function(savedOrder) {
          if (savedOrder.completed) {
            $state.go('order-detail.overview', {orderId: savedOrder.id});
          } else {
            Alerts.info('orders.more_time');
            $state.go('order-list');
          }
        }
      );
    };
    
    function setHeaderStatus() {
      var isOpenPresent = false;
      angular.forEach($scope.order.orderItems,
        function(item) {
          if (item.status == 'DISTRIBUTED') {
            isOpenPresent = true;
          }
        }
      );

      if (isOpenPresent) {
        $scope.input.allItemStatus = false;
      } else {
        $scope.input.allItemStatus = true;
      }
    }

    function setNoQtySpmnPresent() {
      if (!!$scope.order.orderItems && $scope.order.orderItems.length > 0) {
        $scope.input.noQtySpmnsPresent = anyNoQtySpmns($scope.order.orderItems);
      } else if (!!$scope.order.specimenList && !!$scope.order.specimenList.id) {
        $scope.order.specimenList.getSpecimens({noQty: true, maxResults: 1}).then(
          function(specimens) {
            if (!!specimens && specimens.length > 0) {
              $scope.input.noQtySpmnsPresent = true;
            }
          }
        );
      }
    }

    function anyNoQtySpmns(items) {
      return (items || []).some(
        function(item) {
          return !item.specimen.availableQty;
        }
      );
    }

    function getValidationMsgKeys(useBarcode) {
      return {
        title:         'orders.specimen_validation.title',
        foundCount:    'orders.specimen_validation.found_count',
        notFoundCount: 'orders.specimen_validation.not_found_count',
        notFoundError: 'orders.specimen_validation.not_found_error',
        extraCount:    'orders.specimen_validation.extra_count',
        extraError:    'orders.specimen_validation.extra_error',
        itemLabel:     useBarcode ? 'specimens.barcode' : 'specimens.label',
        error:         'common.error',
        reportCopied:  'orders.specimen_validation.report_copied'
      }
    }

    $scope.onDpSelect = function() {
      if (!!spmnRequest) {
        return;
      }

      $scope.order.instituteName = $scope.order.distributionProtocol.instituteName;
      $scope.order.siteName = $scope.order.distributionProtocol.defReceivingSiteName;
      $scope.order.requester = $scope.order.distributionProtocol.principalInvestigator;
      setUserAndSiteList($scope.order);
    }
    
    $scope.onInstSelect = function () {
      var instName = $scope.order.instituteName;
      setUserFilterOpts(instName);
      $scope.order.siteName = '';
      $scope.order.requester = '';
    }

    $scope.addSpecimens = function(specimens) {
      if (!specimens) {
        return false;
      }

      ignoreQtyWarning = false;
      var newItems = getOrderItems(specimens);
      Util.addIfAbsent($scope.order.orderItems, newItems, 'specimen.id');
      $scope.input.noQtySpmnsPresent = $scope.input.noQtySpmnsPresent || anyNoQtySpmns(newItems);
      return true;
    }

    $scope.removeOrderItem = function(orderItem) {
      var idx = order.orderItems.indexOf(orderItem);
      if (idx == -1) {
        return;
      }

      order.orderItems.splice(idx, 1);
      if ($scope.input.noQtySpmnsPresent && !orderItem.specimen.availableQty) {
        $scope.input.noQtySpmnsPresent = anyNoQtySpmns(order.orderItems);
      }
    }

    $scope.distribute = function() {
      $scope.order.status = 'EXECUTED';
      saveOrUpdate($scope.order);
    }

    $scope.saveDraft = function() {
      $scope.order.status = 'PENDING';
      saveOrUpdate($scope.order);
    }

    $scope.passThrough = function() {
      return true;
    }

    $scope.areItemQuantitiesValid = function(doWizard) {
      if (!doWizard.forward) {
        return true;
      }

      return areItemQuantitiesValid($scope.order, function () { doWizard.next(false); });
    }

    $scope.setStatus = function(item) {
      var selected = !spmnRequest ? true : item.specimen.selected;

      if ((!item.specimen.availableQty || item.quantity >= item.specimen.availableQty) && selected) {
        item.status = 'DISTRIBUTED_AND_CLOSED';
      } else {
        item.status = 'DISTRIBUTED';
      }
      
      setHeaderStatus();
    }
    
    $scope.setHeaderStatus = setHeaderStatus;

    $scope.toggleAllItemStatus = function() {
      var allStatus;
      if ($scope.input.allItemStatus) {
        allStatus = 'DISTRIBUTED_AND_CLOSED';
      } else {
        allStatus = 'DISTRIBUTED';
      }
      
      angular.forEach($scope.order.orderItems,
        function(item) {
          if (item.quantity != item.specimen.availableQty) {
            item.status = allStatus;
          }
        }
      );
    }
    
    $scope.searchDp = loadDps;

    $scope.toggleAllSpecimensSelect = function() {
      angular.forEach($scope.order.orderItems,
        function(item) {
          item.specimen.selected = $scope.input.allSelected;
        }
      );
    }

    $scope.toggleSpecimenSelect = function(item) {
      if (!item.specimen.selected) {
        $scope.input.allSelected = false;
      } else {
        var allNotSelected = $scope.order.orderItems.some(
          function(item) {
            return !item.specimen.selected;
          }
        );

        $scope.input.allSelected = !allNotSelected;
      }

      $scope.setStatus(item);
    }

    $scope.validateSpecimens = function(ctrl) {
      var prop = ctrl.useBarcode() ? 'specimen.barcode' : 'specimen.label';
      var result = Util.validateItems($scope.order.orderItems, ctrl.getLabels(), prop);
      Util.showItemsValidationResult(getValidationMsgKeys(ctrl.useBarcode()), result);
    }
    
    init();
  });
