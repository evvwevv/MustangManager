angular.module('sample.signup', [
    'ui.router',
    'angular-storage',
    'ngMaterial'
])
    .config(function ($stateProvider) {
        $stateProvider.state('signup', {
            url: '/signup',
            controller: 'SignupCtrl',
            templateUrl: 'signup/signup.html'
        });
    })
    .controller('SignupCtrl', function SignupController($scope, $http, store, $state, $rootScope) {

        $scope.user = {};
        $scope.isLoading = false;

        $scope.createUser = function () {
            $scope.isLoading = true;
            $http({
                url: $rootScope.server_root + 'user',
                method: 'POST',
                data: $scope.user
            }).then(function (response) {
                store.set('jwt', response.data.id_token);
                $scope.isLoading = false;
                $state.go('home');
            }, function (error) {
                $scope.isLoading = false;
                var errorTitle = 'Error ' + error.status + ': Account Creation Failure';
                if (error.status == 409) {
                    var errorText = 'This Email address is already registered.';
                } else if (error.status == 400) {
                    errorText = 'Please check to make sure all required fields were filled in correctly.';
                } else {
                    errorText = 'Sorry, there was an error in creating your account.';
                }
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