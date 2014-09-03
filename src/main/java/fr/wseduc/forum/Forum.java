package fr.wseduc.forum;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;

import fr.wseduc.forum.controllers.ForumController;
import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.forum.services.MessageService;
import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.forum.services.impl.MongoDbCategoryService;
import fr.wseduc.forum.services.impl.MongoDbMessageService;
import fr.wseduc.forum.services.impl.MongoDbSubjectService;

public class Forum extends BaseServer {

	public static final String CATEGORY_COLLECTION = "forum.categories";
	public static final String SUBJECT_COLLECTION = "forum.subjects";

	@Override
	public void start() {

		final MongoDbConf conf = MongoDbConf.getInstance();
		conf.setCollection(CATEGORY_COLLECTION);
		conf.setResourceIdLabel("id");

		final CategoryService categoryService = new MongoDbCategoryService(CATEGORY_COLLECTION, SUBJECT_COLLECTION);
		final SubjectService subjectService = new MongoDbSubjectService(CATEGORY_COLLECTION, SUBJECT_COLLECTION);
		final MessageService messageService = new MongoDbMessageService(CATEGORY_COLLECTION, SUBJECT_COLLECTION);

		// setResourceProvider(new ForumResourceFilter(CATEGORY_COLLECTION, messageService));

		super.start();
		setDefaultResourceFilter(new ShareAndOwner());
		addController(new ForumController(CATEGORY_COLLECTION, categoryService, subjectService, messageService));
	}

}
