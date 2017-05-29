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
                function success() {
                    $scope.hasSuccess = true;
                },
                function error(response) {
                    var msg = response.data ? response.data.message : null;
                    $scope.hasError = true;
                    $scope.errorMsg = msg ? msg : 'Error has occurred';
                }
            ).finally(function () {
                loadPaymentsHistory();
                $scope.isProcessing = false;
            }).then(reset());
    };

    function getBrokerServiceUrl() {
        return $scope.environment.apiUrl + '/broker/payment';
    }

    function reset () {
        setTimeout(function () {
            $scope.$apply(function () {
                $scope.hasSuccess = false;
                $scope.hasError = false;
                $scope.errorMsg = '';
            });
        }, 5000);
    }

    function loadPaymentsHistory() {
        $http.get(getBrokerServiceUrl()).then(
            function success(response) {
                $scope.payments = response.data;
            },
            function error(response) {
                var msg = response.data ? response.data.message : null;
                $scope.hasError = true;
                $scope.errorMsg = msg ? msg : 'Error loading payments history';
            }
        ).then(reset());
    }

}]);
