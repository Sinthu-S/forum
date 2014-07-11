package fr.wseduc.forum.services.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.forum.services.MessageService;
import fr.wseduc.webutils.Either;

public class MongoDbMessageService extends AbstractService implements MessageService {

	public MongoDbMessageService(final String collection) {
		super(collection);
	}

	@Override
	public void list(String categoryId, String subjectId, UserInfos user,
			Handler<Either<String, JsonArray>> handler) {
		// TODO IMPLEMENT : MongoDbMessageService.list
		
	}

	@Override
	public void create(String categoryId, String subjectId, JsonObject body,
			UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbMessageService.create
		
	}

	@Override
	public void retrieve(String categoryId, String subjectId, String messageId,
			UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbMessageService.retrieve
		
	}

	@Override
	public void update(String categoryId, String subjectId, String messageId,
			JsonObject body, UserInfos user,
			Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbMessageService.update
		
	}

	@Override
	public void delete(String categoryId, String subjectId, String messageId,
			UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbMessageService.delete
		
	}
}
