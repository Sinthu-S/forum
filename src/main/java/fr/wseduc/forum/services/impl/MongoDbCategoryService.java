package fr.wseduc.forum.services.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.webutils.Either;

public class MongoDbCategoryService extends AbstractService implements CategoryService {

	public MongoDbCategoryService(final String collection) {
		super(collection);
	}

	@Override
	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler) {
		// TODO IMPLEMENT : MongoDbCategoryService.list
		
	}

	@Override
	public void retrieve(String id, UserInfos user,
			Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbCategoryService.retrieve
		
	}
}
