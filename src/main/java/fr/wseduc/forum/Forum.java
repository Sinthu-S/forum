package fr.wseduc.forum;

import fr.wseduc.forum.controllers.ActionFilter;
import fr.wseduc.forum.controllers.ForumController;
import fr.wseduc.webutils.Server;
import fr.wseduc.webutils.request.filter.SecurityHandler;

public class Forum extends Server {

	@Override
	public void start() {
		super.start();

		ForumController controller = new ForumController(vertx, container, rm, securedActions);
		controller.get("", "view");

		SecurityHandler.addFilter(
				new ActionFilter(controller.securedUriBinding(), container.config(), vertx)
		);
	}

}
