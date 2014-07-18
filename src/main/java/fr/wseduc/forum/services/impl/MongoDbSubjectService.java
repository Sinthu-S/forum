package fr.wseduc.forum.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.QueryBuilder;

import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;

public class MongoDbSubjectService extends AbstractService implements SubjectService {

	public MongoDbSubjectService(final String categories_collection, final String subjects_collection) {
		super(categories_collection, subjects_collection);
	}

	@Override
	public void list(String categoryId, UserInfos user, Handler<Either<String, JsonArray>> handler) {
		QueryBuilder query = QueryBuilder.start("category").is(categoryId);
		JsonObject sort = new JsonObject().putNumber("modified", -1);
		mongo.find(subjects_collection, MongoQueryBuilder.build(query), sort, null, validResultsHandler(handler));
	}

	@Override
	public void create(String categoryId, JsonObject body, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		
		JsonObject now = MongoDb.now();
		body.putObject("owner", new JsonObject()
				.putString("userId", user.getUserId())
				.putString("displayName", user.getUsername())
		).putObject("created", now).putObject("modified", now)
		.putString("category", categoryId);
		
		mongo.save(subjects_collection, body, validActionResultHandler(handler));
		
	}

	@Override
	public void retrieve(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);
		mongo.findOne(subjects_collection,  MongoQueryBuilder.build(query), validResultHandler(handler));
	}

	@Override
	public void update(String categoryId, String subjectId, JsonObject body, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);
		
		// Clean data
		body.removeField("_id");
		body.removeField("category");
		body.removeField("messages");
		
		// Modifier
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		for (String attr: body.getFieldNames()) {
			modifier.set(attr, body.getValue(attr));
		}
		modifier.set("modified", MongoDb.now());
		mongo.update(subjects_collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void delete(String categoryId, String subjectId, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);
		mongo.delete(subjects_collection, MongoQueryBuilder.build(query), validActionResultHandler(handler));
	}
}
