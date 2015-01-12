package net.atos.entng.forum;

import net.atos.entng.forum.controllers.ForumController;
import net.atos.entng.forum.services.CategoryService;
import net.atos.entng.forum.services.MessageService;
import net.atos.entng.forum.services.SubjectService;
import net.atos.entng.forum.services.impl.MongoDbCategoryService;
import net.atos.entng.forum.services.impl.MongoDbMessageService;
import net.atos.entng.forum.services.impl.MongoDbSubjectService;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;


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