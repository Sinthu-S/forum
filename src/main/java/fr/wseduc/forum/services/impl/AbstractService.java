package fr.wseduc.forum.services.impl;

import java.util.Map;

import org.entcore.common.notification.TimelineHelper;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.security.SecuredAction;

public abstract class AbstractService {

	protected final String categories_collection;
	protected final String subjects_collection;
	
	protected EventBus eb;
	protected MongoDb mongo;
	protected TimelineHelper notification;
	
	public AbstractService(final String categories_collection, final String subjects_collection) {
		this.categories_collection = categories_collection;
		this.subjects_collection = subjects_collection;
	}
	
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		this.eb = vertx.eventBus();
		this.mongo = MongoDb.getInstance();
		this.notification = new TimelineHelper(vertx, eb, container);
	}
}
