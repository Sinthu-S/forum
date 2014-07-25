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
		super.create(request);
	}
	
	public void update(final HttpServerRequest request) {
		super.update(request);
	}
	
	public void delete(final HttpServerRequest request) {
		super.delete(request);
		// TODO IMPROVE : Should also Delete all subjects in this Category
	}
	
	public void share(final HttpServerRequest request) {
		shareJson(request, false);
	}
	
	public void shareSubmit(final HttpServerRequest request) {
		shareJsonSubmit(request, null, false);
	}
	
	public void shareRemove(final HttpServerRequest request) {
		removeShare(request, false);
	}
}
