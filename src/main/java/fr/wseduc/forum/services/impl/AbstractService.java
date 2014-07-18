package fr.wseduc.forum.services.impl;

import fr.wseduc.mongodb.MongoDb;

public abstract class AbstractService {

	protected final String categories_collection;
	protected final String subjects_collection;
	protected final MongoDb mongo;
	
	public AbstractService(final String categories_collection, final String subjects_collection) {
		this.categories_collection = categories_collection;
		this.subjects_collection = subjects_collection;
		this.mongo = MongoDb.getInstance();
	}
}
