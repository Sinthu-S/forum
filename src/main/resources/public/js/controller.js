routes.define(function($routeProvider){
    $routeProvider
      .when('/view/:categoryId', {
        action: 'goToCategory'
      })
      .when('/view/:categoryId/:subjectId', {
        action: 'goToSubject'
      })
	  .otherwise({
        action: 'mainPage'
      });
});

function ForumController($scope, template, model, date, route){
	$scope.notFound = false;
	$scope.template = template;

	$scope.me = model.me;
	$scope.categories = model.categories;
	$scope.date = date;

	$scope.display = {};
	$scope.editedMessage = new Message();
	
	// Definition of actions
	route({
		goToCategory: function(params){
			model.categories.one('sync', function(){
				$scope.category = undefined;
				$scope.category = model.categories.find(function(category){
					return category._id === params.categoryId;
				});
				if($scope.category === undefined){
					$scope.notFound = true;
					template.open('error', '404');
				}
				else{
					$scope.notFound = false;
					$scope.openCategory($scope.category);
				}
			});
			model.categories.sync();
		},
		goToSubject: function(params){
			model.categories.one('sync', function(){
				$scope.category = undefined;
				$scope.category = model.categories.find(function(category){
					return category._id === params.categoryId;
				});
				if($scope.category === undefined){
					$scope.notFound = true;
					template.open('error', '404');
				}
				else{
					$scope.category.subjects.one('sync', function(){
						$scope.subjects = $scope.category.subjects;
						$scope.subject = undefined;
						$scope.subject = $scope.subjects.find(function(subject){
							return subject._id === params.subjectId;
						});
						if($scope.subject === undefined){
							$scope.notFound = true;
							template.open('error', '404');
						}
						else{
							$scope.notFound = false;
							$scope.openSubject($scope.subject);
						}
					});
					$scope.category.subjects.sync();
				}
			});
			model.categories.sync();
		},
		mainPage: function(){
			template.open('main', 'categories');
		}
	});

	$scope.switchAllSubjects = function(){
		if($scope.display.selectSubjects){
			if($scope.category.myRights.manage){
				$scope.category.subjects.selectAll();
			}
		}
		else{
			$scope.category.subjects.deselectAll();
		}
	};
	
	$scope.switchAllCategories = function(){
		if($scope.display.selectCategories){
			$scope.categories.forEach(function(item){
				if(item.myRights.manage){
					item.selected = true;
				}
			});
		}
		else{
			$scope.categories.deselectAll();
		}
	};

	$scope.openCategory = function(category){
		$scope.category = category;
		$scope.subjects = category.subjects;
		category.open(function(){
			template.open('main', 'subjects');
		});
	};

	$scope.openSubject = function(subject){
		$scope.subject = subject;
		subject.open(function(){
			template.open('main', 'read-subject');
		});
		$scope.messages =  subject.messages;
	};
	
	$scope.openMainPage = function(){
		template.open('main', 'categories');
	}

	$scope.newSubject = function(){
		$scope.subject = new Subject();
		template.open('main', 'new-subject');
	};

	$scope.addSubject = function(){
		if($scope.subject.title !== undefined){
			var title = $scope.subject.title.replace(/ /g, '');
			title = title.replace(/&nbsp;/g, '');
			var content = $scope.editedMessage.content.replace(/ /g, '');
			content = content.replace(/&nbsp;/g, '');
			if(title !== "" && content !== "<divclass=\"ng-scope\"></div>"){
				$scope.category.addSubject($scope.subject, function(){
					$scope.subject.addMessage($scope.editedMessage);
					$scope.messages = $scope.subject.messages;
					$scope.editedMessage = new Message();
					$scope.editedMessage.content = "";
				});
				template.open('main', 'read-subject');
			}
		}
	};

	$scope.closeSubject = function(){
		$scope.subject = undefined;
		$scope.subjects.sync();
		template.open('main', 'subjects');
	};

	$scope.addMessage = function(){
		var content = $scope.editedMessage.content.replace(/ /g, '');
		content = content.replace(/&nbsp;/g, '');
		if(content !== "<divclass=\"ng-scope\"></div>"){
			$scope.subject.addMessage($scope.editedMessage);
			$scope.editedMessage = new Message();
			$scope.editedMessage.content = "";
		}
	};

	$scope.editMessage = function(message){
		$scope.editedMessage = message;
	};

	$scope.removeMessage = function(message){
		$scope.subject.messages.remove(message);
		message.remove();
	};

	$scope.saveEditMessage = function(){
		$scope.editedMessage.save();
		$scope.editedMessage = new Message();
	};

	$scope.cancelEditMessage = function(){
		$scope.editedMessage = new Message();
	};

	$scope.editCategory = function(){
		$scope.category = $scope.categories.selection()[0];
		template.open('main', 'edit-category');
	};

	$scope.saveCategoryEdit = function(){
		$scope.category.save();
		$scope.category = undefined;
		$scope.categories.sync();
		template.open('main', 'categories');
	};

	$scope.cancelCategoryEdit = function(){
		$scope.category = undefined;
		template.open('main', 'categories');
	};

	$scope.newCategory = function(){
		$scope.category = new Category();
		template.open('main', 'edit-category');
	};

	$scope.viewAuthor = function(message){
		window.location.href = '/userbook/annuaire#/' + message.owner.userId;
	}

	$scope.formatDate = function(date){
		return moment(date, "YYYY-MM-DDTHH:mm:ss.SSSZ").format('DD MMMM YYYY HH[h]mm');
	}

	$scope.formatDateShort = function(date){
		return moment(date, "YYYY-MM-DDTHH:mm:ss.SSSZ").format('DD/MM/YYYY HH[h]mm');
	}
}