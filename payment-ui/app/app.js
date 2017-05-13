'use strict';

var paymentApp = angular.module('paymentApp', ['ngRoute']);

paymentApp.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'app/component/index/index_view.html',
        controller: 'IndexController'
    }).otherwise({
        redirectTo: '/'
    });
}]);

