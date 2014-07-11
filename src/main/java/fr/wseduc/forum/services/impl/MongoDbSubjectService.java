package fr.wseduc.forum.services.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.webutils.Either;

public class MongoDbSubjectService extends AbstractService implements SubjectService {

	public MongoDbSubjectService(final String collection) {
		super(collection);
	}

	@Override
	public void list(String categoryId, UserInfos user,
			Handler<Either<String, JsonArray>> handler) {
		// TODO IMPLEMENT : MongoDbSubjectService.list
		
	}

	@Override
	public void create(String categoryId, JsonObject body, UserInfos user,
			Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbSubjectService.create
		
	}

	@Override
	public void retrieve(String categoryId, String subjectId, UserInfos user,
			Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbSubjectService.retrieve
		
	}

	@Override
	public void update(String categoryId, String subjectId, JsonObject body,
			UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbSubjectService.update
		
	}

	@Override
	public void delete(String categoryId, String subjectId, UserInfos user,
			Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT : MongoDbSubjectService.delete
		
	}
}
