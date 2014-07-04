package fr.wseduc.forum;

import fr.wseduc.forum.controllers.ForumController;
import org.entcore.common.http.BaseServer;

public class Forum extends BaseServer {

	@Override
	public void start() {
		super.start();
		addController(new ForumController());
	}

}
