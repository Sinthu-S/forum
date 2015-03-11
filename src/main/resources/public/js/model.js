model.build = function(){
	this.makeModels([
		Behaviours.applicationsBehaviours.forum.namespace.Category,
		Behaviours.applicationsBehaviours.forum.namespace.Subject,
		Behaviours.applicationsBehaviours.forum.namespace.Message
	]);

	// Category prototype
	Behaviours.applicationsBehaviours.forum.namespace.Category.prototype.open = function(cb){
		this.subjects.one('sync', function(){
			if(typeof cb === 'function'){
				cb();
			}
		}.bind(this));
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
		}
	};

	// Build
	this.collection(Behaviours.applicationsBehaviours.forum.namespace.Category, {
		sync: function(callback){
			http().get('/forum/categories').done(function(categories){
				this.load(categories);
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