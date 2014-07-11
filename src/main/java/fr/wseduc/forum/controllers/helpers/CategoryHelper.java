package fr.wseduc.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;
import static org.entcore.common.user.UserUtils.getUserInfos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.share.ShareService;
import org.entcore.common.share.impl.MongoDbShareService;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.forum.services.CategoryService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.security.SecuredAction;

public class CategoryHelper extends MongoDbControllerHelper {

	private final String managedCollection;
	private final String type;
	
	private final CategoryService categoryService;
	private ShareService shareService;
	
	private static final String CATEGORY_ID_PARAMETER = "id";
	
	public CategoryHelper(final String managedCollection, final CategoryService categoryService) {
		this(managedCollection, categoryService, null);
	}
	
	public CategoryHelper(final String managedCollection, final CategoryService categoryService, final Map<String, List<String>> groupedActions) {
		super(managedCollection, groupedActions);
		this.managedCollection = managedCollection;
		this.type = managedCollection.toUpperCase();
		this.categoryService = categoryService;
	}
	
	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
	
		this.shareService = new MongoDbShareService(eb, mongo, managedCollection, securedActions, null);
	}
	
	
	public void list(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				categoryService.list(user, arrayResponseHandler(request));
			}
		});
	}
	
	public void retrieve(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				String id = request.params().get(CATEGORY_ID_PARAMETER);
				categoryService.retrieve(id, user, notEmptyResponseHandler(request));
			}
		});
	}
	
	public void create(final HttpServerRequest request) {
		create(request);
	}
	
	public void update(final HttpServerRequest request) {
		update(request);
	}
	
	public void delete(final HttpServerRequest request) {
		delete(request);
	}
	
	public void share(final HttpServerRequest request) {
		shareJson(request);
	}
	
	public void shareSubmit(final HttpServerRequest request) {
		shareJsonSubmit(request, null);
	}
	
	public void shareRemove(final HttpServerRequest request) {
		removeShare(request);
	}
	
	
	@Override
	public void shareJson(final HttpServerRequest request) {
		final String id = request.params().get(CATEGORY_ID_PARAMETER);
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}
		getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					shareService.shareInfos(user.getUserId(), id,
							I18n.acceptLanguage(request), defaultResponseHandler(request));
				} else {
					unauthorized(request);
				}
			}
		});
	}
	
	@Override
	protected void shareJsonSubmit(final HttpServerRequest request, final String notifyShareTemplate) {
		final String id = request.params().get(CATEGORY_ID_PARAMETER);
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}
		request.expectMultiPart(true);
		request.endHandler(new VoidHandler() {
			@Override
			protected void handle() {
				final List<String> a = request.formAttributes().getAll("actions");
				final String groupId = request.formAttributes().get("groupId");
				final String userId = request.formAttributes().get("userId");
				if (a == null || a.size() == 0) {
					badRequest(request);
					return;
				}
				final List<String> actions = new ArrayList<>();
				for (Object o: a) {
					if (o != null && o instanceof String) {
						actions.add(o.toString());
					}
				}
				getUserInfos(eb, request, new Handler<UserInfos>() {
					@Override
					public void handle(final UserInfos user) {
						if (user != null) {
							Handler<Either<String, JsonObject>> r = new Handler<Either<String, JsonObject>>() {
								@Override
								public void handle(Either<String, JsonObject> event) {
									if (event.isRight()) {
										JsonObject n = event.right().getValue()
												.getObject("notify-timeline");
										if (n != null && notifyShareTemplate != null) {
											notifyShare(request, id, user, new JsonArray().add(n),
													notifyShareTemplate);
										}
										renderJson(request, event.right().getValue());
									} else {
										JsonObject error = new JsonObject()
												.putString("error", event.left().getValue());
										renderJson(request, error, 400);
									}
								}
							};
							if (groupId != null) {
								shareService.groupShare(user.getUserId(), groupId, id, actions, r);
							} else if (userId != null) {
								shareService.userShare(user.getUserId(), userId, id, actions, r);
							} else {
								badRequest(request);
							}
						} else {
							unauthorized(request);
						}
					}
				});
			}
		});
	}

	@Override
	protected void removeShare(final HttpServerRequest request) {
		final String id = request.params().get(CATEGORY_ID_PARAMETER);
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}
		request.expectMultiPart(true);
		request.endHandler(new VoidHandler() {
			@Override
			protected void handle() {
				final List<String> a = request.formAttributes().getAll("actions");
				final String groupId = request.formAttributes().get("groupId");
				final String userId = request.formAttributes().get("userId");
				if (a == null || a.size() == 0) {
					badRequest(request);
					return;
				}
				final List<String> actions = new ArrayList<>();
				for (Object o: a) {
					if (o != null && o instanceof String) {
						actions.add(o.toString());
					}
				}
				getUserInfos(eb, request, new Handler<UserInfos>() {
					@Override
					public void handle(final UserInfos user) {
						if (user != null) {
							if (groupId != null) {
								shareService.removeGroupShare(groupId, id, actions,
										defaultResponseHandler(request));
							} else if (userId != null) {
								shareService.removeUserShare(userId, id, actions,
										defaultResponseHandler(request));
							} else {
								badRequest(request);
							}
						} else {
							unauthorized(request);
						}
					}
				});
			}
		});
	}
	
	protected void notifyShare(final HttpServerRequest request, final String resource,
			final UserInfos user, JsonArray sharedArray, final String notifyShareTemplate) {
		final List<String> recipients = new ArrayList<>();
		final AtomicInteger remaining = new AtomicInteger(sharedArray.size());
		for (Object j : sharedArray) {
			JsonObject json = (JsonObject) j;
			String userId = json.getString("userId");
			if (userId != null) {
				recipients.add(userId);
				remaining.getAndDecrement();
			} else {
				String groupId = json.getString("groupId");
				if (groupId != null) {
					UserUtils.findUsersInProfilsGroups(groupId, eb, user.getUserId(), false, new Handler<JsonArray>() {
						@Override
						public void handle(JsonArray event) {
							if (event != null) {
								for (Object o : event) {
									if (!(o instanceof JsonObject)) continue;
									JsonObject j = (JsonObject) o;
									String id = j.getString("id");
									log.debug(id);
									recipients.add(id);
								}
							}
							if (remaining.decrementAndGet() < 1) {
								sendNotify(request, resource, user, recipients, notifyShareTemplate);
							}
						}
					});
				}
			}
		}
		if (remaining.get() < 1) {
			sendNotify(request, resource, user, recipients, notifyShareTemplate);
		}
	}

	protected void sendNotify(final HttpServerRequest request, final String resource,
			final UserInfos user, final List<String> recipients, final String notifyShareTemplate) {
		final JsonObject params = new JsonObject()
				.putString("uri", container.config().getString("userbook-host") +
						"/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
				.putString("username", user.getUsername())
				.putString("resourceUri", container.config().getString("host", "http://localhost:8011") +
						pathPrefix + "/document/" + resource);
		mongo.findOne(managedCollection, new JsonObject().putString("_id", resource),
				new JsonObject().putNumber("name", 1), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				if ("ok".equals(event.body().getString("status")) && event.body().getObject("result") != null) {
					params.putString("resourceName", event.body().getObject("result").getString("name", ""));
					notification.notifyTimeline(request, user, type, type + "_SHARE",
							recipients, resource, notifyShareTemplate, params);
				} else {
					log.error("Unable to send timeline notification : missing name on resource " + resource);
				}
			}
		});
	}
}
