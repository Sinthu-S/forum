routes.define(function($routeProvider){
    $routeProvider
      .when('/view/:categoryId', {
        action: 'goToCategory'
      })
      .when('/view/:categoryId/:subjectId', {
        action: 'goToSubject'
      });
});

function ForumController($scope, template, model, date, route){
	template.open('main', 'categories');

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
				$scope.category = model.categories.find(function(category){
					return category._id === params.categoryId;
				});
				$scope.openCategory($scope.category);
			});
			model.categories.sync();
		},
		goToSubject: function(params){
			model.categories.one('sync', function(){
				$scope.category = model.categories.find(function(category){
					return category._id === params.categoryId;
				});
				
				$scope.category.subjects.one('sync', function(){
					$scope.subjects = $scope.category.subjects;
					console.log("$scope.category.subjects : " + $scope.category.subjects.all.length);
					$scope.subject = $scope.subjects.find(function(subject){
						return subject._id === params.subjectId;
					});
					console.log("subject : " + $scope.subject);
					$scope.openSubject($scope.subject);
				});
				$scope.category.subjects.sync();
			});
			model.categories.sync();
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

	$scope.newSubject = function(){
		$scope.subject = new Subject();
		template.open('main', 'new-subject');
	};

	$scope.addSubject = function(){
		$scope.category.addSubject($scope.subject, function(){
			$scope.subject.addMessage($scope.editedMessage);
			$scope.messages = $scope.subject.messages;
			$scope.editedMessage = new Message();
			$scope.editedMessage.content = "";
		});
		template.open('main', 'read-subject');
	};

	$scope.closeSubject = function(){
		$scope.subject = undefined;
		$scope.subjects.sync();
		template.open('main', 'subjects');
	};

	$scope.addMessage = function(){
		$scope.subject.addMessage($scope.editedMessage);
		$scope.editedMessage = new Message();
		$scope.editedMessage.content = "";
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