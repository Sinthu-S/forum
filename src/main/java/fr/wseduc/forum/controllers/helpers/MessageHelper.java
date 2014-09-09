package fr.wseduc.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.entcore.common.notification.TimelineHelper;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.forum.services.MessageService;
import fr.wseduc.forum.services.SubjectService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.security.SecuredAction;

public class MessageHelper extends ExtractorHelper {

	private static final String CATEGORY_ID_PARAMETER = "id";
	private static final String SUBJECT_ID_PARAMETER = "subjectid";
	private static final String MESSAGE_ID_PARAMETER = "messageid";
	private static final String FORUM_NAME = "FORUM";
	private static final String NEW_MESSAGE_EVENT_TYPE = FORUM_NAME + "_NEW_MESSAGE";
	private static final String UPDATE_MESSAGE_EVENT_TYPE = FORUM_NAME + "_UPDATE_MESSAGE";
	private static final int OVERVIEW_LENGTH = 50;


	private final MessageService messageService;
	private final SubjectService subjectService;

	protected TimelineHelper notification;

	public MessageHelper(final MessageService messageService, final SubjectService subjectService) {
		this.messageService = messageService;
		this.subjectService = subjectService;
	}

	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		this.notification = new TimelineHelper(vertx, eb, container);
	}

	public void list(final HttpServerRequest request) {
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		if (categoryId == null || subjectId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					messageService.list(categoryId, subjectId, user, arrayResponseHandler(request));
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
		final String messageId = extractParameter(request, MESSAGE_ID_PARAMETER);
		if (categoryId == null || subjectId == null || messageId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					messageService.retrieve(categoryId, subjectId, messageId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}

	public void create(final HttpServerRequest request) {
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
											notifyTimeline(request, user, body, NEW_MESSAGE_EVENT_TYPE);
											renderJson(request, event.right().getValue(), 200);
										}
									} else {
										JsonObject error = new JsonObject().putString("error", event.left().getValue());
										renderJson(request, error, 400);
									}
								}
							};
							messageService.create(categoryId, subjectId, body, user, handler);
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
		final String messageId = extractParameter(request, MESSAGE_ID_PARAMETER);
		if (categoryId == null || subjectId == null || messageId == null) {
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
											notifyTimeline(request, user, body, UPDATE_MESSAGE_EVENT_TYPE);
											renderJson(request, event.right().getValue(), 200);
										}
									} else {
										JsonObject error = new JsonObject().putString("error", event.left().getValue());
										renderJson(request, error, 400);
									}
								}
							};
							messageService.update(categoryId, subjectId, messageId, body, user, handler);
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
		final String messageId = extractParameter(request, MESSAGE_ID_PARAMETER);
		if (categoryId == null || subjectId == null || messageId == null) {
			return;
		}

		extractUserFromRequest(request, new Handler<UserInfos>(){
			@Override
			public void handle(final UserInfos user) {
				try {
					messageService.delete(categoryId, subjectId, messageId, user, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}

	private void notifyTimeline(final HttpServerRequest request, final UserInfos user, final JsonObject message, final String eventType){
		final String categoryId = extractParameter(request, CATEGORY_ID_PARAMETER);
		final String subjectId = extractParameter(request, SUBJECT_ID_PARAMETER);
		subjectService.getSubjectTitle(categoryId, subjectId, user, new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					final JsonObject subject = event.right().getValue();
					messageService.getContributors(categoryId, subjectId, user, new Handler<Either<String, JsonArray>>() {
						@Override
						public void handle(Either<String, JsonArray> event) {
							final List<String> ids = new ArrayList<String>();
							if (event.isRight()) {
								// get all owners
								JsonArray owners = event.right().getValue();
								if (owners.size() > 0) {
									String id = null;
									// Extract owners
									for(int i=0; i<owners.size(); i++){
										id = ((JsonObject) owners.get(i)).getString("userId");
										if(!id.equals(user.getUserId()) && !ids.contains(id)){
											ids.add(id);
										}
									}
									String template = null;
									if (eventType == NEW_MESSAGE_EVENT_TYPE) {
										template = "notify-message-created.html";
									}
									else {
										if(eventType == UPDATE_MESSAGE_EVENT_TYPE){
											template = "notify-message-updated.html";
										}
									}
									String overview = message.getString("content");
									if(overview.length() > OVERVIEW_LENGTH){
										overview = overview.substring(0, OVERVIEW_LENGTH);
										overview = overview.concat(" ...");
									}
									JsonObject params = new JsonObject()
										.putString("profilUri", container.config().getString("host") +
												"/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
										.putString("username", user.getUsername())
										.putString("subject", subject.getObject("result").getString("title"))
										.putString("subjectUri", container.config().getString("host") + pathPrefix +
												"#/view/" + categoryId + "/" + subjectId)
										.putString("overview", overview);
									if (subjectId != null && !subjectId.trim().isEmpty()) {
										notification.notifyTimeline(request, user, FORUM_NAME, eventType, ids, subjectId, template, params);
									}
								}
							}
						}
					});
				}
			}
		});
	}
}
