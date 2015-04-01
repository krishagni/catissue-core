angular.module('openspecimen')
  .directive('osSelect', function() {
    function linker(scope, element, attrs) {
      var changeFn = undefined
      if (attrs.onChange) {
        changeFn = scope.$eval(attrs.onChange);
        scope.$watch(attrs.ngModel, function(newVal, oldVal) {
          if (newVal != oldVal) {
            scope.$eval(attrs.onChange)(newVal);
          }
        });
      }
    }

    function getItemDisplayValue(item, tAttrs) {
      var result = item;
      if (!!tAttrs.displayProp) {
        result += '.' + tAttrs.displayProp;
      }
      return result;
    }  

    return {
      restrict: 'E',
      compile: function(tElem, tAttrs) {
        var multiple = angular.isDefined(tAttrs.multiple);
        var uiSelect = angular.element(multiple ? '<ui-select multiple/>' : '<ui-select/>')
          .attr('ng-model', tAttrs.ngModel)
          .attr('ng-disabled', tAttrs.ngDisabled)
          .attr('reset-search-input', true);
    
        if (tAttrs.onSelect) {
          uiSelect.attr('on-select', tAttrs.onSelect);
        }

        var uiSelectMatch = angular.element('<ui-select-match/>')
          .attr('placeholder', tAttrs.placeholder);
        
        var searchItem = getItemDisplayValue('item', tAttrs);
        var uiSelectChoices = angular.element('<ui-select-choices/>')
          .attr('repeat', "item in " + tAttrs.list + " | filter: $select.search")
          .append('<span ng-bind-html="' + searchItem + ' | highlight: $select.search"></span>');

        if (multiple) {
          uiSelectMatch.append('{{' + getItemDisplayValue('$item', tAttrs) + '}}');
        } else {
          uiSelectMatch.append('{{' + getItemDisplayValue('$select.selected', tAttrs) + '}}');
        }

        if (angular.isDefined(tAttrs.refresh)) {
          uiSelectChoices.attr({
            'refresh': tAttrs.refresh + '($select.search, ' + tAttrs.refreshArgs + ')',
            'refresh-delay': tAttrs.refreshDelay || 750
          });
        }
            
        uiSelect.append(uiSelectMatch).append(uiSelectChoices);
        
        var selectContainer = angular.element("<div/>")
          .addClass("os-select-container")
          .append(uiSelect);

        tElem.replaceWith(selectContainer);
        return linker;
      }
    };
  });