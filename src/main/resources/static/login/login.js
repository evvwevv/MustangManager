angular.module('sample.login', [
    'ui.router',
    'angular-storage',
    'ngMaterial'
])
    .config(function ($stateProvider) {
        $stateProvider.state('login', {
            url: '/login',
            controller: 'LoginCtrl',
            templateUrl: 'login/login.html'
        });
    })
    .controller('LoginCtrl', function LoginController($scope, $http, store, $state, $rootScope) {

        $scope.user = {};
        $scope.isLoading = false;

        $scope.login = function () {
            $scope.isLoading = true;
            $http({
                url: $rootScope.server_root + 'login',
                method: 'POST',
                data: $scope.user,
                transformResponse: undefined
            }).then(function (response) {
                store.set('jwt', response.data);
                $scope.isLoading = false;
                $state.go('home');
            }, function (error) {
                $scope.isLoading = false;
                var errorTitle = 'Error ' + error.status + ': Login Failure';
                var errorText = 'An Error occurred logging in, please try again. Please make sure your username and password are correct.';
                $rootScope.errorDialogue(errorTitle, errorText);
            });
        }

    })
    .directive('autofocus', function ($timeout) {
        return {
            link: function (scope, element) {
                $timeout(function () {
                    element.focus();
                });
            }
        }
    });

