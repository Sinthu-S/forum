var forumNamespace = {
	Message : function() {
	},

	Subject : function () {
		var subject = this;
		this.collection(forumNamespace.Message, {
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
	},

	Category : function() {
		var category = this;
		this.collection(forumNamespace.Subject, {
			sync: function(callback){
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
					if(typeof callback === 'function'){
						callback();
					}
				}.bind(this))
			},
			removeSelection: function(callback){
				var counter = this.selection().length;
				this.selection().forEach(function(item){
					http().delete('/forum/category/' + category._id + '/subject/' + item._id).done(function(){
						counter = counter - 1;
						if (counter === 0) {
							Collection.prototype.removeSelection.call(this);
							category.subjects.sync();
							if(typeof callback === 'function'){
								callback();
							}
						}
					});
				});
			},
			lockSelection: function(){
				var counter = this.selection().length;
				this.selection().forEach(function(item){
					item.locked = true;
					http().putJson('/forum/category/' + category._id + '/subject/' + item._id, item).done(function(){
						counter = counter - 1;
						if (counter === 0) {
							category.subjects.sync();
						}
					});
				});
				notify.info('forum.subject.locked');
			},
			unlockSelection: function(){
				var counter = this.selection().length;
				this.selection().forEach(function(item){
					item.locked = false;
					http().putJson('/forum/category/' + category._id + '/subject/' + item._id, item).done(function(){
						counter = counter - 1;
						if (counter === 0) {
							category.subjects.sync();
						}
					});
				});
				notify.info('forum.subject.unlocked');
			},
			behaviours: 'forum'
		});
	}
}

forumNamespace.Message.prototype.createMessage = function(cb){
	notify.info('forum.message.sent');
	http().postJson('/forum/category/' + this.subject.category._id + '/subject/' + this.subject._id + '/messages', this).done(function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
};

forumNamespace.Message.prototype.editMessage = function(cb){
	http().putJson('/forum/category/' + this.subject.category._id + '/subject/' + this.subject._id + '/message/' + this._id, this).done(function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
};

forumNamespace.Message.prototype.save = function(cb){
	if(!this._id){
		this.createMessage(cb);
	}
	else{
		this.editMessage(cb);
	}
};

forumNamespace.Message.prototype.remove = function(){
	http().delete('/forum/category/' + this.subject.category._id + '/subject/' + this.subject._id + '/message/' + this._id).done(function(){
		notify.info('forum.message.deleted');
	});
};

forumNamespace.Message.prototype.toJSON = function(){
	return {
		content: this.content
	}
};

forumNamespace.Subject.prototype.open = function(cb){
	this.messages.one('sync', function(){
		if(typeof cb === 'function'){
			cb();
		}
	});
	this.messages.sync();
};

forumNamespace.Subject.prototype.addMessage = function(message){
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

forumNamespace.Subject.prototype.createSubject = function(cb){
	var subject = this;
	http().postJson('/forum/category/' + this.category._id + '/subjects', this).done(function(e){
		subject.updateData(e);
		if(typeof cb === 'function'){
			cb();
		}
	}.bind(this));
};

forumNamespace.Subject.prototype.saveModifications = function(cb){
	http().putJson('/forum/category/' + this.category._id + '/subject/' + this._id, this).done(function(e){
		notify.info('forum.subject.modification.saved');
		if(typeof cb === 'function'){
			cb();
		}
	});
};

forumNamespace.Subject.prototype.save = function(cb){
	if(this._id){
		this.saveModifications(cb);
	}
	else{
		this.createSubject(cb);
	}
};

forumNamespace.Subject.prototype.toJSON = function(){
	return {
		title: this.title,
		locked: this.locked
	}
};

forumNamespace.Category.prototype.sync = function(cb){
	http().get('/forum/category/' + this._id).done(function(category){
		this.updateData(category);
		this.subjects.sync(cb);
	}.bind(this))
};

model.makeModels(forumNamespace);


var forumBehaviours = {
	resources: {
		contrib: {
			right: 'net-atos-entng-forum-controllers-ForumController|createMessage'
		},
		publish: {
			right: 'net-atos-entng-forum-controllers-ForumController|createSubject'
		},
		manage: {
			right: 'net-atos-entng-forum-controllers-ForumController|updateCategory'
		},
		share: {
			right: 'net-atos-entng-forum-controllers-ForumController|shareCategory'
		}
	},
	workflow: {
		admin: 'net.atos.entng.forum.controllers.ForumController|createCategory'
	}
};


Behaviours.register('forum', {
	namespace: forumNamespace,
	behaviours: forumBehaviours,
	resource: function(resource){
		var rightsContainer = resource;
		if(resource instanceof forumNamespace.Subject && resource.category){
			rightsContainer = resource.category;
		}
		if(resource instanceof forumNamespace.Message && resource.subject && resource.subject.category){
			rightsContainer = resource.subject.category;
		}
		if(!resource.myRights){
			resource.myRights = {};
		}

		for(var behaviour in forumBehaviours.resources){
			if(model.me.hasRight(rightsContainer, forumBehaviours.resources[behaviour]) 
					|| model.me.userId === resource.owner.userId 
					|| model.me.userId === rightsContainer.owner.userId){
				if(resource.myRights[behaviour] !== undefined){
					resource.myRights[behaviour] = resource.myRights[behaviour] && forumBehaviours.resources[behaviour];
				}
				else{
					resource.myRights[behaviour] = forumBehaviours.resources[behaviour];
				}
			}
		}
		return resource;
	},
	workflow: function(){
		var workflow = { };
		var forumWorkflow = forumBehaviours.workflow;
		for(var prop in forumWorkflow){
			if(model.me.hasWorkflow(forumWorkflow[prop])){
				workflow[prop] = true;
			}
		}

		return workflow;
	},
	resourceRights: function(){
		return ['read', 'contrib', 'publish', 'manager']
	},
	loadResources: function(callback) {
		http().get('/forum/categories').done(function(categories) {
			this.resources = categories;
			callback(this.resources);
		}.bind(this));
	},

    sniplets : {
        forum : {
            title : 'Forum',
            description : 'Catégorie de forum dédiée',
            controller : {
            	initSource : function() {
                    Behaviours.applicationsBehaviours.forum.loadResources(function(resources){
                        this.categories = resources;
                        this.$apply('categories');
                    }.bind(this));
                },

                init : function() {
                	var scope = this;
                	scope.display = { category: true };
                	var category = new forumNamespace.Category({ _id: this.source._id });
                    category.sync(function() {
                        scope.category = category;
                        scope.subjects = category.subjects;
                        /*DEBUG*/console.log(scope.subjects);
                        scope.$apply('subjects');    
                    });
                },

                openSubject : function(subject) {
                	var scope = this;
                	scope.subject = subject;
                	scope.display = { subject: true };
                	subject.sync(function(){
                		scope.messages = subject.messages;
                		/*DEBUG*/console.log(scope.messages);
                		scope.$apply('messages');
                	});
                },

                backToSubjects : function() {
                	scope.display = { category: true };
                },

                formatDate : function(date){
					return moment(date).format('DD MMMM YYYY HH[h]mm');
				},

				formatDateShort : function(date){
					return moment(date).format('DD/MM/YYYY HH[h]mm');
				},

				autoCreateCategory : function() {
					// TODO
				},

				searchCategory : function(element) {
					return lang.removeAccents((element.name || '').toLowerCase()).indexOf(lang.removeAccents((this.searchTest || '').toLowerCase())) !== -1;
				},

                getReferencedResources: function(source){
					if(source._id){
						return [source._id];
					}
				}
            }
        }
    }
});