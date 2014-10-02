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
		delete $scope.category;
		template.open('main', 'categories');
	}

	$scope.newSubject = function(){
		$scope.subject = new Subject();
		template.open('main', 'new-subject');
	};

	$scope.addSubject = function(){
		if ($scope.isTitleEmpty($scope.subject.title)) {
			$scope.subject.title = undefined;
			$scope.subject.error = 'forum.subject.missing.title';
			return;	
		}

		if ($scope.isTextEmpty($scope.editedMessage.content)) {
			$scope.subject.error = 'forum.message.empty';
			return;
		}

		$scope.subject.error = undefined;
		$scope.category.addSubject($scope.subject, function(){
			$scope.subject.addMessage($scope.editedMessage);
			$scope.messages = $scope.subject.messages;
			$scope.editedMessage = new Message();
			$scope.editedMessage.content = "";
		});
		template.open('main', 'read-subject');
	};

	$scope.closeSubject = function(){
		$scope.subject.error = undefined;
		$scope.subject = undefined;
		$scope.subjects.sync();
		template.open('main', 'subjects');
	};

	$scope.addMessage = function(){
		if ($scope.isTextEmpty($scope.editedMessage.content)) {
			$scope.editedMessage.error = 'forum.message.empty';
			return;
		}

		$scope.editedMessage.error = undefined;
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
		if ($scope.isTextEmpty($scope.editedMessage.content)) {
			$scope.editedMessage.error = 'forum.message.empty';
			return;
		}

		$scope.editedMessage.error = undefined;
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
		if ($scope.category._id) { // when editing a category
			$scope.category.save();
			delete $scope.category;
			$scope.categories.sync(function(){
				template.open('main', 'categories');
				$scope.$apply();
			});
		}
		else { // when creating a category
			$scope.category.save(function(){
				template.open('main', 'share-category');
			});
			$scope.categories.sync();
		}
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
	
	$scope.scrollTo = function(item){
		window.scrollTo(0, $("#" + item)[0].offsetTop -100);
	}

	$scope.isTitleEmpty = function(str) {
		if (str !== undefined && str.replace(/ |&nbsp;/g, '') !== "") {
			return false;
		}
		return true;
	}

	$scope.isTextEmpty = function(str) {
		if (str !== undefined && str.replace(/<div class="ng-scope">|<\/div>|<br>|<p>|<\/p>|&nbsp;| /g, '') !== "") {
			return false;
		}
		return true;
	}
}