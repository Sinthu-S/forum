package fr.wseduc.forum.controllers;

import java.util.Map;

import fr.wseduc.forum.controllers.helpers.CategoryHelper;
import fr.wseduc.forum.controllers.helpers.MessageHelper;
import fr.wseduc.forum.controllers.helpers.SubjectHelper;
import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.forum.services.MessageService;
import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.forum.services.impl.MongoDbCategoryService;
import fr.wseduc.forum.services.impl.MongoDbMessageService;
import fr.wseduc.forum.services.impl.MongoDbSubjectService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.ResourceFilter;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

public class ForumController extends BaseController {

	private final CategoryService categoryService;
	private final SubjectService subjectService;
	private final MessageService messageService;
	
	private final CategoryHelper categoryHelper;
	private final SubjectHelper subjectHelper;
	private final MessageHelper messageHelper;
	
	public ForumController(final String collection, final CategoryService categoryService, final SubjectService subjectService, final MessageService messageService) {
		this.categoryService = categoryService;
		this.subjectService = subjectService;
		this.messageService = messageService;
		
		this.categoryHelper = new CategoryHelper(collection, categoryService);
		this.subjectHelper = new SubjectHelper(subjectService);
		this.messageHelper = new MessageHelper(messageService);
	}
	
	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		this.categoryHelper.init(vertx, container, rm, securedActions);
		this.subjectHelper.init(vertx, container, rm, securedActions);
		this.messageHelper.init(vertx, container, rm, securedActions);
		
		// Init the Services
		((MongoDbCategoryService) this.categoryService).init(vertx, container, rm, securedActions);
		((MongoDbSubjectService) this.subjectService).init(vertx, container, rm, securedActions);
		((MongoDbMessageService) this.messageService).init(vertx, container, rm, securedActions);
	}
	
	
	@Get("")
	@SecuredAction("forum.view")
	public void view(HttpServerRequest request) {
		renderView(request);
	}

	@Get("/admin")
	@SecuredAction("forum.view.admin")
	public void adminView(HttpServerRequest request) {
		renderView(request);
	}
	
	
	@Get("/categories")
	@SecuredAction("forum.view")
	public void listCategories(HttpServerRequest request) {
		categoryHelper.list(request);
	}
	
	@Post("/categories")
	@SecuredAction("forum.view.admin")
	public void createCategory(HttpServerRequest request) {
		categoryHelper.create(request);
	}
	
	@Get("/category/:id")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void getCategory(HttpServerRequest request) {
		categoryHelper.retrieve(request);
	}
	
	@Put("/category/:id")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void updateCategory(HttpServerRequest request) {
		categoryHelper.update(request);
	}
	
	@Delete("/category/:id")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void deleteCategory(HttpServerRequest request) {
		categoryHelper.delete(request);
	}
	
	
	@Get("/share/json/:id")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void shareCategory(final HttpServerRequest request) {
		categoryHelper.shareJson(request);
	}
	
	@Put("/share/json/:id")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void shareCategorySubmit(final HttpServerRequest request) {
		categoryHelper.shareSubmit(request);
	}
	
	@Put("/share/remove/:id")
	@ApiDoc("Remove Share by id.")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void shareShareCategory(final HttpServerRequest request) {
		categoryHelper.shareRemove(request);
	}
	
	
	@Get("/category/:id/subjects")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void listSubjects(HttpServerRequest request) {
		subjectHelper.list(request);
	}
	
	@Post("/category/:id/subjects")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void createSubject(HttpServerRequest request) {
		subjectHelper.create(request);
	}
	
	@Get("/category/:id/subject/:subjectid")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void getSubject(HttpServerRequest request) {
		subjectHelper.retrieve(request);
	}
	
	@Put("/category/:id/subject/:subjectid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void updateSubject(HttpServerRequest request) {
		subjectHelper.update(request);
	}
	
	@Delete("/category/:id/subject/:subjectid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void deleteSubject(HttpServerRequest request) {
		subjectHelper.delete(request);
	}
	
	
	@Get("/category/:id/subject/:subjectid/messages")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void listMessages(HttpServerRequest request) {
		messageHelper.list(request);
	}
	
	@Post("/category/:id/subject/:subjectid/messages")
	@SecuredAction(value = "category.contrib", type = ActionType.RESOURCE)
	public void createMessage(HttpServerRequest request) {
		messageHelper.create(request);
	}
	
	@Get("/category/:id/subject/:subjectid/message/:messageid")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void getMessage(HttpServerRequest request) {
		messageHelper.retrieve(request);
	}
	
	@Put("/category/:id/subject/:subjectid/message/:messageid")
	@ResourceFilter("updateMine")
	@SecuredAction(value = "category.contrib", type = ActionType.RESOURCE)
	public void updateMessage(HttpServerRequest request) {
		messageHelper.update(request);
		// TODO IMPLEMENT : custom filter for mine / shared
	}
	
	@Delete("/category/:id/subject/:subjectid/message/:messageid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void deleteMessage(HttpServerRequest request) {
		messageHelper.delete(request);
	}
}
