package fr.wseduc.forum.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.forum.services.MessageService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;

public class MongoDbMessageService extends AbstractService implements MessageService {

	public MongoDbMessageService(final String categories_collection, final String subjects_collection) {
		super(categories_collection, subjects_collection);
	}

	@Override
	public void list(final String categoryId, final String subjectId, final UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId)
				.put("category").is(categoryId);
		
		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("messages", 1);
		
		mongo.findOne(subjects_collection, MongoQueryBuilder.build(query), projection, validResultHandler(new Handler<Either<String, JsonObject>>(){
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					try {
						// Extract messages
						JsonObject subject = event.right().getValue();
						JsonArray messages = subject.getArray("messages");
						handler.handle(new Either.Right<String, JsonArray>(messages));
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonArray>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
				}
			}
		}));
	}

	@Override
	public void create(final String categoryId, final String subjectId, final JsonObject body, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		// Prepare Message object
		ObjectId newId = new ObjectId();
		JsonObject now = MongoDb.now();
		body.putString("_id", newId.toStringMongod())
			.putObject("owner", new JsonObject()
				.putString("userId", user.getUserId())
				.putString("displayName", user.getUsername()))
			.putObject("created", now).putObject("modified", now);
		
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId);
		query.put("category").is(categoryId);
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.push("messages", body);
		
		// Execute query
		mongo.update(subjects_collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void retrieve(final String categoryId, final String subjectId, final String messageId, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId)
				.put("category").is(categoryId)
				.put("messages").elemMatch(new BasicDBObject("_id", messageId));
		
		// Projection
		JsonObject idMatch = new JsonObject();
		idMatch.putString("_id", messageId);
		JsonObject elemMatch = new JsonObject();
		elemMatch.putObject("$elemMatch", idMatch);
		JsonObject projection = new JsonObject();
		projection.putObject("messages", elemMatch);
		
		mongo.findOne(subjects_collection, MongoQueryBuilder.build(query), projection, validResultHandler(new Handler<Either<String, JsonObject>>(){
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					try {
						// Extract message
						JsonObject subject = event.right().getValue();
						JsonArray messages = subject.getArray("messages");
						JsonObject extractedMessage = messages.get(0);
						handler.handle(new Either.Right<String, JsonObject>(extractedMessage));
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonObject>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(event);
				}
			}
		}));
	}

	@Override
	public void update(final String categoryId, final String subjectId, final String messageId, final JsonObject body, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId)
				.put("category").is(categoryId)
				.put("messages").elemMatch(new BasicDBObject("_id", messageId));
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Message object update
		body.removeField("_id");
		body.removeField("owner");
		
		for (String attr: body.getFieldNames()) {
			modifier.set("messages.$." + attr, body.getValue(attr));
		}
		modifier.set("messages.$.modified", MongoDb.now());
		
		// Prepare Subject update
		modifier.set("modified", MongoDb.now());
		
		// Execute query
		mongo.update(subjects_collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void delete(final String categoryId, final String subjectId, final String messageId, final UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(subjectId)
				.put("category").is(categoryId)
				.put("messages").elemMatch(new BasicDBObject("_id", messageId));
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Info delete
		JsonObject messageMatcher = new JsonObject();
		modifier.pull("messages", messageMatcher.putString("_id", messageId));
		
		// Execute query
		mongo.update(subjects_collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}
	

	@Override
	public void checkIsMine(final String categoryId, final String subjectId, final String messageId, final UserInfos user, final String sharedMethod, final Handler<Boolean> handler) {
		// Prepare Category Query
		final QueryBuilder categoryQuery = QueryBuilder.start();
		prepareIsSharedQuery(categoryQuery, user, categoryId, sharedMethod);
		
		// Check Category Sharing
		executeCountQuery(categories_collection, MongoQueryBuilder.build(categoryQuery), 1, new Handler<Boolean>() {
			@Override
			public void handle(Boolean event) {
				if (event) {
					
					// Prepare Subject and Message query
					QueryBuilder query = QueryBuilder.start("_id").is(subjectId)
							.put("category").is(categoryId);
					
					DBObject messageMatch = new BasicDBObject();
					messageMatch.put("_id", messageId);
					messageMatch.put("owner.userId", user.getUserId());
					query.put("infos").elemMatch(messageMatch);
					
					// Check Subject and Message
					executeCountQuery(subjects_collection, MongoQueryBuilder.build(query), 1, handler);
				}
				else {
					handler.handle(false);
				}
			}
		});
	}
	
	protected void prepareIsSharedQuery(final QueryBuilder query, final UserInfos user, final String threadId, final String sharedMethod) {
		// ThreadId
		query.put("_id").is(threadId);
		
		// Permissions
		List<DBObject> groups = new ArrayList<>();
		groups.add(QueryBuilder.start("userId").is(user.getUserId())
				.put(sharedMethod).is(true).get());
		for (String gpId: user.getProfilGroupsIds()) {
			groups.add(QueryBuilder.start("groupId").is(gpId)
					.put(sharedMethod).is(true).get());
		}
		query.or(
				QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
				QueryBuilder.start("visibility").is(VisibilityFilter.PUBLIC.name()).get(),
				QueryBuilder.start("visibility").is(VisibilityFilter.PROTECTED.name()).get(),
				QueryBuilder.start("shared").elemMatch(
						new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()).get()
		);
	}
	
	protected void executeCountQuery(final String collection, final JsonObject query, final int expectedCountResult, final Handler<Boolean> handler) {
		mongo.count(collection, query, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				JsonObject res = event.body();
				handler.handle(
						res != null &&
						"ok".equals(res.getString("status")) &&
						expectedCountResult == res.getInteger("count")
				);
			}
		});
	}
}
