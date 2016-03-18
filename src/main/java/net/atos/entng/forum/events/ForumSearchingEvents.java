package net.atos.entng.forum.events;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Either.Right;
import fr.wseduc.webutils.I18n;
import net.atos.entng.forum.Forum;
import org.entcore.common.search.SearchingEvents;
import org.entcore.common.service.VisibilityFilter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

import static org.entcore.common.mongodb.MongoDbResult.validResults;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

public class ForumSearchingEvents implements SearchingEvents {

	private static final Logger log = LoggerFactory.getLogger(ForumSearchingEvents.class);
	private final MongoDb mongo;
	private static final I18n i18n = I18n.getInstance();

	public ForumSearchingEvents() {
		this.mongo = MongoDb.getInstance();
	}

	@Override
	public void searchResource(List<String> appFilters, String userId, JsonArray groupIds, final JsonArray searchWords, final Integer page, final Integer limit,
							   final JsonArray columnsHeader, final String locale, final Handler<Either<String, JsonArray>> handler) {
		if (appFilters.contains(ForumSearchingEvents.class.getSimpleName())) {

			final List<String> groupIdsLst = groupIds.toList();
			final List<DBObject> groups = new ArrayList<DBObject>();
			groups.add(QueryBuilder.start("userId").is(userId).get());
			for (String gpId: groupIdsLst) {
				groups.add(QueryBuilder.start("groupId").is(gpId).get());
			}

			final QueryBuilder rightsQuery = new QueryBuilder().or(
					QueryBuilder.start("visibility").is(VisibilityFilter.PUBLIC.name()).get(),
					QueryBuilder.start("visibility").is(VisibilityFilter.PROTECTED.name()).get(),
					QueryBuilder.start("owner.userId").is(userId).get(),
					QueryBuilder.start("shared").elemMatch(
							new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
					).get());

			JsonObject sort = new JsonObject().putNumber("modified", -1);
			final JsonObject projection = new JsonObject();
			projection.putNumber("name", 1);
			//search all category of user
			mongo.find(Forum.CATEGORY_COLLECTION, MongoQueryBuilder.build(rightsQuery), sort,
					projection, new Handler<Message<JsonObject>>() {
						@Override
						public void handle(Message<JsonObject> event) {
							final Either<String, JsonArray> ei = validResults(event);
							if (ei.isRight()) {
								final JsonArray categoryResult = ei.right().getValue();

								final Map<String, String> mapIdName = new HashMap<String, String>();
								for (int i=0;i<categoryResult.size();i++) {
									final JsonObject j = categoryResult.get(i);
									mapIdName.put(j.getString("_id"), j.getString("name"));
								}

								//search subject for the catagories found
								searchSubject(page, limit, searchWords.toList(), mapIdName, new Handler<Either<String, JsonArray>>() {
									@Override
									public void handle(Either<String, JsonArray> event) {
										if (event.isRight()) {
											if (log.isDebugEnabled()) {
												log.debug("[ForumSearchingEvents][searchResource] The resources searched by user are finded");
											}
											final JsonArray res = formatSearchResult(event.right().getValue(), columnsHeader, searchWords.toList(), mapIdName, locale);
											handler.handle(new Right<String, JsonArray>(res));
										} else {
											handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
										}
									}
								});
							} else {
								handler.handle(new Either.Left<String, JsonArray>(ei.left().getValue()));
							}
						}
					});
		} else {
			handler.handle(new Right<String, JsonArray>(new JsonArray()));
		}
	}

