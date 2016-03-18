package net.atos.entng.forum.events;

import static net.atos.entng.forum.Forum.CATEGORY_COLLECTION;
import static net.atos.entng.forum.Forum.SUBJECT_COLLECTION;
import static net.atos.entng.forum.Forum.MANAGE_RIGHT_ACTION;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;

import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.RepositoryEvents;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class ForumRepositoryEvents implements RepositoryEvents {

	private static final Logger log = LoggerFactory.getLogger(ForumRepositoryEvents.class);
	private final MongoDb mongo = MongoDb.getInstance();

	@Override
	public void exportResources(String exportId, String userId, JsonArray groups, String exportPath, String locale, String host, final Handler<Boolean> handler) {
		// TODO Implement exportResources
		log.warn("[ForumRepositoryEvents] exportResources is not implemented");
	}

	@Override
	public void deleteGroups(JsonArray groups) {
		if(groups == null || groups.size() == 0) {
			log.warn("[ForumRepositoryEvents][deleteGroups] JsonArray groups is null or empty");
			return;
		}

		final String [] groupIds = new String[groups.size()];
		for (int i = 0; i < groups.size(); i++) {
			JsonObject j = groups.get(i);
			groupIds[i] = j.getString("group");
		}

		final JsonObject matcher = MongoQueryBuilder.build(QueryBuilder.start("shared.groupId").in(groupIds));

		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.pull("shared", MongoQueryBuilder.build(QueryBuilder.start("groupId").in(groupIds)));
		// remove all the shares with groups
		mongo.update(CATEGORY_COLLECTION, matcher, modifier.build(), false, true, MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					log.info("[ForumRepositoryEvents][deleteGroups] All groups shares are removed");
				} else {
					log.error("[ForumRepositoryEvents][deleteGroups] Error removing groups shares. Message : " + event.left().getValue());
				}
			}
		}));
	}

	@Override
	public void deleteUsers(JsonArray users) {
		// TODO : make the user anonymous
		if(users == null || users.size() == 0) {
			log.warn("[ForumRepositoryEvents][deleteUsers] JsonArray users is null or empty");
			return;
		}

		final String [] usersIds = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			JsonObject j = users.get(i);
			usersIds[i] = j.getString("id");
		}
		/*	Clean the database :
		 	- First, remove shares of all the categories shared with (usersIds)
			- then, get the categories identifiers that have no user and no manger,
			- delete all these categories,
			- delete all the subjects that do not belong to a category
			- finally, tag all users as deleted in their own categories
		*/
		ForumRepositoryEvents.this.removeSharesCategories(usersIds);
	}

	/**
	 * Remove the shares of categories with a list of users
	 * if OK, Call prepareCleanCategories()
	 * @param usersIds users identifiers
	 */
	private void removeSharesCategories(final String [] usersIds){
		final JsonObject criteria = MongoQueryBuilder.build(QueryBuilder.start("shared.userId").in(usersIds));
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.pull("shared", MongoQueryBuilder.build(QueryBuilder.start("userId").in(usersIds)));

		// Remove Categories shares with these users
		mongo.update(CATEGORY_COLLECTION, criteria, modifier.build(), false, true, MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					log.info("[ForumRepositoryEvents][removeSharesCategories] All categories shares with users are removed");
					ForumRepositoryEvents.this.prepareCleanCategories(usersIds);
				} else {
					log.error("[ForumRepositoryEvents][removeSharesCategories] Error removing categories shares with users. Message : " + event.left().getValue());
				}
			}
		}));
	}

	/**
	 * Prepare a list of categories identifiers
	 * if OK, Call cleanCategories()
	 * @param usersIds users identifiers
	 */
	private void prepareCleanCategories(final String [] usersIds) {
		DBObject deletedUsers = new BasicDBObject();
		// users currently deleted
		deletedUsers.put("owner.userId", new BasicDBObject("$in", usersIds));
		// users who have already been deleted
		DBObject ownerIsDeleted = new BasicDBObject("owner.deleted", true);
		// no manager found
		JsonObject matcher = MongoQueryBuilder.build(QueryBuilder.start("shared." + MANAGE_RIGHT_ACTION).notEquals(true).or(deletedUsers, ownerIsDeleted));
		// return only categories identifiers
		JsonObject projection = new JsonObject().putNumber("_id", 1);

		mongo.find(CATEGORY_COLLECTION, matcher, null, projection, MongoDbResult.validResultsHandler(new Handler<Either<String,JsonArray>>() {
			@Override
			public void handle(Either<String, JsonArray> event) {
				if (event.isRight()) {
					JsonArray categories = event.right().getValue();
					if(categories == null || categories.size() == 0) {
						log.info("[ForumRepositoryEvents][prepareCleanCategories] No categorie to delete");
						return;
					}
					final String[] categoriesIds = new String[categories.size()];
					for (int i = 0; i < categories.size(); i++) {
						JsonObject j = categories.get(i);
						categoriesIds[i] = j.getString("_id");
					}
					ForumRepositoryEvents.this.cleanCategories(usersIds, categoriesIds);
				} else {
					log.error("[ForumRepositoryEvents][prepareCleanCategories] Error retreving the categories created by users. Message : " + event.left().getValue());
				}
			}
		}));
	}

	/**
	 * Delete categories by identifier
	 * if OK, call cleanSubjects() and tagUsersAsDeleted()
	 * @param usersIds users identifiers, used for tagUsersAsDeleted()
	 * @param categoriesIds categories identifiers
	 */
	private void cleanCategories(final String [] usersIds, final String [] categoriesIds) {
		JsonObject matcher = MongoQueryBuilder.build(QueryBuilder.start("_id").in(categoriesIds));

		mongo.delete(CATEGORY_COLLECTION, matcher, MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					log.info("[ForumRepositoryEvents][cleanCategories] The categories created by users are deleted");
					ForumRepositoryEvents.this.cleanSubjects(categoriesIds);
					ForumRepositoryEvents.this.tagUsersAsDeleted(usersIds);
				} else {
					log.error("[ForumRepositoryEvents][cleanCategories] Error deleting the categories created by users. Message : " + event.left().getValue());
				}
			}
		}));
	}

	/**
	 * Delete subjects by category identifier
	 * @param categoriesIds categories identifiers
	 */
	private void cleanSubjects(final String [] categoriesIds) {
		JsonObject matcher = MongoQueryBuilder.build(QueryBuilder.start("category").in(categoriesIds));

		mongo.delete(SUBJECT_COLLECTION, matcher, MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					log.info("[ForumRepositoryEvents][cleanSubjects] The subjects created by users are deleted");
				} else {
					log.error("[ForumRepositoryEvents][cleanSubjects] Error deleting the subjects created by users. Message : " + event.left().getValue());
				}
			}
		}));
	}

	/**
	 * Tag as deleted a list of users in their own categories
	 * @param userIds users identifiers
	 */
	private void tagUsersAsDeleted(final String[] usersIds) {
		final JsonObject criteria = MongoQueryBuilder.build(QueryBuilder.start("owner.userId").in(usersIds));
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.set("owner.deleted", true);

		mongo.update(CATEGORY_COLLECTION, criteria, modifier.build(), false, true, MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					log.info("[ForumRepositoryEvents][deleteCategoriesUser] users are tagged as deleted in their own categories");
				} else {
					log.error("[ForumRepositoryEvents][deleteCategoriesUser] Error tagging as deleted users. Message : " + event.left().getValue());
				}
			}
		}));
	}

}
