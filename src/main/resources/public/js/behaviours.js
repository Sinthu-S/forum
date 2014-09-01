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
		var rightsContainer = resource;
		if(resource instanceof Subject && resource.category){
			rightsContainer = resource.category;
		}
		if(resource instanceof Message && resource.subject && resource.subject.category){
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
	}
});