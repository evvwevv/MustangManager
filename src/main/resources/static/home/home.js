angular.module('sample.home', [
    'ui.router',
    'angular-storage',
    'angular-jwt',
    'ngMaterial',
    'ngAnimate',
    angularDragula(angular)
])
    .config(function ($stateProvider, $mdThemingProvider) {
        $stateProvider.state('home', {
            url: '/',
            controller: 'HomeCtrl',
            templateUrl: 'home/home.html',
            data: {
                requiresLogin: true
            }
        });

        $mdThemingProvider.theme('default')
            .primaryPalette('green');
    })
    .controller('HomeCtrl', function HomeController($scope, $http, store, jwtHelper, $mdSidenav, $log, $mdBottomSheet,
                                                    $timeout, $rootScope, dragulaService, $mdToast, $mdDialog) {
        $scope.jwt = store.get('jwt');
        $scope.decodedJwt = $scope.jwt && jwtHelper.decodeToken($scope.jwt);

        $scope.content = [];
        $scope.flowchartArr = [];
        $rootScope.flowchartName;
        $rootScope.yearArr = [];
        $scope.originatorEv;
        $scope.userName;

        $scope.showToast = function (actionText) {
            $mdToast.show({
                template: '<md-toast class= "md-toast">' + actionText.toString() + '</md-toast>',
                position: 'bottom left',
                hideDelay: 3000
            });
        };

        $rootScope.compareYear = function (year1, year2) {
            if (year1.id > year2.id) {
                return 1;
            } else if (year1.id < year2.id) {
                return -1;
            } else {
                return 0;
            }
        };

        $scope.getContent = function () {
            $http({
                url: $rootScope.server_root + 'user',
                method: 'GET',
                headers: {
                    'X-Auth-Token': $scope.jwt
                }
            }).then(function (response) {
                $scope.content = response.data;
                $scope.flowchartArr = response.data.flowcharts;
                $rootScope.flowchartName = response.data.flowcharts[0].name;
                $rootScope.yearArr = response.data.flowcharts[0].years;
                $rootScope.yearArr.sort($rootScope.compareYear);
                $rootScope.flowchartId = $scope.flowchartArr[0].id;
                var first = response.data.firstname;
                var last = response.data.lastname;
                $scope.userName = first.concat(" ", last);
            }, function (error) {
                var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                $rootScope.errorDialogue(errorTitle, errorText);
            });
        };

        $scope.getContent();

        $scope.logout = function () {
            store.set('jwt', '');
            location.reload();
        }

        $scope.toggleLeft = buildToggler('left');
        $scope.isOpenLeft = function () {
            return $mdSidenav('left').isOpen();
        };

        $scope.openMenu = function ($mdMenu, ev) {
            $scope.originatorEv = ev;
            $mdMenu.open(ev);
        };

        $scope.refreshYears = function (id) {
            $http({
                url: $rootScope.server_root + 'flowchart/' + id,
                method: 'GET',
                headers: {
                    'X-Auth-Token': $scope.jwt
                }
            }).then(function (response) {
                $rootScope.yearArr = response.data.years;
                $rootScope.yearArr.sort($rootScope.compareYear);
            }, function (error) {
                var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                $rootScope.errorDialogue(errorTitle, errorText);
            });
        };


        $scope.showSummer = function (year, name) {
            var yearID = year;
            $http({
                url: $rootScope.server_root + 'year/toggleSummer/' + yearID,
                method: 'PUT',
                headers: {
                    'X-Auth-Token': $scope.jwt
                }
            }).then(function (response) {
                var summerVal = response.data.showSummer;
                $scope.originatorEv = null;
                $scope.refreshYears($rootScope.flowchartId);
                var actionText;
                if (summerVal === true) {
                    actionText = 'Summer quarter added to year ' + name;
                }
                else {
                    actionText = 'Summer quarter removed from year ' + name;
                }
                $scope.showToast(actionText);
            }, function (error) {
                var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                $rootScope.errorDialogue(errorTitle, errorText);
            });
        };

        $scope.addYear = function () {
            $http({
                url: $rootScope.server_root + 'year/addYear/' + $rootScope.flowchartId,
                method: 'PUT',
                headers: {
                    'X-Auth-Token': $scope.jwt
                },
                data: {
                    'name': 'New Year'
                }
            }).then(function (response) {
                $scope.refreshYears($rootScope.flowchartId)
                var actionText = 'New Year added';
                $scope.showToast(actionText);
            }, function (error) {
                var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                $rootScope.errorDialogue(errorTitle, errorText);
            });
        };

        $scope.removeYear = function (yearId, yearName, ev) {
            var confirm = $mdDialog.confirm()
                .title('Confirm year removal')
                .textContent('Are you sure you want to remove this year?')
                .ariaLabel('Year Removal Confirmation')
                .targetEvent(ev)
                .ok('Yes')
                .cancel('Cancel');
            $mdDialog.show(confirm).then(function () {
                $http({
                    url: $rootScope.server_root + 'year/removeYear/' + $rootScope.flowchartId,
                    method: 'PUT',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    },
                    data: {
                        'id': yearId
                    }
                }).then(function (response) {
                    $scope.refreshYears($rootScope.flowchartId)
                    var actionText = 'Year ' + yearName + ' successfully removed';
                    $scope.showToast(actionText);
                }, function (error) {
                    var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                    var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                });
            }, function () {
            });
        };

        $scope.renameYear = function (yearId, origName, ev) {
            var confirm = $mdDialog.prompt()
                .title('Enter a year name')
                .placeholder('Year name')
                .ariaLabel('Year name')
                .initialValue('New Year')
                .targetEvent(ev)
                .ok('Okay')
                .cancel('Cancel');
            $mdDialog.show(confirm).then(function (result) {
                var yearName = result;
                $http({
                    url: $rootScope.server_root + 'year/rename/' + yearId,
                    method: 'PUT',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    },
                    data: {
                        'name': yearName
                    }
                }).then(function (response) {
                    $scope.refreshYears($rootScope.flowchartId)
                    var actionText = 'Year ' + origName + ' renamed to "' + yearName + '"';
                    $scope.showToast(actionText);
                }, function (error) {
                    var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                    var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                });
            }, function () {
            });
        };

        $scope.clearYear = function (yearId, yearName, ev) {
            var confirm = $mdDialog.confirm()
                .title('Confirm Course Removal')
                .textContent('Are you sure you want to remove all courses from ' + yearName + ' year?')
                .ariaLabel('Course Removal Confirmation')
                .targetEvent(ev)
                .ok('Yes')
                .cancel('Cancel');
            $mdDialog.show(confirm).then(function () {
                $http({
                    url: $rootScope.server_root + 'year/clear/' + yearId,
                    method: 'PUT',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    }
                }).then(function (response) {
                    $scope.refreshYears($rootScope.flowchartId)
                    var actionText = 'Courses successfully cleared from year ' + yearName;
                    $scope.showToast(actionText);
                }, function (error) {
                    var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                    var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                });
            }, function () {
            });
        }

        function buildToggler(navID) {
            return function () {
                $mdSidenav(navID)
                    .toggle();
            };
        }

        $scope.isLoading = false;
        $scope.courseArr = [];

        $scope.search = function () {
            if ($scope.query != '') {
                $scope.isLoading = true;
                $http({
                    url: $rootScope.server_root + 'course/search',
                    method: 'POST',
                    data: {
                        'name': $scope.query
                    }
                }).then(function (response) {
                    $scope.courseArr = response.data;
                    $scope.isLoading = false;
                }, function (error) {
                    $scope.isLoading = false;
                });
            }
        };

        $scope.loadCourseInfo = function (course) {
            $scope.name = course.name;
            $scope.title = course.title;
            $scope.termsOffered = course.termsOffered;
            $scope.prerequisites = course.prerequisites;
            $scope.description = course.description;
        };

        dragulaService.options($scope, 'quarter-bag', {
            copy: function (el, source) {
                return source.className.split(' ')[0] == 'courseSearchWrapper';
            },
            accepts: function (el, source) {
                return source.className.split(' ')[0] != 'courseSearchWrapper';
            },
            removeOnSpill: true
        });

        $scope.removeCourse = function(el, source) {
            var sourceId = source[0].className.split(" ")[0];
            var courseName = el[0].className.split(" ")[1];

            var dragFromSearch = false
            if (source[0].className.split(" ")[0] == 'courseSearchWrapper') {
                dragFromSearch = true;
            }

            if (!dragFromSearch) {
                $http({
                    url: $rootScope.server_root + 'quarter/deleteCourse/' + sourceId,
                    method: 'PUT',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    },
                    data: {
                        'name': courseName
                    }
                });
            }
        };

        $scope.$on('quarter-bag.remove', function(e, el, container, source) {
            $scope.removeCourse(el, source);
            $scope.showToast('Course removed');
        });

        $scope.$on('quarter-bag.drop', function (e, el, target, source) {
            var targetId = target[0].className.split(" ")[0];
            var courseName = el[0].className.split(" ")[1];

            var dragFromSearch = false
            if (source[0].className.split(" ")[0] == 'courseSearchWrapper') {
                dragFromSearch = true;
            }

            $http({
                url: $rootScope.server_root + 'quarter/addCourse/' + targetId,
                method: 'PUT',
                headers: {
                    'X-Auth-Token': $scope.jwt
                },
                data: {
                    'name': courseName
                }
            }).then(function () {
                if (!dragFromSearch) {
                    $scope.removeCourse(el, source);
                    $scope.showToast('Course moved');
                } else {
                    $scope.showToast('Course added to flowchart');
                }
            }, function (error) {
                if(error.status == 409) {
                    var dialogTitle = 'Unable to add course';
                    var dialogText = courseName + ' was unable to be moved to the selected quarter because the quarter already contains ' + courseName;
                    $rootScope.errorDialogue(dialogTitle, dialogText);
                    $scope.refreshYears($rootScope.flowchartId);
                } else {
                    var errorTitle = 'Error ' + error.status + ': API Request Failure';
                    var errorText = 'An Error occurred moving a course. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                }
            });
        });
    })
    .controller('LeftCtrl', function ($scope, $timeout, $mdSidenav, $log) {
        $scope.close = function () {
            // Component lookup should always be available since we are not using `ng-if`
            $mdSidenav('left').close();
        };
    })
    .controller('ManageFlowchartMenu', function ($scope, $http, store, jwtHelper, $mdDialog, $rootScope) {
        $scope.showMenu = false;
        $scope.namelength = 20;

        $scope.selectFlowchart = function (id) {
            $rootScope.flowchartId = id;

            $http({
                url: $rootScope.server_root + 'flowchart/' + id,
                method: 'GET',
                headers: {
                    'X-Auth-Token': $scope.jwt
                }
            }).then(function (response) {
                $rootScope.yearArr = response.data.years;
                $rootScope.yearArr.sort($rootScope.compareYear);
                $rootScope.flowchartName = response.data.name;
                var actionText = 'Selected flowchart loaded';
                $scope.showToast(actionText);
            }, function (error) {
                var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                $rootScope.errorDialogue(errorTitle, errorText);
            });
        };

        $scope.createFlowchart = function () {
            if ($scope.flowchartArr.length < 5) {
                $http({
                    url: $rootScope.server_root + 'flowchart',
                    method: 'POST',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    },
                    data: {
                        'name': 'New Flowchart'
                    }
                }).then(function (response) {
                    $scope.name = response.data.name;
                    $scope.refreshFlowchart();
                }, function (error) {
                    var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                    var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                });
            }
            else {
                var errorTitle = 'Cannot Create Additional Flowcharts';
                var errorText = 'You may have only 5 flowcharts at once.';
                $rootScope.errorDialogue(errorTitle, errorText);
            }
        };

        $scope.refreshFlowchart = function () {
            $http({
                url: $rootScope.server_root + 'user',
                method: 'GET',
                headers: {
                    'X-Auth-Token': $scope.jwt
                }
            }).then(function (response) {
                $scope.flowchartArr = response.data.flowcharts;
            });
        };

        $scope.deleteFlowchart = function (id) {
            $rootScope.flowchartId = id;

            var confirm = $mdDialog.confirm()
                .title('Confirm flowchart removal')
                .textContent('Are you sure you want to delete this flowchart?')
                .ariaLabel('Flowchart Removal Confirmation')
                .ok('Yes')
                .cancel('Cancel');
            $mdDialog.show(confirm).then(function () {
                $http({
                    url: $rootScope.server_root + 'flowchart/' + id,
                    method: 'DELETE',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    }
                }).then(function () {
                    $scope.refreshFlowchart();
                }, function (error) {
                    var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                    var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                });
            }, function () {
            });
        };

        $scope.renameFlowchart = function (id) {
            $rootScope.flowchartId = id;
            $scope.flowchartname;

            var confirm = $mdDialog.prompt()
                .clickOutsideToClose(true)
                .title('Enter Name of New Flowchart')
                .ariaLabel('Flowchart Rename Dialog')
                .ok('Save')
                .cancel('Cancel')
                .openFrom({
                    top: -50,
                    width: 30,
                    height: 80
                })
                .closeTo({
                    left: 1500
                });

            $mdDialog.show(confirm).then(function (result) {
                var flowchartname = result;

                $http({
                    url: $rootScope.server_root + 'flowchart/' + id,
                    method: 'PUT',
                    headers: {
                        'X-Auth-Token': $scope.jwt
                    },
                    data: {
                        'name': flowchartname
                    }
                }).then(function () {
                    $scope.refreshFlowchart();
                    $rootScope.flowchartName = flowchartname;
                }, function (error) {
                    var errorTitle = 'Error ' + error.status + ': Retrieve Data Failure';
                    var errorText = 'An Error occurred getting user information. Please make sure you are logged in.';
                    $rootScope.errorDialogue(errorTitle, errorText);
                });
            }, function () {

            });
        }
    })
    .directive('year', function () {
        return {
            restrict: "E",
            templateUrl: 'home/year.tmpl.html'
        };
    })
    .directive('quarter', function () {
        return {
            restrict: "E",
            templateUrl: 'home/quarter.tmpl.html'
        };
    })
    .directive('course', function () {
        return {
            restrict: "E",
            templateUrl: 'home/card.tmpl.html'
        };
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