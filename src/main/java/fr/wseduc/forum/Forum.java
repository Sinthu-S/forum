package fr.wseduc.forum;

import fr.wseduc.forum.controllers.ForumController;
import fr.wseduc.forum.filters.impl.ForumResourceFilter;
import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.forum.services.MessageService;
import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.forum.services.impl.MongoDbCategoryService;
import fr.wseduc.forum.services.impl.MongoDbMessageService;
import fr.wseduc.forum.services.impl.MongoDbSubjectService;

import org.entcore.common.http.BaseServer;

public class Forum extends BaseServer {

	private final String COLLECTION = "forum.categories";
	
	@Override
	public void start() {
		
		final CategoryService categoryService = new MongoDbCategoryService(COLLECTION);
		final SubjectService subjectService = new MongoDbSubjectService(COLLECTION);
		final MessageService messageService = new MongoDbMessageService(COLLECTION);
		
		setResourceProvider(new ForumResourceFilter(COLLECTION, messageService));
		
		super.start();
		addController(new ForumController(COLLECTION, categoryService, subjectService, messageService));
	}

}
