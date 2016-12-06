model.build = function () {
	window.forumModel = Behaviours.applicationsBehaviours.forum.namespace;

	this.makeModels([
		forumModel.Category,
		forumModel.Subject,
		forumModel.Message
	]);

	window.ForumExtensions.extendEditor();

	// Category prototype
	Behaviours.applicationsBehaviours.forum.namespace.Category.prototype.open = function(cb){
		this.subjects.one('sync', function(){
			if(typeof cb === 'function'){
				cb();
			}
		}.bind(this));
		console.log('open');
		this.subjects.sync();
	};

	Behaviours.applicationsBehaviours.forum.namespace.Category.prototype.saveModifications = function(callback){
		http().putJson('/forum/category/' + this._id, this).done(function(e){
			notify.info('forum.subject.modification.saved');
			if(typeof callback === 'function'){
				callback();
			}
		});
	};

	Behaviours.applicationsBehaviours.forum.namespace.Category.prototype.save = function(callback){
		if(this._id){
			this.saveModifications(callback);
		}
		else{
			this.createCategory(function(){
				model.categories.sync();
				if (typeof callback === 'function') {
					callback();
				}
			});
		}
	};

	Behaviours.applicationsBehaviours.forum.namespace.Category.prototype.toJSON = function(){
		return {
			name: this.name,
			icon: this.icon
		};
	};

	// Build
	this.collection(Behaviours.applicationsBehaviours.forum.namespace.Category, {
		sync: function(callback){
			http().get('/forum/categories/all').done(function(results){
				this.load(results[0]);
				var subjects = results[1];
				var categories = this.all;
				for (var i = categories.length - 1; i >= 0; i--) {
					for (var j = 0; j < subjects.length; j++) {
						if(categories[i]._id == subjects[j].category){
							subjects[j].category = categories[i];
							if (subjects[j].messages instanceof Array) {
								subjects[j].lastMessage = subjects[j].messages[subjects[j].messages.length-1];
							}
							categories[i].subjects.push(subjects[j]);
						}
					}
				}

				if(typeof callback === 'function'){
					callback();
				}
			}.bind(this));
		},
		removeSelection: function(callback){
			var counter = this.selection().length;
			this.selection().forEach(function(item){
				http().delete('/forum/category/' + item._id).done(function(){
					counter = counter - 1;
					if (counter === 0) {
						model.categories.sync();
						if(typeof callback === 'function'){
							callback();
						}
					}
				});
			});
		},
		behaviours: 'forum'
	})
};