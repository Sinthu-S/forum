function Message(){

}

Message.prototype.createMessage = function(){
	notify.info('Message envoyé');
};

Message.prototype.editMessage = function(){

};

Message.prototype.save = function(){
	if(!this.id){
		this.createMessage();
	}
	else{
		this.editMessage();
	}
};

function Subject(){
	var subject = this;

	this.collection(Message, {
		sync: function(){
			http().get('/forum/public/json/messages-' + subject.id + '.json').done(function(messages){
				this.load(messages);
			}.bind(this));
		}
	});
}

Subject.prototype.open = function(){
	this.messages.sync();
};

Subject.prototype.addMessage = function(message){
	this.messages.push(message);
	message.save();
};

Subject.prototype.save = function(){

};

function Category(){
	var category = this;

	this.collection(Subject, {
		sync: function(){
			http().get('/forum/public/json/subjects-' + category.id + '.json').done(function(subjects){
				this.load(subjects);
			}.bind(this))
		}
	});
}

Category.prototype.open = function(){
	this.subjects.sync();
};

Category.prototype.addSubject = function(subject){
	this.subjects.push(subject);
	subject.save();
};

model.build = function(){
	this.makeModels([Category, Subject, Message]);

	this.collection(Category, {
		sync: function(){
			http().get('/forum/public/json/categories.json').done(function(categories){
				this.load(categories);
			}.bind(this));
		}
	})
};