paymentApp.controller('IndexController', ['$scope', '$http', function IndexController($scope, $http) {
    $http.get('/environment.json')
        .then(function(response) {
            $scope.environment = JSON.parse(JSON.stringify(response.data));
            loadPaymentsHistory();
        });

    $scope.payment = {};
    $scope.pay = function () {

        $scope.isProcessing = true;
        $http.post(getBrokerServiceUrl(), this.payment)
            .then(
                function() {
                    $scope.hasSuccess = true;
                }, function() {
                    $scope.hasError = true;
                    $scope.errorMsg = 'Error has occurred';
                }
            ).finally(function () {
                loadPaymentsHistory();
                $scope.isProcessing = false;
            }).then(function () {
                setTimeout(function () {
                    $scope.$apply(function () {
                        $scope.hasSuccess = false;
                        $scope.hasError = false;
                        $scope.errorMsg = '';
                    });
                }, 3000);
            });
    };

    function loadPaymentsHistory() {
        $http.get(getBrokerServiceUrl()).then(
            function success(response) {
                $scope.payments = response.data;
            },
            function error() {
                $scope.hasError = true;
                $scope.errorMsg = 'Error loading payments history';
            }
        );
    }

    function getBrokerServiceUrl() {
        return $scope.environment.apiUrl + '/broker/payment';
    }

}]);
