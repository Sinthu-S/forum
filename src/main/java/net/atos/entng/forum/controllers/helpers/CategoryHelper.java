package net.atos.entng.forum.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;
import static org.entcore.common.user.UserUtils.getUserInfos;

import java.util.List;
import java.util.Map;

import net.atos.entng.forum.services.CategoryService;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.share.ShareService;
import org.entcore.common.share.impl.MongoDbShareService;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
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


	@Override
	public void list(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				categoryService.list(user, arrayResponseHandler(request));
			}
		});
	}

	@Override
	public void retrieve(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				String id = request.params().get(CATEGORY_ID_PARAMETER);
				categoryService.retrieve(id, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Override
	public void create(final HttpServerRequest request) {
		super.create(request);
	}

	@Override
	public void update(final HttpServerRequest request) {
		super.update(request);
	}

	@Override
	public void delete(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				final String id = request.params().get(CATEGORY_ID_PARAMETER);
				Handler<Either<String,JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
					@Override
					public void handle(Either<String, JsonObject> event) {
						if (event.isRight()) {
							categoryService.delete(id, user, defaultResponseHandler(request));
						} else {
							JsonObject error = new JsonObject().putString("error", event.left().getValue());
							Renders.renderJson(request, error, 400);
						}
					}
				};
				categoryService.deleteSubjects(id, user, handler);
			}
		});
	}

	public void share(final HttpServerRequest request) {
		shareJson(request, false);
	}

	public void shareSubmit(final HttpServerRequest request) {
		getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					final String categoryId = request.params().get("id");
					if(categoryId == null || categoryId.trim().isEmpty()) {
			            badRequest(request);
			            return;
			        }
					JsonObject params = new JsonObject()
					.putString("profilUri", "/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
					.putString("username", user.getUsername())
					.putString("resourceUri", container.config().getString("host", "http://localhost:8024") +
							pathPrefix + "#/view/" + categoryId);
					shareJsonSubmit(request, "forum.category-shared", false, params, "name");
				} else {
					unauthorized(request);
				}
			}
		});
	}

	public void shareRemove(final HttpServerRequest request) {
		removeShare(request, false);
	}
}
