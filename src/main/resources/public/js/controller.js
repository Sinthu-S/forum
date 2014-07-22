function ForumController($scope, template, model, date){
	template.open('main', 'categories');
	template.open('admin-main', 'categories-admin');

	$scope.template = template;

	$scope.me = model.me;
	$scope.categories = model.categories;
	$scope.date = date;

	$scope.display = {};
	$scope.editedMessage = new Message();

	$scope.switchAllSubjects = function(){
		if($scope.display.selectSubjects){
			$scope.category.subjects.selectAll();
		}
		else{
			$scope.category.subjects.deselectAll();
		}
	};

	$scope.switchAllCategories = function(){
		if($scope.display.selectCategories){
			$scope.categories.selectAll();
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
		$scope.category.addSubject($scope.subject);
		$scope.subject.addMessage($scope.editedMessage);
		template.open('main', 'read-subject');
	};

	$scope.closeSubject = function(){
		$scope.subject = undefined;
		template.open('main', 'subjects');
	};

	$scope.addMessage = function(){
		$scope.subject.addMessage($scope.editedMessage);
		$scope.editedMessage = new Message();
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
		template.open('admin-main', 'edit-category-admin');
	};

	$scope.saveCategoryEdit = function(){
		$scope.category.save();
		$scope.category = undefined;
		$scope.categories.sync();
		template.open('admin-main', 'categories-admin');
	};

	$scope.cancelCategoryEdit = function(){
		$scope.category = undefined;
		template.open('admin-main', 'categories-admin');
	};

	$scope.newCategory = function(){
		$scope.category = new Category();
		template.open('admin-main', 'edit-category-admin');
	};

	$scope.viewAuthor = function(message){
		window.location.href = '/userbook/annuaire#/' + message.owner.userId;
	}

	$scope.formatDate = function(date){
		return moment(date).format('DD MMMM YYYY hh[h]mm');
	}

	$scope.formatDateShort = function(date){
		return moment(date).format('DD/MM/YYYY hh[h]mm');
	}
}