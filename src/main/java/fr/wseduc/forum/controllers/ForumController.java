package fr.wseduc.forum.controllers;

import fr.wseduc.rs.Get;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;
import org.vertx.java.core.http.HttpServerRequest;

public class ForumController extends BaseController {

	@Get("")
	@SecuredAction("forum.view")
	public void view(HttpServerRequest request) {
		renderView(request);
	}

}