	private void searchSubject(int page, int limit, List<String> searchWords, final Map<String,String> mapIdName, Handler<Either<String, JsonArray>> handler) {
		final int skip = (0 == page) ? -1 : page * limit;

		final List<String> searchFields = new ArrayList<String>();
		//search on main title
		searchFields.add("title");
		//search on content of messages
		searchFields.add("content");

		final List<DBObject> listMainTitleField = new ArrayList<DBObject>();
		final List<DBObject> listContentMessagesField = new ArrayList<DBObject>();

		for (String field : searchFields) {
			final List<DBObject> elemsMatch = new ArrayList<DBObject>();
			for (String word : searchWords) {
				final DBObject dbObject = QueryBuilder.start(field).regex(Pattern.compile(".*" +
						word + ".*", Pattern.CASE_INSENSITIVE)).get();
				if ("title".equals(field)) {
					listMainTitleField.add(dbObject);
				} else {
					//content of messages
					listContentMessagesField.add(QueryBuilder.start("messages").elemMatch(dbObject).get());
				}
			}
		}

		final QueryBuilder worldsOrQuery = new QueryBuilder();
		worldsOrQuery.or(new QueryBuilder().and(listMainTitleField.toArray(new DBObject[listMainTitleField.size()])).get());
		worldsOrQuery.or(new QueryBuilder().and(listContentMessagesField.toArray(new DBObject[listContentMessagesField.size()])).get());

		final QueryBuilder categoryQuery = new QueryBuilder();
		categoryQuery.start("category").in(mapIdName.keySet());

		final QueryBuilder query = new QueryBuilder().and(worldsOrQuery.get(), categoryQuery.get());

		JsonObject sort = new JsonObject().putNumber("modified", -1);
		final JsonObject projection = new JsonObject();
		projection.putNumber("title", 1);
		projection.putNumber("messages", 1);
		projection.putNumber("category", 1);
		projection.putNumber("modified", 1);
		projection.putNumber("owner.userId", 1);
		projection.putNumber("owner.displayName", 1);

		mongo.find(Forum.SUBJECT_COLLECTION, MongoQueryBuilder.build(query), sort,
				projection, skip, limit, Integer.MAX_VALUE, validResultsHandler(handler));
	}

	private JsonArray formatSearchResult(final JsonArray results, final JsonArray columnsHeader, final List<String> words,
										 final Map<String,String> mapIdName, final String locale) {
		final List<String> aHeader = columnsHeader.toList();
		final JsonArray traity = new JsonArray();

		for (int i=0;i<results.size();i++) {
			final JsonObject j = results.get(i);
			final JsonObject jr = new JsonObject();
			if (j != null) {
				final String categoryId = j.getString("category");
				final Map<String, Object> map = formatDescription(j.getArray("messages", new JsonArray()),
						words, j.getObject("modified"), categoryId, j.getString("_id"), j.getString("title"), locale);
				jr.putString(aHeader.get(0),  mapIdName.get(categoryId));
				jr.putString(aHeader.get(1), map.get("description").toString());
				jr.putObject(aHeader.get(2), (JsonObject) map.get("modified"));
				jr.putString(aHeader.get(3), j.getObject("owner").getString("displayName"));
				jr.putString(aHeader.get(4), j.getObject("owner").getString("userId"));
				jr.putString(aHeader.get(5), "/forum#/view/" + categoryId);
				traity.add(jr);
			}
		}
		return traity;
	}

	private Map<String, Object> formatDescription(JsonArray ja, final List<String> words, JsonObject defaultDate,
												  String categoryId, String subjectId, String subjectTitle, String locale) {
		final Map<String, Object> map = new HashMap<String, Object>();

		Integer countMatchMessages = 0;
		String titleRes = "<a href=\"/forum#/view/" + categoryId + "/" + subjectId + "\">" + subjectTitle + "</a>";
		JsonObject modifiedRes = null;
		Date modifiedMarker = null;

		//get the last modified page that match with searched words for create the description
		for(int i=0;i<ja.size();i++) {
			final JsonObject jO = ja.get(i);
			final String content = jO.getString("content" ,"");

			final Date currentDate = MongoDb.parseIsoDate(jO.getObject("modified"));
			boolean match = false;
			for (final String word : words) {
				if (content.toLowerCase().contains(word.toLowerCase())) {
					match = true;
					break;
				}
			}
			if (countMatchMessages == 0 && match) {
				modifiedRes = jO.getObject("modified");
			} else if (countMatchMessages > 0 && modifiedMarker.before(currentDate)) {
				modifiedMarker = currentDate;
				modifiedRes = jO.getObject("modified");
			}
			if (match) {
				modifiedMarker = currentDate;
				countMatchMessages++;
			}
		}

		if (countMatchMessages == 0) {
			map.put("modified", defaultDate);
			map.put("description", i18n.translate("forum.search.description.none", locale, titleRes));
		} else if (countMatchMessages == 1) {
			map.put("modified", modifiedRes);
			map.put("description", i18n.translate("forum.search.description.one", locale, titleRes));
		} else {
			map.put("modified", modifiedRes);
			map.put("description", i18n.translate("forum.search.description.several", locale,
					titleRes, countMatchMessages.toString()));
		}

		return  map;
	}
}
