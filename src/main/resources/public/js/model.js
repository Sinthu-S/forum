function Message(){

}

Message.prototype.createMessage = function(cb){
	notify.info('Message envoyé');
	http().postJson('/').done(function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
};

Message.prototype.editMessage = function(cb){
	http().putJson('/').done(function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
};

Message.prototype.save = function(cb){
	if(!this.id){
		this.createMessage(cb);
	}
	else{
		this.editMessage(cb);
	}
};

Message.prototype.remove = function(){
	notify.info('Message supprimé');
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

Subject.prototype.open = function(cb){
	this.messages.one('sync', function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
	this.messages.sync();
};

Subject.prototype.addMessage = function(message){
	this.messages.push(message);
	message.save(function(){
		this.messages.sync();
	}.bind(this));
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
		},
		removeSelection: function(){
			http().delete('/');
			Collection.prototype.removeSelection.call(this);
		},
		lockSelection: function(){
			this.selection().forEach(function(item){
				item.locked = true;
			});
			http().putJson('/');
		},
		unlockSelection: function(){
			this.selection().forEach(function(item){
				item.locked = undefined;
			});
			http().putJson('/');
		}
	});
}

Category.prototype.open = function(cb){
	this.subjects.one('sync', function(){
		if(typeof cb === 'function'){
			cb();
		}
	}.bind(this));
	this.subjects.sync();
};

Category.prototype.addSubject = function(subject){
	this.subjects.push(subject);
	subject.save();
};

Category.prototype.createCategory = function(){

};

Category.prototype.saveModifications = function(){
	notify.info('Modifications enregistrées');
};

Category.prototype.save = function(){
	if(this.id){
		this.saveModifications();
	}
	else{
		this.createCategory();
	}
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