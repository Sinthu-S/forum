var forumBehaviours = {
	resources: {
		contrib: {
			right: 'fr-wseduc-forum-controllers-ForumController|createMessage'
		},
		publish: {
			right: 'fr-wseduc-forum-controllers-ForumController|createSubject'
		},
		manage: {
			right: 'fr-wseduc-forum-controllers-ForumController|updateCategory'
		},
		share: {
			right: 'fr-wseduc-forum-controllers-ForumController|shareCategory'
		}
	},
	workflow: {
		admin: 'fr.wseduc.forum.controllers.ForumController|adminView'
	}
};

Behaviours.register('forum', {
	behaviours: forumBehaviours,
	resource: function(resource){
		// debug
		console.log("== Behaviour: " + resource._id + " ==");
		// /debug
		var rightsContainer = resource;
		if(resource instanceof Subject && resource.category){
			// debug
			console.log("Behaviour on Subject");
			// /debug
			rightsContainer = resource.category;
		}
		if(resource instanceof Message && resource.subject && resource.subject.category){
			// debug
			console.log("Behaviour on Message");
			// /debug
			rightsContainer = resource.subject.category;
		}
		if(!resource.myRights){
			resource.myRights = {};
		}

		for(var behaviour in forumBehaviours.resources){
			// debug
			console.log(" - trying: " + behaviour);
			// /debug
			if(model.me.hasRight(rightsContainer, forumBehaviours.resources[behaviour]) || model.me.userId === resource.owner.userId){
				// debug
				console.log("   -> ok: " + (model.me.userId === resource.owner.userId ? "me" : "right"));
				// /debug
				if(resource.myRights[behaviour] !== undefined){
					resource.myRights[behaviour] = resource.myRights[behaviour] && forumBehaviours.resources[behaviour];
				}
				else{
					resource.myRights[behaviour] = forumBehaviours.resources[behaviour];
				}
			}
			else {
				// debug
				console.log("   -x ko");
				// /debug
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
	}
});