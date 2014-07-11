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

	private final String CATEGORY_COLLECTION = "forum.categories";
	private final String SUBJECT_COLLECTION = "forum.subjects";
	
	@Override
	public void start() {
		
		final CategoryService categoryService = new MongoDbCategoryService(CATEGORY_COLLECTION);
		final SubjectService subjectService = new MongoDbSubjectService(CATEGORY_COLLECTION, SUBJECT_COLLECTION);
		final MessageService messageService = new MongoDbMessageService(CATEGORY_COLLECTION, SUBJECT_COLLECTION);
		
		setResourceProvider(new ForumResourceFilter(CATEGORY_COLLECTION, messageService));
		
		super.start();
		addController(new ForumController(CATEGORY_COLLECTION, categoryService, subjectService, messageService));
	}

}
