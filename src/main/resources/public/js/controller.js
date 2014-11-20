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
			template.open('categories', 'categories');
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
			template.open('categories', 'categories');
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
			template.open('categories', 'categories');
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

	$scope.hideAlmostAllButtons = function(category){
		$scope.categories.forEach(function(cat){
			if(cat !== category){
				cat.showButtons = false;
			}
		});
	};

	$scope.openCategory = function(category){
		$scope.category = category;
		$scope.subjects = category.subjects;
		$scope.categories.forEach(function(cat){
			if(cat !== category){
				cat.showButtons = false;
			}
		});
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
		template.close('main');
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

	$scope.confirmRemoveSelectedSubjects = function() {
		$scope.display.confirmDeleteSubjects = true;
	};

	$scope.removeSelectedSubjects = function() {
		$scope.subjects.removeSelection(function(){
			$scope.display.confirmDeleteSubjects = undefined;
		});
	};

	$scope.cancelRemoveSubjects = function() {
		$scope.display.confirmDeleteSubjects = undefined;
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
		template.open('main', 'read-subject');
	};
	
	$scope.cancelAddMessage = function(){
		template.open('main', 'read-subject');
	};

	$scope.editMessage = function(message){
		$scope.editedMessage = message;
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

	$scope.confirmRemoveMessage = function(message){
		$scope.removedMessage = message;
		$scope.display.confirmDelete = true;
	};

	$scope.removeMessage = function(){
		$scope.subject.messages.remove($scope.removedMessage);
		delete $scope.display.confirmDelete;
		$scope.removedMessage.remove();
	};

	$scope.cancelRemoveMessage = function(){
		delete $scope.removedMessage;
		delete $scope.display.confirmDelete;
	};

	$scope.editCategory = function(category, event){
		$scope.category = category;
		event.stopPropagation();
		template.open('main', 'edit-category');
	};

	$scope.shareCategory = function(category, event){
		$scope.category = category;
		$scope.display.showPanel = true;
		event.stopPropagation();
	};

	$scope.saveCategoryEdit = function(){
		if ($scope.category._id) { // when editing a category
			$scope.category.save(function(){
				$scope.categories.sync(function(){
					$scope.cancelCategoryEdit();
					$scope.$apply();
				});
			});
		}
		else { // when creating a category
			$scope.category.save(function(){
				template.open('main', 'share-category');
			});
			$scope.categories.sync();
		}
		template.close('main');
	};

	$scope.cancelCategoryEdit = function(){
		$scope.category = undefined;
		template.close('main');
	};

	$scope.newCategory = function(){
		$scope.category = new Category();
		template.open('main', 'edit-category');
	};

	$scope.confirmRemoveCategory = function(category, event){
		$scope.categories.deselectAll();
		category.selected = true;
		$scope.display.confirmDeleteCategories = true;
		event.stopPropagation();
	};

	$scope.removeSelectedCategories = function() {
		$scope.categories.removeSelection(function(){
			$scope.cancelRemoveCategory();
		});
	};

	$scope.cancelRemoveCategory = function() {
		$scope.categories.deselectAll();
		$scope.display.confirmDeleteCategories = undefined;
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
	
	$scope.ownerCanEditMessage = function(subject, message) {
		return (!subject.myRights.publish && 
				!subject.category.myRights.publish &&
				model.me.userId === message.owner.userId && 
				!subject.locked);
	};
}