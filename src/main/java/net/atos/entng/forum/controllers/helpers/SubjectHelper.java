package net.atos.entng.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.atos.entng.forum.services.CategoryService;
import net.atos.entng.forum.services.SubjectService;

import org.entcore.common.notification.TimelineHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.security.SecuredAction;

public class SubjectHelper extends ExtractorHelper {

	private final SubjectService subjectService;
	private final CategoryService categoryService;

	private static final String CATEGORY_ID_PARAMETER = "id";
	private static final String SUBJECT_ID_PARAMETER = "subjectid";
	private static final String FORUM_NAME = "FORUM";
	private static final String NEW_SUBJECT_EVENT_TYPE = FORUM_NAME + "_NEW_SUBJECT";
	private static final String UPDATE_SUBJECT_EVENT_TYPE = FORUM_NAME + "_UPDATE_SUBJECT";

	protected TimelineHelper notification;

	public SubjectHelper(final SubjectService subjectService, final CategoryService categoryService) {
		this.subjectService = subjectService;
		this.categoryService = categoryService;
	}

	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		this.notification = new TimelineHelper(vertx, eb, container);
	}

	public void list(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		if (categoryId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					subjectService.list(categoryId, user, arrayResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}

	public void retrieve(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					subjectService.retrieve(categoryId, subjectId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}

	public void create(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		if (categoryId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				extractBodyFromRequest(request, new Handler<JsonObject>(){
					@Override
					public void handle(final JsonObject body) {
						try {
							Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
								@Override
								public void handle(Either<String, JsonObject> event) {
									if (event.isRight()) {
										if (event.right().getValue() != null && event.right().getValue().size() > 0) {
											notifyTimeline(request, user, body, event.right().getValue().getString("_id"), NEW_SUBJECT_EVENT_TYPE);
											renderJson(request, event.right().getValue(), 200);
										}
									} else {
										JsonObject error = new JsonObject().putString("error", event.left().getValue());
										renderJson(request, error, 400);
									}
								}
							};
							subjectService.create(categoryId, body, user, handler);
						}
						catch (Exception e) {
							renderErrorException(request, e);
						}
					}
				});

			}
		});
	}

	public void update(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				extractBodyFromRequest(request, new Handler<JsonObject>(){
					@Override
					public void handle(final JsonObject body) {
						try {
							Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
								@Override
								public void handle(Either<String, JsonObject> event) {
									if (event.isRight()) {
										if (event.right().getValue() != null && event.right().getValue().size() > 0) {
											notifyTimeline(request, user, body, subjectId, UPDATE_SUBJECT_EVENT_TYPE);
											renderJson(request, event.right().getValue(), 200);
										}
									} else {
										JsonObject error = new JsonObject().putString("error", event.left().getValue());
										renderJson(request, error, 400);
									}
								}
							};
							subjectService.update(categoryId, subjectId, body, user, handler);
						}
						catch (Exception e) {
							renderErrorException(request, e);
						}
					}
				});

			}
		});
	}

	public void delete(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					subjectService.delete(categoryId, subjectId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}

	private void notifyTimeline(final HttpServerRequest request, final UserInfos user, final JsonObject subject, final String subjectId, final String eventType){
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		categoryService.getOwnerAndShared(categoryId, user, new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {

				if (event.isLeft()) {
					StringBuilder message = new StringBuilder("Error when getting owner and shared of category ").append(categoryId);
					message.append(". Unable to send ").append(eventType)
						.append(" timeline notification :").append(event.left());

					log.error(message);
				}
				else {
					JsonObject result = event.right().getValue();
					if(result == null ||
							(result.getObject("owner", null) == null && result.getArray("shared", null) == null)) {
						log.error("Unable to send " + eventType
								+ " timeline notification. No owner nor shared found for category " + categoryId);
						return;
					}

					String ownerId = result.getObject("owner").getString("userId", null);
					if(ownerId == null || ownerId.isEmpty()) {
						log.error("Unable to send " + eventType
								+ " timeline notification. OwnerId not found for category "  +categoryId);
						return;
					}

					final List<String> recipients = new ArrayList<String>();
					// 1) Add category's owner to recipients
					if(!ownerId.equals(user.getUserId())) {
						recipients.add(ownerId);
					}

					// 2) Add users in array "shared" to recipients
					JsonArray shared = result.getArray("shared");

					if(shared != null && shared.size() > 0) {
						JsonObject jo;
						String uId, groupId;
						final AtomicInteger remaining = new AtomicInteger(shared.size());

						for(int i=0; i<shared.size(); i++){
							jo = shared.get(i);
							if(jo.containsField("userId")){
								uId = ((JsonObject) shared.get(i)).getString("userId");
								if(!uId.equals(user.getUserId()) && !recipients.contains(uId)){
									recipients.add(uId);
								}
								remaining.getAndDecrement();
							}
							else if(jo.containsField("groupId")){
								groupId = jo.getString("groupId");
								if (groupId != null) {
									// Get users' ids of the group (exclude current userId)
									UserUtils.findUsersInProfilsGroups(groupId, eb, user.getUserId(), false, new Handler<JsonArray>() {
										@Override
										public void handle(JsonArray event) {
											if (event != null) {
												String userId = null;
												for (Object o : event) {
													if (!(o instanceof JsonObject)) continue;
													userId = ((JsonObject) o).getString("id");
													if(!userId.equals(user.getUserId()) && !recipients.contains(userId)){
														recipients.add(userId);
													}
												}
											}
											if (remaining.decrementAndGet() < 1 && !recipients.isEmpty()) {
												sendNotify(request, recipients, user, subject, subjectId, eventType);
											}
										}
									});
								}
							}
						}

						if (remaining.get() < 1 && !recipients.isEmpty()) {
							sendNotify(request, recipients, user, subject, subjectId, eventType);
						}
					}

				}

			}
		});
	}

	private void sendNotify(final HttpServerRequest request, final List<String> recipients, final UserInfos user,
			final JsonObject subject, final String subjectId, final String eventType){

		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);

		String notificationName = null;
		if (NEW_SUBJECT_EVENT_TYPE.equals(eventType)) {
			notificationName = "forum.subject-created";
		}
		else if(UPDATE_SUBJECT_EVENT_TYPE.equals(eventType)){
			notificationName = "forum.subject-updated";
		}

		JsonObject params = new JsonObject()
			.putString("profilUri", "/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
			.putString("username", user.getUsername())
			.putString("subject", subject.getString("title"))
			.putString("subjectUri", container.config().getString("host", "http://localhost:8024") +
					pathPrefix + "#/view/" + categoryId + "/" + subjectId);
		params.putString("resourceUri", params.getString("subjectUri"));

		if (subjectId != null && !subjectId.isEmpty()) {
			notification.notifyTimeline(request, notificationName, user, recipients, categoryId, params);
		}
	}
}
