package fr.wseduc.forum.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import java.util.ArrayList;
import java.util.List;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

public class MongoDbCategoryService extends AbstractService implements CategoryService {

	public MongoDbCategoryService(final String categories_collection, final String subjects_collection) {
		super(categories_collection, subjects_collection);
	}

	@Override
	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler) {
		// Start
		QueryBuilder query = QueryBuilder.start();

		// Permissions Filter
		List<DBObject> groups = new ArrayList<>();
		groups.add(QueryBuilder.start("userId").is(user.getUserId()).get());
		for (String gpId: user.getProfilGroupsIds()) {
			groups.add(QueryBuilder.start("groupId").is(gpId).get());
		}
		query.or(
			QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
			QueryBuilder.start("shared").elemMatch(
					new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
			).get());

		JsonObject sort = new JsonObject().putNumber("modified", -1);
		mongo.find(categories_collection, MongoQueryBuilder.build(query), sort, null, validResultsHandler(handler));
	}

	@Override
	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder builder = QueryBuilder.start("_id").is(id);
		mongo.findOne(categories_collection,  MongoQueryBuilder.build(builder), null, validResultHandler(handler));
	}

	@Override
	public void delete(String id, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Delete the category
		QueryBuilder builder = QueryBuilder.start("_id").is(id);
		mongo.delete(categories_collection,  MongoQueryBuilder.build(builder), validResultHandler(handler));
	}

	@Override
	public void deleteSubjects(String id, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Delete all subjects of the category
		QueryBuilder query = QueryBuilder.start("category").is(id);
		mongo.delete(subjects_collection, MongoQueryBuilder.build(query), validResultHandler(handler));
	}

	@Override
	public void getSharedWithIds(String categoryId, UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		this.retrieve(categoryId, user, new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				JsonArray sharedWithIds = new JsonArray();
				if (event.isRight()) {
					try {
						JsonObject category = event.right().getValue();
						if (category.containsField("shared")) {
							sharedWithIds = category.getArray("shared");
							handler.handle(new Either.Right<String, JsonArray>(sharedWithIds));
						}
						else {
							handler.handle(new Either.Right<String, JsonArray>(new JsonArray()));
						}
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonArray>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
				}
			}
		});
	}
}
