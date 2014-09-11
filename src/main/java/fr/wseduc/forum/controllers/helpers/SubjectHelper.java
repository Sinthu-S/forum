package fr.wseduc.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.forum.services.SubjectService;
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
		categoryService.getSharedWithIds(categoryId, user, new Handler<Either<String, JsonArray>>() {
			@Override
			public void handle(Either<String, JsonArray> event) {
				final List<String> ids = new ArrayList<String>();
				if (event.isRight()) {
					// get all ids
					JsonArray shared = event.right().getValue();
					if (shared.size() > 0) {
						JsonObject jo = null;
						String groupId = null;
						String id = null;
						final AtomicInteger remaining = new AtomicInteger(shared.size());
						// Extract shared with
						for(int i=0; i<shared.size(); i++){
							jo = shared.get(i);
							if(jo.containsField("userId")){
								id = ((JsonObject) shared.get(i)).getString("userId");
								if(!id.equals(user.getUserId()) && !ids.contains(id)){
									ids.add(id);
									remaining.getAndDecrement();
								}
							}
							else{
								if(jo.containsField("groupId")){
									groupId = jo.getString("groupId");
									if (groupId != null) {
										UserUtils.findUsersInProfilsGroups(groupId, eb, user.getUserId(), false, new Handler<JsonArray>() {
											@Override
											public void handle(JsonArray event) {
												if (event != null) {
													String userId = null;
													for (Object o : event) {
														if (!(o instanceof JsonObject)) continue;
														userId = ((JsonObject) o).getString("id");
														if(!userId.equals(user.getUserId()) && !ids.contains(userId)){
															ids.add(userId);
														}
													}
												}
												if (remaining.decrementAndGet() < 1) {
													sendNotify(request, ids, user, subject, subjectId, eventType);
												}
											}
										});
									}
								}
							}
						}
						if (remaining.get() < 1) {
							sendNotify(request, ids, user, subject, subjectId, eventType);
						}
					}
				}
			}
		});
	}

	private void sendNotify(final HttpServerRequest request, final List<String> ids, final UserInfos user, final JsonObject subject, final String subjectId, final String eventType){
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		String template = null;
		if (eventType == NEW_SUBJECT_EVENT_TYPE) {
			template = "notify-subject-created.html";
		}
		else {
			if(eventType == UPDATE_SUBJECT_EVENT_TYPE){
				template = "notify-subject-updated.html";
			}
		}
		JsonObject params = new JsonObject()
			.putString("profilUri", container.config().getString("host") +
					"/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
			.putString("username", user.getUsername())
			.putString("subject", subject.getString("title"))
			.putString("subjectUri", container.config().getString("host") + pathPrefix +
					"#/view/" + categoryId + "/" + subjectId);
		if (subjectId != null && !subjectId.isEmpty()) {
			notification.notifyTimeline(request, user, FORUM_NAME, eventType, ids, categoryId, template, params);
		}
	}
}
