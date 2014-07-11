package fr.wseduc.forum.services;

import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface CategoryService {

	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler);
	
	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler);
}
