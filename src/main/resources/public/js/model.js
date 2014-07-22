function Message(){
	// content
}

Message.prototype.createMessage = function(cb){
	notify.info('Message envoyé');
	http().postJson('/forum/category/' + this.subject.category._id + '/subject/' + this.subject._id + '/messages', this).done(function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
};

Message.prototype.editMessage = function(cb){
	http().putJson('/forum/category/' + this.subject.category._id + '/subject/' + this.subject._id + '/message/' + this._id, this).done(function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
};

Message.prototype.save = function(cb){
	if(!this._id){
		this.createMessage(cb);
	}
	else{
		this.editMessage(cb);
	}
};

Message.prototype.remove = function(){
	http().delete('/forum/category/' + this.subject.category._id + '/subject/' + this.subject._id + '/message/' + this._id).done(function(){
		notify.info('Message supprimé');
	});
};

Message.prototype.toJSON = function(){
	return {
		content: this.content
	}
};


function Subject(){
	var subject = this;

	this.collection(Message, {
		sync: function(){
			http().get('/forum/category/' + subject.category._id + '/subject/' + subject._id + '/messages').done(function(messages){
				_.each(messages, function(message){
					message.subject = subject;
				});
				this.load(messages);
			}.bind(this));
		},
		behaviours: 'forum'
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
	message.subject = this;
	message.owner = {
		userId: model.me.userId,
		displayName: model.me.username
	}
	this.messages.push(message);
	message.save(function(){
		this.messages.sync();
	}.bind(this));
};

Subject.prototype.createSubject = function(){
	http().postJson('/forum/category/' + this.category._id + '/subjects', this).done(function(e){
		model.categories.sync();
	}.bind(this));
};

Subject.prototype.saveModifications = function(){
	http().putJson('/forum/category/' + this.category._id + '/subject/' + this._id, this).done(function(e){
		notify.info('Modifications enregistrées');
	});
};

Subject.prototype.save = function(){
	if(this._id){
		this.saveModifications();
	}
	else{
		this.createSubject();
	}
};

Subject.prototype.toJSON = function(){
	return {
		title: this.title,
		locked: this.locked
	}
};

function Category(){
	var category = this;

	this.collection(Subject, {
		sync: function(){
			http().get('/forum/category/' + category._id + '/subjects').done(function(subjects){
				_.each(subjects, function(subject){
					subject.category = category;
					if (! subject.nbMessages) {
						subject.nbMessages = 0;
					}
					if (subject.messages instanceof Array) {
						subject.lastMessage = subject.messages[0];
					}
				});
				this.load(subjects);
			}.bind(this))
		},
		removeSelection: function(){
			var counter = this.selection().size();
			this.selection().forEach(function(item){
				http().delete('/forum/category/' + category._id + '/subject/' + item._id).done(function(){
					counter = counter - 1;
					if (counter === 0) {
						Collection.prototype.removeSelection.call(this);
					}
				});
			});
		},
		lockSelection: function(){
			this.selection().forEach(function(item){
				item.locked = true;
			});
			// TODO BACK : Lock Subjects
			// http().putJson('/');
		},
		unlockSelection: function(){
			this.selection().forEach(function(item){
				item.locked = undefined;
			});
			// TODO BACK : Unlock Subjects
			//http().putJson('/');
		},
		behaviours: 'forum'
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
	subject.category = this;
	subject.owner = {
		userId: model.me.userId,
		displayName: model.me.username
	}
	this.subjects.push(subject);
	subject.save(function(){
		this.subjects.sync();
	}.bind(this));
};

Category.prototype.createCategory = function(){
	http().postJson('/forum/categories', this).done(function(e){
		model.categories.sync();
	}.bind(this));
};

Category.prototype.saveModifications = function(){
	http().putJson('/forum/category/' + this._id, this).done(function(e){
		notify.info('Modifications enregistrées');
	});
};

Category.prototype.save = function(){
	if(this._id){
		this.saveModifications();
	}
	else{
		this.createCategory();
	}
};

Category.prototype.toJSON = function(){
	return {
		name: this.name,
		icon: this.icon
	}
};

model.build = function(){
	this.makeModels([Category, Subject, Message]);

	this.collection(Category, {
		sync: function(){
			http().get('/forum/categories').done(function(categories){
				this.load(categories);
			}.bind(this));
		},
		behaviours: 'forum'
	})
};