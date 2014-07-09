package fr.wseduc.forum.controllers;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.ResourceFilter;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;

import org.vertx.java.core.http.HttpServerRequest;

public class ForumController extends BaseController {

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
		// TODO IMPLEMENT listCategories
	}
	
	@Post("/categories")
	@SecuredAction("forum.view.admin")
	public void createCategory(HttpServerRequest request) {
		// TODO IMPLEMENT createCategory
	}
	
	@Get("/category/:id")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void getCategory(HttpServerRequest request) {
		// TODO IMPLEMENT getCategory
	}
	
	@Put("/category/:id")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void updateCategory(HttpServerRequest request) {
		// TODO IMPLEMENT updateCategory
	}
	
	@Delete("/category/:id")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void deleteCategory(HttpServerRequest request) {
		// TODO IMPLEMENT deleteCategory
	}
	
	
	@Get("/share/json/:id")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void shareCategory(final HttpServerRequest request) {
		// TODO IMPLEMENT shareCategory
	}
	
	@Put("/share/json/:id")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void shareCategorySubmit(final HttpServerRequest request) {
		// TODO IMPLEMENT shareCategorySubmit
	}
	
	@Put("/share/remove/:id")
	@ApiDoc("Remove Share by id.")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void shareShareCategory(final HttpServerRequest request) {
		// TODO IMPLEMENT shareShareCategory
	}
	
	
	@Get("/category/:id/subjects")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void listSubjects(HttpServerRequest request) {
		// TODO IMPLEMENT listSubjects
	}
	
	@Post("/category/:id/subjects")
	@SecuredAction(value = "category.manager", type = ActionType.RESOURCE)
	public void createSubject(HttpServerRequest request) {
		// TODO IMPLEMENT createSubject
	}
	
	@Get("/category/:id/subject/:subjectid")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void getSubject(HttpServerRequest request) {
		// TODO IMPLEMENT getSubject
	}
	
	@Put("/category/:id/subject/:subjectid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void updateSubject(HttpServerRequest request) {
		// TODO IMPLEMENT updateSubject
	}
	
	@Delete("/category/:id/subject/:subjectid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void deleteSubject(HttpServerRequest request) {
		// TODO IMPLEMENT deleteSubject
	}
	
	
	@Get("/category/:id/subject/:subjectid/messages")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void listMessages(HttpServerRequest request) {
		// TODO IMPLEMENT listMessages
	}
	
	@Post("/category/:id/subject/:subjectid/messages")
	@SecuredAction(value = "category.contrib", type = ActionType.RESOURCE)
	public void createMessage(HttpServerRequest request) {
		// TODO IMPLEMENT createMessage
	}
	
	@Get("/category/:id/subject/:subjectid/message/:messageid")
	@SecuredAction(value = "category.read", type = ActionType.RESOURCE)
	public void getMessage(HttpServerRequest request) {
		// TODO IMPLEMENT getMessage
	}
	
	@Put("/category/:id/subject/:subjectid/message/:messageid")
	@ResourceFilter("updateMine")
	@SecuredAction(value = "category.contrib", type = ActionType.RESOURCE)
	public void updateMessage(HttpServerRequest request) {
		// TODO IMPLEMENT updateMessage
		// TODO IMPLEMENT custom filter for mine / shared
	}
	
	@Delete("/category/:id/subject/:subjectid/message/:messageid")
	@SecuredAction(value = "category.publish", type = ActionType.RESOURCE)
	public void deleteMessage(HttpServerRequest request) {
		// TODO IMPLEMENT deleteMessage
	}
}
