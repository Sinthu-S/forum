package fr.wseduc.forum.services;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface SubjectService {

	public void list(String categoryId, UserInfos user, Handler<Either<String, JsonArray>> handler);

	public void create(String categoryId, JsonObject body, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void retrieve(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void update(String categoryId, String subjectId, JsonObject body, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void delete(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void getSubjectTitle(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler);
}
