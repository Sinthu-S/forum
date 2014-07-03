function ForumController($scope, template, model, date){
	template.open('main', 'categories');

	$scope.categories = model.categories;
	$scope.date = date;

	$scope.display = {};

	$scope.message = new Message();

	$scope.selectAll = function(){
		if($scope.display.selectSubjects){
			$scope.category.selectAll();
		}
		else{
			$scope.category.deselectAll();
		}
	};

	$scope.openCategory = function(category){
		$scope.category = category;
		$scope.subjects = category.subjects;
		category.open();
		template.open('main', 'subjects');
	};

	$scope.openSubject = function(subject){
		$scope.subject = subject;
		subject.open();
		template.open('main', 'read-subject');
		$scope.messages =  subject.messages;
	};

	$scope.newSubject = function(){
		$scope.subject = new Subject();
		template.open('main', 'new-subject');
	};

	$scope.addSubject = function(){
		$scope.category.addSubject($scope.subject);
		$scope.subject.addMessage($scope.message);
		template.open('main', 'read-subject');
	};

	$scope.closeSubject = function(){
		$scope.subject = undefined;
		template.open('main', 'subjects');
	};

	$scope.addMessage = function(){
		$scope.subject.addMessage($scope.message);
		$scope.message = new Message();
	}
}