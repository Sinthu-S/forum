package fr.wseduc.forum.controllers;

import java.util.Map;

import org.entcore.common.http.filter.ResourceFilter;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

import fr.wseduc.forum.controllers.helpers.CategoryHelper;
import fr.wseduc.forum.controllers.helpers.MessageHelper;
import fr.wseduc.forum.controllers.helpers.SubjectHelper;
import fr.wseduc.forum.filters.impl.ForumMessageMine;
import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.forum.services.MessageService;
import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;

public class ForumController extends BaseController {

	private final CategoryHelper categoryHelper;
	private final SubjectHelper subjectHelper;
	private final MessageHelper messageHelper;

	public ForumController(final String collection, final CategoryService categoryService, final SubjectService subjectService, final MessageService messageService) {

		this.categoryHelper = new CategoryHelper(collection, categoryService);
		this.subjectHelper = new SubjectHelper(subjectService, categoryService);
		this.messageHelper = new MessageHelper(messageService, subjectService);
	}

	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		this.categoryHelper.init(vertx, container, rm, securedActions);
		this.subjectHelper.init(vertx, container, rm, securedActions);
		this.messageHelper.init(vertx, container, rm, securedActions);
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
		categoryHelper.share(request);
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
	public void removeShareCategory(final HttpServerRequest request) {
		categoryHelper.shareRemove(request);
	}


	@Get("/category/:id/subjects")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void listSubjects(HttpServerRequest request) {
		subjectHelper.list(request);
	}

	@Post("/category/:id/subjects")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
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
	@ResourceFilter(ForumMessageMine.class)
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void updateMessage(HttpServerRequest request) {
		messageHelper.update(request);
	}

	@Delete("/category/:id/subject/:subjectid/message/:messageid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void deleteMessage(HttpServerRequest request) {
		messageHelper.delete(request);
	}
}
